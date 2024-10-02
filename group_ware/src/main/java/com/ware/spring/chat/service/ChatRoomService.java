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

import com.ware.spring.chat.domain.ChatRoom;
import com.ware.spring.chat.domain.ChatRoomDto;
import com.ware.spring.chat.repository.ChatRoomRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.security.vo.SecurityUser;

@Service
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final MemberRepository memberRepository;
	
	public ChatRoomService(ChatRoomRepository chatRoomRepository, MemberRepository memberRepositroy) {
		this.chatRoomRepository = chatRoomRepository;
		this.memberRepository = memberRepositroy;
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

	
	public Page<ChatRoomDto> selectChatRoomList(Pageable pageable, String memId) {
	    // 데이터베이스에서 채팅방 목록 가져오기
	    Page<ChatRoom> chatRoomList = chatRoomRepository.findAllByfromIdAndtoId(memId, pageable); // 조건에 맞는 채팅방
	    
	    // 쿼리 결과 확인을 위한 로깅 추가
	    System.out.println("조회된 채팅방 수: " + chatRoomList.getTotalElements());
	    
	    List<ChatRoomDto> chatRoomDtoList = new ArrayList<>();
	    for (ChatRoom cr : chatRoomList) {
	        ChatRoomDto dto = new ChatRoomDto().toDto(cr);
	        // 상대방 이름 셋팅
	        // 1. 지금 로그인한 사용자 == fromId -> 상대방 : toId
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
	        chatRoomDtoList.add(dto);
	    }
	    return new PageImpl<>(chatRoomDtoList, pageable, chatRoomList.getTotalElements());
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
