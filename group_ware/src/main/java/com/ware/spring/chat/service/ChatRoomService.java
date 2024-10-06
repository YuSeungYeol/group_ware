package com.ware.spring.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.ware.spring.chat.domain.ChatMsg;
import com.ware.spring.chat.domain.ChatRoom;
import com.ware.spring.chat.domain.ChatRoomDto;
import com.ware.spring.chat.repository.ChatMsgRepository;
import com.ware.spring.chat.repository.ChatRoomRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.security.vo.SecurityUser;

@Service
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMsgRepository chatMsgRepository;
	private final MemberRepository memberRepository;
	
	public ChatRoomService(ChatRoomRepository chatRoomRepository, MemberRepository memberRepositroy, ChatMsgRepository chatMsgRepository) {
		this.chatRoomRepository = chatRoomRepository;
		this.memberRepository = memberRepositroy;
		this.chatMsgRepository = chatMsgRepository;
	}
	 
	public ChatRoomDto selectChatRoomOne(Long roomNo, String memId) {
		ChatRoom chatRoom = chatRoomRepository.findByroomNo(roomNo);
		
		ChatRoomDto dto = new ChatRoomDto().toDto(chatRoom);
		if (memId.equals(dto.getFrom_id())) {
            // 상대방 아이디 -> 상대방 이름
            // (1) ChatRoomDto에 필드(not_me_name) 추가
            // (2) MemberRepository한테 부탁해서 회원 정보 조회(아이디 기준)
            Optional<Member> temp = memberRepository.findByMemId(dto.getTo_id());
            // Optional에서 값이 있는지 체크하고, 있을 때만 이름을 셋팅
            if (temp.isPresent()) {
                // (3) ChatRoomDto의 not_me_name필드에 회원 이름 셋팅
                // (4) 목록 화면에 상대방 아이디 -> 이름
                dto.setNot_me_name(temp.get().getMemName()); // Optional에서 Member를 꺼내서 사용
                dto.setNot_me_id(dto.getTo_id());
            }
        } else {
            // 2. 지금 로그인한 사용자 == toId -> 상대방 : fromId
            Optional<Member> temp = memberRepository.findByMemId(dto.getFrom_id());
            // Optional에서 값이 있는지 체크하고, 있을 때만 이름을 셋팅
            if (temp.isPresent()) {
                dto.setNot_me_name(temp.get().getMemName()); // Optional에서 Member를 꺼내서 사용
                dto.setNot_me_id(dto.getFrom_id());
            }
        }
		return dto;
	}
	
	public int createChatRoom(ChatRoomDto dto) {
		int result = -1;
		try {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    
	    Member member = ((SecurityUser) userDetails).getMember();
	    
	    dto.setFrom_id(member.getMemId());
	    ChatRoom chatRoom = dto.toEntity();
	    chatRoom = chatRoomRepository.save(chatRoom);
	    result = 1;
	    
		}catch(Exception e) {
			e.printStackTrace();
		}
	    // 응답 데이터 형식을 명확하게 지정
	    return result;
	}


	public List<ChatRoomDto> selectChatRoomList(String memId) {
	    // 데이터베이스에서 채팅방 목록 가져오기
	    List<ChatRoom> chatRoomList = chatRoomRepository.findAllByfromIdAndtoId(memId);

	    List<ChatRoomDto> chatRoomDtoList = new ArrayList<>();
	    for (ChatRoom cr : chatRoomList) {
	        ChatRoomDto dto = new ChatRoomDto().toDto(cr);
	        ChatMsg chatMsg = chatMsgRepository.findByRoomNoOne(cr.getRoomNo());
	        
	        // 로그 추가
	        System.out.println("ChatRoom RoomNo: " + cr.getRoomNo());
	        if (chatMsg == null) {
	            System.out.println("ChatMsg is null for RoomNo: " + cr.getRoomNo());
	        } else {
	            System.out.println("ChatMsg isFromSender: " + chatMsg.getIsFromSender());
	            System.out.println("ChatMsg isReceiverRead: " + chatMsg.getIsReceiverRead());
	        }
	        
/*
			 * ChatMsg chatMsg = chatMsgRepository.findByRoomNoOne(cr.getRoomNo());
			 * if(chatMsg != null) { dto.setIs_receiver_read(chatMsg.getIsReceiverRead()); }
			 */
	        // 최근 메시지의 송신자와 수신자 설정
	        if (chatMsg != null) {
	            // 송신자 및 수신자 설정
	            if ("Y".equals(chatMsg.getIsFromSender())) {
	                dto.setSender_id(chatMsg.getChatRoom().getFromId());
	                dto.setReceiver_id(chatMsg.getChatRoom().getToId());
	            } else {
	                dto.setSender_id(chatMsg.getChatRoom().getToId());
	                dto.setReceiver_id(chatMsg.getChatRoom().getFromId());
	            }

	            // 읽음 여부 설정
	            dto.setIs_receiver_read(chatMsg.getIsReceiverRead());
	        } else {
	            // 필요한 경우 기본값 설정 또는 로깅
	            System.out.println("ChatMsg is null for RoomNo: " + cr.getRoomNo());
	        }

	        // 상대방 이름 설정
	        if (memId.equals(dto.getFrom_id())) {
	            // 현재 로그인한 사용자가 방 개설자인 경우
	            Optional<Member> temp = memberRepository.findByMemId(dto.getTo_id());
	            if (temp.isPresent()) {
	                dto.setNot_me_name(temp.get().getMemName());
	                dto.setNot_me_id(dto.getTo_id());
	            }
	        } else {
	            // 현재 로그인한 사용자가 방 개설자가 아닌 경우
	            Optional<Member> temp = memberRepository.findByMemId(dto.getFrom_id());
	            if (temp.isPresent()) {
	                dto.setNot_me_name(temp.get().getMemName());
	                dto.setNot_me_id(dto.getFrom_id());
	            }
	        }

	        chatRoomDtoList.add(dto);
	    }
	    // PageImpl이 아닌 List를 반환
	    return chatRoomDtoList;
	}

	
	/*
	 * public List<ChatRoomDto> selectChatRoomList(ChatRoomDto dto, String memId) {
	 * //데이터베이스에서 채팅방 목록 가져오기 List<ChatRoom> chatRoomList =
	 * chatRoomRepository.findAllByfromIdAndtoId(memId); // 조건에 맞는 채팅방
	 * 
	 * List<ChatRoomDto> chatRoomDtoList = chatRoomList.stream() .map(chatRoom ->
	 * ChatRoomDto.builder() .room_name(chatRoom.getRoomName())
	 * .from_id(chatRoom.getFromId()) .to_id(chatRoom.getToId())
	 * .is_group(chatRoom.getIsGroup()) .last_msg(chatRoom.getLastMsg())
	 * .last_date(chatRoom.getLastDate()) .build()) .collect(Collectors.toList());
	 * 
	 * return chatRoomDtoList; }
	 */
}
