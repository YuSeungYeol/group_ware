package com.ware.spring.drive.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.drive.domain.FileDto;
import com.ware.spring.drive.domain.Folder;
import com.ware.spring.drive.domain.FolderDto;
import com.ware.spring.drive.service.FileService;
import com.ware.spring.drive.service.FolderService;
import com.ware.spring.member.domain.Member;
import com.ware.spring.security.vo.SecurityUser;

@Controller
public class FolderApiController {

    private final FolderService folderService;
    private final FileService fileService;
    
    @Autowired
    public FolderApiController(FolderService folderService, FileService fileService) {
        this.folderService = folderService;
        this.fileService = fileService;
    }
    
    @ResponseBody
    @PostMapping("/folder/uploadFile")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file, 
                                          @RequestParam(value = "folderNo", required = false) Long folderNo,
                                          @RequestParam(value = "isPersonalDrive", required = true) boolean isPersonalDrive,
                                          @AuthenticationPrincipal SecurityUser securityUser) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "파일 업로드 중 오류가 발생하였습니다.");

        Member member = null;
        if (isPersonalDrive) {
            member = securityUser.getMember();  // 개인 드라이브일 때만 member 가져오기
        }

        FolderDto folderDto = null;

        if (folderNo != null && folderNo != 0) {
            folderDto = folderService.selecetFolderOne(folderNo);
            if (folderDto == null) {
                resultMap.put("res_msg", "해당 폴더를 찾을 수 없습니다.");
                return resultMap;
            }
        }

        Folder folder = (folderDto != null) ? folderDto.toEntity() : null;

        // 파일 업로드 서비스 호출
        String newFileName = fileService.upload(file, folder, member, isPersonalDrive);

        if (newFileName != null) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "파일 업로드가 성공하였습니다.");
        } else {
            resultMap.put("res_msg", "파일 업로드가 실패하였습니다.");
        }

        return resultMap;
    }
    
    @ResponseBody
    @PostMapping("/folder/updateDelYn")
    public Map<String, String> updateDelYn(@RequestBody Map<String, List<Long>> request) {
        Map<String, String> resultMap = new HashMap<>();
        // 폴더 번호 가져오기
        List<Long> folderNos = request.get("folderNos");
        // 파일 번호 가져오기
        List<Long> fileNos = request.get("fileNos");

        // 폴더 삭제 처리
        if (folderNos != null && !folderNos.isEmpty()) {
            for (Long folderNo : folderNos) {
                try {
                    // 재귀적으로 하위 폴더 및 파일들도 삭제 처리
                    folderService.updateFolderAndFilesDelYn(folderNo, "Y");
                    resultMap.put("res_code", "200");
                    resultMap.put("res_msg", "폴더 삭제가 성공하였습니다.");
                } catch (Exception e) {
                    resultMap.put("res_code", "404");
                    resultMap.put("res_msg", "일부 폴더 삭제가 실패하였습니다.");
                    break;  // 하나라도 실패하면 중지하고 오류 메시지를 반환합니다.
                }
            }
        }

        // 파일 삭제 처리
        if (fileNos != null && !fileNos.isEmpty()) {
            for (Long fileNo : fileNos) {
                FileDto fileDto = new FileDto();
                fileDto.setFile_no(fileNo);
                fileDto.setDel_yn("Y");

                int result = fileService.updateFile(fileDto);
                if (result > 0) {
                    resultMap.put("res_code", "200");
                    resultMap.put("res_msg", "파일 삭제가 성공하였습니다.");
                } else {
                    resultMap.put("res_code", "404");
                    resultMap.put("res_msg", "일부 파일 삭제가 실패하였습니다.");
                    break;
                }
            }
        }

        return resultMap;
    }
    
    
    
    @ResponseBody
    @PostMapping("/folder/create")
    public Map<String, String> createFolder(@RequestBody FolderDto dto, @RequestParam("parentFolderNo") Long parentFolderNo) {
        Map<String, String> resultMap = new HashMap<>();
        System.out.println("Received parentFolderNo: " + parentFolderNo); // 이 줄을 추가
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "폴더 생성이 실패하였습니다.");

        if (parentFolderNo != null && parentFolderNo > 0) {
            FolderDto parentFolder = folderService.selectFolderById(parentFolderNo);
            if (parentFolder != null) {
            	dto.setParentFolder(parentFolder.toEntity());
                System.out.println("Set Parent Folder ID in DTO: " + dto.getParentFolder());
                // 하위 폴더 경로 설정
                String folderPath = parentFolder.getFolder_upload_path() + "/" + dto.getFolder_name();
                dto.setFolder_upload_path(folderPath);
            }
        }else {
        	// 최상위 폴더의 경우
        	String folderPath = "C:/uploads/" + dto.getFolder_name();
        	dto.setFolder_upload_path(folderPath);
        }

        if (folderService.createFolder(dto) > 0) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "폴더 생성이 성공하였습니다.");
        }

        return resultMap;
    }
    
    @ResponseBody
    @GetMapping("/folder/apiList")
    public Map<String, Object> getFolderContents(@RequestParam("parentFolderNo") Long parentFolderNo){
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	if (parentFolderNo == 0) {
            parentFolderNo = null;
        }
    	// 폴더 리스트 조회
    	List<FolderDto> folderDtos = folderService.getSubFoldersAndFiles(parentFolderNo);
    	resultMap.put("folders", folderDtos);
    	
    	// 파일 리스트 조회
    	List<FileDto> fileDtos = fileService.getFilesByFolder(parentFolderNo);
    	resultMap.put("files", fileDtos);
    	
    	return resultMap;
    }
    
    
    @ResponseBody
    @GetMapping("/folder/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileNo") Long fileNo){
    	try {
    	FileDto dto = fileService.getFileByNo(fileNo);
    	Path filePath = Paths.get(dto.getFile_path(), dto.getFile_new_name());
    	
    	Resource resource = new FileSystemResource(filePath);
    	
    	// 파일명 utf-8로 인코딩
    	String encodedFileName = URLEncoder.encode(dto.getFile_ori_name(), StandardCharsets.UTF_8.toString()).replace("+","%20");
    	
    	return ResponseEntity.ok()
    			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
    			.body(resource);
	    }catch(UnsupportedEncodingException e) {
	    	e.printStackTrace();
	    	return ResponseEntity.status(500).build();
	    }
    }
    
    @ResponseBody
    @GetMapping("/personal-drive/apiList")
    public Map<String, Object> getPersonalFolderContents(@RequestParam("parentFolderNo") Long parentFolderNo, 
    													@AuthenticationPrincipal SecurityUser securityUser){
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	Member member = securityUser.getMember();
    	
    	if (parentFolderNo == 0) {
    		parentFolderNo = null;
    	}
    	// 개인 리스트 조회
    	List<FolderDto> folderDtos = folderService.getFoldersByMemberNoAndParentFolderNo(member.getMemNo(),parentFolderNo);
    	
    	resultMap.put("folders", folderDtos);
    	
    	// 개인 파일 리스트 조회
    	List<FileDto> fileDtos = fileService.getFilesByFolderAndMemberNo(parentFolderNo, member.getMemNo(), true);
    	resultMap.put("files", fileDtos);
    	
    	return resultMap;
    }
    
    @ResponseBody
    @PostMapping("/personal-drive/uploadFile")
    public Map<String, String> uploadPersonalFile(@RequestParam("file") MultipartFile file, 
                                                 @RequestParam(value = "folderNo", required = false) Long folderNo,
                                                 @AuthenticationPrincipal SecurityUser securityUser) {

        Member member = securityUser.getMember();
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "파일 업로드 중 오류가 발생하였습니다.");

        FolderDto folderDto = null;

        if (folderNo != null && folderNo != 0) {
            folderDto = folderService.selectFolderById(folderNo);
            if (folderDto == null) {
                resultMap.put("res_msg", "해당 폴더를 찾을 수 없습니다.");
                return resultMap;
            }
        }

        // 개인 폴더 업로드(member 필요)
        Folder folder = (folderDto != null) ? folderDto.toEntity() : null;
        boolean isPersonalDrive = true; // 개인 드라이브에서 호출되었으므로 true
        String newFileName = fileService.upload(file, folder, member, isPersonalDrive);

        if (newFileName != null) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "파일 업로드가 성공하였습니다.");
        } else {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "파일 업로드가 실패하였습니다.");
        }
        return resultMap;
    }
    
    @ResponseBody
    @PostMapping("/personal-drive/create")
    public Map<String, String> createPersonalFolder(@RequestBody FolderDto dto, @RequestParam("parentFolderNo") Long parentFolderNo, 
    												@AuthenticationPrincipal SecurityUser securityUser) {
    	
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "폴더 생성이 실패하였습니다.");
        
        Member member = securityUser.getMember();
        dto.setMem_no(member.getMemNo());
        
        if (parentFolderNo != null && parentFolderNo > 0) {
            FolderDto parentFolder = folderService.selectFolderById(parentFolderNo);
            if (parentFolder != null) {
            	dto.setParentFolder(parentFolder.toEntity());
                System.out.println("Set Parent Folder ID in DTO: " + dto.getParentFolder());
                // 하위 폴더 경로 설정
                String folderPath = parentFolder.getFolder_upload_path() + "/" + dto.getFolder_name();
                dto.setFolder_upload_path(folderPath);
            }
        }else {
        	// 최상위 폴더의 경우
        	String folderPath = "C:/personal_drive/" + dto.getFolder_name();
        	dto.setFolder_upload_path(folderPath);
        }

        if (folderService.createPersonalFolder(dto, member.getMemNo()) > 0) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "폴더 생성이 성공하였습니다.");
        }

        return resultMap;
    }
}

