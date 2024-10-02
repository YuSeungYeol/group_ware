package com.ware.spring.chat.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	// JSON 형식으로 데이터를 반환하도록 수정된 컨트롤러
	@GetMapping("/chat/room/list/data")
	@ResponseBody // 이 어노테이션을 추가하여 JSON 형식으로 응답
	public ResponseEntity<Page<ChatRoomDto>> selectListChatRoom(
	        @PageableDefault(page = 0, size = 10, sort = "lastDate", direction = Sort.Direction.DESC) Pageable pageable) {
	    
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    User user = (User) authentication.getPrincipal();
	    String memId = user.getUsername();
	    
	    // 서비스에서 채팅방 리스트를 가져옴
	    Page<ChatRoomDto> resultList = chatRoomService.selectChatRoomList(pageable, memId);
	    
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
