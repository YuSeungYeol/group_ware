package com.ware.spring.drive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.drive.domain.Folder;
import com.ware.spring.drive.service.FolderService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.security.vo.SecurityUser;

@Controller
public class PersonalDriveController {

	private final FolderService folderService;
	
	@Autowired
	public PersonalDriveController(FolderService folderService) {
		this.folderService = folderService;
	}
	
	// 개인 드라이브 폴더 목록 조회
	@GetMapping("/folder/personal-drive")
    public String getPersonalDrive(Authentication authentication, Model model) {
		// SecurityUser 캐스팅
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        
        // SecurityUser에서 Member 추출
        Member currentUser = securityUser.getMember();
        List<Folder> personalFolders = folderService.getPersonalFolders(currentUser);
        model.addAttribute("folders", personalFolders);
        return "folder/personal-drive"; // personalDrive.html 뷰로 렌더링
    }
}
