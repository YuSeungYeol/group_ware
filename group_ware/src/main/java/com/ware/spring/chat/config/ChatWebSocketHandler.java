package com.ware.spring.chat.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ware.spring.chat.domain.ChatMsgDto;
import com.ware.spring.chat.repository.ChatMsgRepository;
import com.ware.spring.chat.service.ChatMsgService;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMsgService chatMsgService;
    // 채팅방 별 세션 관리
    private final Map<Long, List<WebSocketSession>> roomSessions = new HashMap<>();
    // 모든 채팅방의 메시지를 수신하기 위한 공통 채널 세션 관리
    private final List<WebSocketSession> commonChannelSessions = new ArrayList<>();

    private final ChatMsgRepository chatMsgRepository;
    @Autowired
    public ChatWebSocketHandler(ChatMsgRepository chatMsgRepository, ChatMsgService chatMsgService) {
        this.chatMsgService = chatMsgService;
        this.chatMsgRepository = chatMsgRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = session.getUri().getPath();

        // 공통 채널에 대한 세션 추가
        if ("/chatting/all".equals(path)) {
            commonChannelSessions.add(session);
            System.out.println("Session added to common channel");
            return;
        }

        // 채팅방 번호 추출하여 세션 추가 (예: /chatting/18)
        String roomNoStr = path.substring(path.lastIndexOf('/') + 1);
        Long roomNo = Long.valueOf(roomNoStr);

        List<WebSocketSession> sessions = roomSessions.computeIfAbsent(roomNo, k -> new ArrayList<>());
        if (!sessions.contains(session)) {
            sessions.add(session);
            System.out.println("Session added to room " + roomNo);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ObjectMapper objMapper = new ObjectMapper();
        ChatMsgDto msg = objMapper.readValue(payload, ChatMsgDto.class);

        String path = session.getUri().getPath();
        Long roomNo = Long.valueOf(path.substring(path.lastIndexOf('/') + 1));

        switch (msg.getChat_type()) {
            case "open":
                List<WebSocketSession> sessions = roomSessions.computeIfAbsent(roomNo, k -> new ArrayList<>());
                if (!sessions.contains(session)) {
                    sessions.add(session);
                    System.out.println("Session added to room " + roomNo);
                }
                break;

            case "msg":
                chatMsgService.createChatMsg(msg);
                System.out.println("Message saved to database: " + msg.getChat_content());

                // 해당 방의 모든 세션에게 메시지 전송
                for (WebSocketSession wsSession : roomSessions.getOrDefault(roomNo, new ArrayList<>())) {
                    if (wsSession.getId().equals(session.getId())) {
                        msg.setIs_from_sender("Y"); // 송신자로 설정
                    } else {
                        msg.setIs_from_sender("N"); // 수신자로 설정}
                    }
                    if (wsSession.isOpen()) {
                        wsSession.sendMessage(new TextMessage(objMapper.writeValueAsString(msg)));
                     }
                    
                }
                
                // 모든 사용자가 방에 있을 때 읽음 처리
                List<WebSocketSession> roomSessionsForRoom = roomSessions.get(roomNo);
                if (roomSessionsForRoom != null && roomSessionsForRoom.size() == 2) {  // 1:1 채팅방에서 두 사용자가 모두 연결된 경우
                    chatMsgService.updateReceiverReadStatus(roomNo, "Y");
                }

                // 공통 채널 세션에도 메시지 전송
                for (WebSocketSession wsSession : commonChannelSessions) {
                    if (wsSession.isOpen()) {
                        wsSession.sendMessage(new TextMessage(objMapper.writeValueAsString(msg)));
                    }
                }
                break;

            default:
                System.out.println("Unknown message type: " + msg.getChat_type());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String path = session.getUri().getPath();
        if ("/chatting/all".equals(path)) {
            commonChannelSessions.remove(session);
            System.out.println("Session removed from common channel");
        } else {
            Long roomNo = Long.valueOf(path.substring(path.lastIndexOf('/') + 1));
            roomSessions.getOrDefault(roomNo, new ArrayList<>()).remove(session);
            System.out.println("Session removed from room " + roomNo);
        }
    }
}
