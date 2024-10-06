package com.ware.spring.chat.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ware.spring.chat.domain.ChatRoomDto;
import com.ware.spring.chat.service.ChatMsgService;
import com.ware.spring.chat.service.ChatRoomService;

@Controller
public class ChatRoomApiController {
	
	private final ChatRoomService chatRoomService;
	private final ChatMsgService chatMsgService;
	
	@Autowired
	public ChatRoomApiController(ChatRoomService chatRoomService, ChatMsgService chatMsgService) {
		this.chatRoomService = chatRoomService;
		this.chatMsgService = chatMsgService;
	}
	
	
	@ResponseBody
	@PostMapping("/chat/room/create")
	public Map<String,String> createChatRoom(@RequestBody ChatRoomDto dto){
		Map<String,String> resultMap = new HashMap<String,String>();
		resultMap.put("res_code", "404");
		resultMap.put("res_msg", "채팅방 생성중 오류가 발생하였습니다.");
		
		if(chatRoomService.createChatRoom(dto) > 0) {
			resultMap.put("res_code", "200");
			resultMap.put("res_msg", "성공적으로 채팅생성이 완료되었습니다.");
		}
		
		return resultMap;
	}
	@GetMapping("/chat/room/list/data")
	@ResponseBody
	public ResponseEntity<List<ChatRoomDto>> selectListChatRoom() {
	    
	    // 현재 인증된 사용자의 정보를 가져옴
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    Object principal = authentication.getPrincipal();
	    String memId;

	    // principal이 UserDetails 타입인 경우 처리
	    if (principal instanceof UserDetails) {
	        UserDetails userDetails = (UserDetails) principal;
	        memId = userDetails.getUsername();
	    } 
	    // principal이 String 타입인 경우 처리
	    else if (principal instanceof String) {
	        memId = (String) principal;
	    } else {
	        // 예상치 못한 타입인 경우의 처리
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	    
	    // 서비스에서 채팅방 리스트를 가져옴
	    List<ChatRoomDto> resultList = chatRoomService.selectChatRoomList(memId);
	    
	    // JSON 형식으로 반환
	    return ResponseEntity.ok(resultList);
	}

	
	/*
	 * @ResponseBody
	 * 
	 * @PostMapping("/chat/room/create") public Map<String,String>
	 * createChatRoom(@RequestBody ChatRoomDto dto){ Map<String,String> resultMap =
	 * new HashMap<String,String>(); resultMap.put("res_code","404");
	 * resultMap.put("res_msg", "채팅방 생성이 실패하였습니다.");
	 * 
	 * if(chatRoomService.createChatRoom(dto)) { resultMap.put("res_code", "200");
	 * resultMap.put("res_msg", "채팅방이 생성되었습니다."); } return resultMap; }
	 */
}
