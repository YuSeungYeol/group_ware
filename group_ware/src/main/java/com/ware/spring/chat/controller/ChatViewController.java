package com.ware.spring.chat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ware.spring.chat.domain.ChatMsgDto;
import com.ware.spring.chat.domain.ChatRoomDto;
import com.ware.spring.chat.service.ChatMsgService;
import com.ware.spring.chat.service.ChatRoomService;
import com.ware.spring.member.domain.MemberDto;
import com.ware.spring.member.service.MemberService;

@Controller
public class ChatViewController {

	private final MemberService memberService;
	private final ChatRoomService chatRoomService;
	private final ChatMsgService chatMsgService;
	
	@Autowired
	public ChatViewController(MemberService memberService, ChatRoomService chatRoomService, ChatMsgService chatMsgService) {
		this.memberService = memberService;
		this.chatRoomService = chatRoomService;
		this.chatMsgService = chatMsgService;
	}
	 
	@GetMapping("/chat/{room_no}")
	public String startChatting(@PathVariable("room_no") Long room_no, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		String memId = user.getUsername();
		
		
		ChatRoomDto dto = chatRoomService.selectChatRoomOne(room_no, memId);
	    if (dto == null) {
	        throw new RuntimeException("ChatRoomDto를 찾을 수 없습니다.");
	    }
		
		model.addAttribute("dto",dto);
		
		List<ChatMsgDto> resultList = chatMsgService.selectChatMsgList(room_no, memId);
		model.addAttribute("resultList",resultList);
		
		List<ChatRoomDto> chatRoomList = chatRoomService.selectChatRoomList(memId);
		model.addAttribute("chatRoomList",chatRoomList);
		
		return "chat/detail";
	}
	
	@GetMapping("/chat/room/list")
	public String selectChatRoom(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		String memId = user.getUsername();
		
		List<ChatRoomDto> resultList = chatRoomService.selectChatRoomList(memId);
		model.addAttribute("resultList",resultList);
		
		return "chat/list";
	}
	
	// 멤버서비스에 채팅있음
	@GetMapping("/chat/room/create")
	public String chatRoomList(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		
		String memId = user.getUsername();
		List<MemberDto> resultList = memberService.findAllForChat(memId);
		model.addAttribute("resultList",resultList);
		return "chat/create";
	}
}
