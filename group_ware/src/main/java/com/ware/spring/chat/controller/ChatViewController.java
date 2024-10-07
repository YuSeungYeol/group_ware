package com.ware.spring.chat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ware.spring.chat.domain.ChatMsgDto;
import com.ware.spring.chat.domain.ChatRoomDto;
import com.ware.spring.chat.service.ChatMsgService;
import com.ware.spring.chat.service.ChatRoomService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.domain.MemberDto;
import com.ware.spring.member.service.MemberService;
import com.ware.spring.security.vo.SecurityUser;

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
	public String listChatRoomsAndMembers(
	        @RequestParam(value = "statusFilter", required = false, defaultValue = "active") String statusFilter,
	        @RequestParam(value = "searchType", required = false) String searchType,
	        @RequestParam(value = "searchText", required = false) String searchText,
	        @RequestParam(value = "page", defaultValue = "0") int page,
	        Model model,
	        @AuthenticationPrincipal SecurityUser securityUser
	) {
	    // 1. 채팅방 목록 로드
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    User user = (User) authentication.getPrincipal();
	    String memId = user.getUsername();
	    List<ChatRoomDto> chatRoomList = chatRoomService.selectChatRoomList(memId);
	    model.addAttribute("chatRoomList", chatRoomList);

	    // 2. 회원 목록 로드
	    Pageable pageable = PageRequest.of(page, 10);
	    Page<Member> members;

	    if (searchText != null && !searchText.isEmpty()) {
	        Long currentUserDistributorNo = securityUser.getMember().getDistributor().getDistributorNo();
	        members = memberService.searchMembersByCriteria(searchType, searchText, statusFilter, currentUserDistributorNo, pageable);
	    } else {
	        // 검색어가 없을 경우 필터링만 적용
	        if ("mybranch".equals(statusFilter)) {
	            Long currentUserDistributorNo = securityUser.getMember().getDistributor().getDistributorNo();
	            members = memberService.findMembersByCurrentDistributor(currentUserDistributorNo, pageable);
	        } else if ("resigned".equals(statusFilter)) {
	            members = memberService.findAllByMemLeaveOrderByEmpNoAsc("Y", pageable);
	        } else if ("all".equals(statusFilter)) {
	            members = memberService.findAllOrderByEmpNoAsc(pageable);
	        } else {
	            members = memberService.findAllByMemLeaveOrderByEmpNoAsc("N", pageable);
	        }
	    }

	    // 페이지네이션 처리
	    int totalPages = members.getTotalPages();
	    int pageNumber = members.getNumber();
	    int pageGroupSize = 5;
	    int currentGroup = (pageNumber / pageGroupSize);
	    int startPage = currentGroup * pageGroupSize + 1;
	    int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

	    model.addAttribute("memberList", members.getContent());
	    model.addAttribute("page", members);
	    model.addAttribute("statusFilter", statusFilter);
	    model.addAttribute("searchType", searchType);
	    model.addAttribute("searchText", searchText);
	    model.addAttribute("totalPages", totalPages);
	    model.addAttribute("pageNumber", pageNumber);
	    model.addAttribute("startPage", startPage);
	    model.addAttribute("endPage", endPage);

	    return "chat/list"; // 두 리스트를 합쳐서 보여줄 페이지 템플릿
	}

	
	// 멤버서비스에 채팅있음
	@GetMapping("/chat/room/create")
	public String chatRoomList(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		
		String memId = user.getUsername();
		List<MemberDto> resultList = memberService.findAllForChat(memId);
		model.addAttribute("resultList",resultList);
		return "chat/list";
	}
}
