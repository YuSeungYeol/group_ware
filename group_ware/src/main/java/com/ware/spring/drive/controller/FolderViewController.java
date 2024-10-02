package com.ware.spring.drive.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ware.spring.drive.service.FolderService;

@Controller
public class FolderViewController {

    private final FolderService folderService;

    @Autowired
    public FolderViewController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping("/folder/create")
    public String createFolderPage() {
        return "folder/create"; // 폴더 생성 폼을 보여주는 뷰 템플릿 경로
    }
    
    @GetMapping("/folder/list")
    public String listFolderPage(Model model) {
    	model.addAttribute("resultList", folderService.listFolders());
    	return "folder/list";
    }
}
