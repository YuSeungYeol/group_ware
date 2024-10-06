package com.ware.spring.chat.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ware.spring.chat.domain.ChatMsg;
import com.ware.spring.chat.domain.ChatMsgDto;
import com.ware.spring.chat.domain.ChatRoom;
import com.ware.spring.chat.domain.ChatRoomDto;
import com.ware.spring.chat.repository.ChatMsgRepository;
import com.ware.spring.chat.repository.ChatRoomRepository;
import com.ware.spring.member.repository.MemberRepository;

import jakarta.transaction.Transactional;

@Service
public class ChatMsgService {
	
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMsgRepository chatMsgRepository;
	private final MemberRepository memberRepository;
	
	@Autowired
	public ChatMsgService(ChatRoomRepository chatRoomRepositroy,MemberRepository memberRepository, ChatMsgRepository chatMsgRepository) {
		this.chatRoomRepository = chatRoomRepositroy;
		this.chatMsgRepository = chatMsgRepository;
		this.memberRepository = memberRepository;
	}
	 

	@Transactional
	public void updateReceiverReadStatus(Long roomNo, String readStatus) {
	    ChatRoom chatRoom = chatRoomRepository.findByroomNo(roomNo);
	    
	    // roomNo에 해당하는 모든 메시지를 읽음 처리
	    List<ChatMsg> messages = chatMsgRepository.findAllByChatRoom(chatRoom);
	    
	    for (ChatMsg msg : messages) {
	        if (!msg.getIsReceiverRead().equals(readStatus)) {
	            ChatMsgDto msgDto = new ChatMsgDto().toDto(msg);
	            msgDto.setIs_receiver_read(readStatus);
	            ChatMsg updatedMsg = msgDto.toEntity();
	            chatMsgRepository.save(updatedMsg);
	        }
	    }
	}
	
	
	// 채팅메시지 생성
	public int createChatMsg(ChatMsgDto dto) {
		int result = -1;
		try {
			
			ChatRoom room = chatRoomRepository.findByroomNo(dto.getRoom_no());
			
			ChatRoomDto roomDto = new ChatRoomDto().toDto(room);
			
			
			
			ChatMsg msg = ChatMsg.builder()
					.chatContent(dto.getChat_content())
					.isFromSender(dto.getIs_from_sender())
					.isReceiverRead("N")
					.chatRoom(room)
					.build();
			chatMsgRepository.save(msg);
			
			// 채팅방 최신 메시지와 날짜 업데이트
			roomDto.setLast_msg(msg.getChatContent());
			roomDto.setLast_date(msg.getSendDate());
			
			//Dto Entity 전환
			ChatRoom updateRoom = roomDto.toEntity();
			
			//업데이트된 채팅방 저장
			chatRoomRepository.save(updateRoom);
			System.out.println("ChatMsg 객체: " + msg);
			System.out.println("ChatRoomDto 객체: " + roomDto);
			System.out.println("ChatRoom 업데이트 객체: " + updateRoom);
			result = 1;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Transactional
	public List<ChatMsgDto> selectChatMsgList(Long roomNo, String memId) {
	    ChatRoom chatRoom = chatRoomRepository.findByroomNo(roomNo);
	    List<ChatMsg> selectChatMsgList = chatMsgRepository.findAllBychatRoom(chatRoom);
	    List<ChatMsgDto> selectChatMsgDtoList = new ArrayList<>();

	    for (ChatMsg cm : selectChatMsgList) {
	        ChatMsgDto dto = new ChatMsgDto().toDto(cm);

	        // 메시지의 송신자와 수신자 설정
	        if (cm.getIsFromSender().equals("Y")) {
	            dto.setSender_id(cm.getChatRoom().getFromId());
	            dto.setReceiver_id(cm.getChatRoom().getToId());
	        } else {
	            dto.setSender_id(cm.getChatRoom().getToId());
	            dto.setReceiver_id(cm.getChatRoom().getFromId());
	        }

	        // 본인 여부 플래그 설정
	        if (dto.getSender_id().equals(memId)) {
	            dto.setMe_flag("Y");
	        } else {
	            dto.setMe_flag("N");
	        }

	        // 수신자일 경우 읽음 표시 업데이트
	        if (dto.getMe_flag().equals("N") && !dto.getIs_receiver_read().equals("Y")) {
	            dto.setIs_receiver_read("Y");
	            ChatMsg updatedEntity = dto.toEntity();
	            chatMsgRepository.save(updatedEntity); // 읽음 상태를 데이터베이스에 업데이트
	        }

	        selectChatMsgDtoList.add(dto);
	    }

	    return selectChatMsgDtoList;
	}

}
