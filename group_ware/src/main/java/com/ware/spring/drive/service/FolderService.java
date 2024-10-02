package com.ware.spring.drive.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ware.spring.drive.domain.FileDto;
import com.ware.spring.drive.domain.Files;
import com.ware.spring.drive.domain.Folder;
import com.ware.spring.drive.domain.FolderDto;
import com.ware.spring.drive.repository.FileRepository;
import com.ware.spring.drive.repository.FolderRepository;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;

import jakarta.transaction.Transactional;

@Service
public class FolderService {

	
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public FolderService(FolderRepository folderRepository ,MemberRepository memberRepository
    			, FileRepository fileRepository) {
        this.folderRepository = folderRepository;
        this.memberRepository = memberRepository;
        this.fileRepository = fileRepository;
    }
    
    @Transactional
    public void updateFolderAndFilesDelYn(Long folderNo, String delYn) {
    	  System.out.println("Method updateFolderAndFilesDelYn called with folderNo: " + folderNo + ", delYn: " + delYn);
        // 폴더 업데이트
        folderRepository.updateDelYnByFolderNo(folderNo);
        
        // 해당 폴더의 파일들 업데이트
        List<Files> files = fileRepository.findByFolderFolderNoAndDelYn(folderNo, "N");
        for (Files file : files) {
            FileDto fileDto = new FileDto().toDto(file);
            fileDto.setDel_yn(delYn);
            Files updateFile = fileDto.toEntity();
            fileRepository.save(updateFile);
        }
    }
    
    @Transactional
    public int updateFolder(FolderDto dto) {
        int result = -1;
        try {
            // 데이터베이스에서 기존 엔티티를 찾음
            Folder folder = folderRepository.findByfolderNo(dto.getFolder_no());
            if (folder != null) {
                // DTO의 값을 사용하여 새로운 엔티티 객체 생성
            	 Folder updatedFolder = Folder.builder()
                         .folderNo(folder.getFolderNo())  // 기존 ID 유지
                         .folderName(folder.getFolderName())  // 기존의 이름 유지
                         .folderRegDate(folder.getFolderRegDate())  // 기존의 등록 날짜 유지
                         .folderUploadPath(folder.getFolderUploadPath())  // 기존의 업로드 경로 유지
                         .member(folder.getMember())  // 기존의 회원 정보 유지
                         .parentFolder(folder.getParentFolder())  // 기존의 부모 폴더 유지
                         .delYn("Y")  // 삭제 여부만 업데이트
                         .build();
            	 
                // 변경된 엔티티 저장
                folderRepository.save(updatedFolder);
                result = 1;
            } else {
                result = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    @Transactional
    public int updateDelYn(List<Long> folderNos) {
        int result = 0;
        for (Long folderNo : folderNos) {
            result += folderRepository.updateDelYnByFolderNo(folderNo);
        }
        return result;
    }
    
    public FolderDto selectFolderOne(Long folder_no) {
    	Folder folder = folderRepository.findByfolderNo(folder_no);
    	FolderDto dto = FolderDto.builder()
    			.folder_no(folder.getFolderNo())
    			.del_yn(folder.getDelYn())
    			.build();
    	return dto;
    }
    
    public FolderDto selectFolderById(Long folderNo) {
        Folder folder = folderRepository.findById(folderNo).orElse(null);
        return folder != null ? new FolderDto().toDto(folder) : null;
    }
    
    public FolderDto selecetFolderOne(Long folder_no) {
    	Folder folder = folderRepository.findByfolderNo(folder_no);
    	FolderDto dto = FolderDto.builder()
    			.folder_no(folder.getFolderNo())
    			.folder_name(folder.getFolderName())
    			.folder_reg_date(folder.getFolderRegDate())
    			.folder_upload_path(folder.getFolderUploadPath())
    			.build();
    	return dto;
    }
    
    @Transactional
    public int createFolder(FolderDto dto) {
        int result = -1;
        try {
            // 기본 경로 설정
            String baseFolderPath = "C:/uploads"; // 저장할 폴더 경로 설정
            String parentFolderPath = baseFolderPath;

            // 부모 폴더 경로를 가져옴
            if (dto.getParentFolder() != null) {
                Folder parentFolder = dto.getParentFolder();  // 부모 폴더 객체 가져오기
                parentFolderPath = parentFolder.getFolderUploadPath();
                System.out.println("부모 폴더: " + parentFolderPath);
            }

            // 최종경로 = 부모 폴더 경로 + 새 폴더 이름
            String folderPath = parentFolderPath + "/" + dto.getFolder_name();
            System.out.println("최종 폴더 경로: " + folderPath);

            File folderCr = new File(folderPath);

            if (!folderCr.exists()) {
                boolean folderCreated = folderCr.mkdirs();
                if (!folderCreated) {
                    return -1; // 폴더 생성 실패
                }
            }

            // Member 객체와 Parent Folder 객체 조회
            dto.setMem_no(999L); // 임의의 mem_no 값 설정
            dto.setDel_yn("N");
            dto.setFolder_upload_path(folderPath);  // 폴더 경로 설정

            // 데이터베이스에 폴더 정보 저장
            Folder folder = dto.toEntity();  // toEntity 메서드에서 parentFolder도 설정됨
            folderRepository.save(folder);

            result = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public List<FolderDto> getSubFoldersAndFiles(Long parentFolderNo) {
        return findSubFolders(parentFolderNo); // 현재는 하위 폴더만 반환
    }
    
    public List<FolderDto> findSubFolders(Long parentFolderNo) {
        // Repository에서 올바른 메소드를 호출합니다.
        List<Folder> folders = folderRepository.findByParentFolder_FolderNo(parentFolderNo);
        
        // 폴더 목록을 DTO로 변환합니다.
        List<FolderDto> folderDtos = folders.stream()
                .map(folder -> new FolderDto().toDto(folder))
                .collect(Collectors.toList());
        
        return folderDtos;
    }
    
    // 폴더 목록 조회 서비스 예시 (옵션)
    public List<FolderDto> listFolders() {
        List<Folder> folders = folderRepository.findAll();
        
        if (folders == null || folders.isEmpty()) {
            System.out.println("No folders found in the database.");
        } else {
            System.out.println("Folders retrieved: " + folders.size());
        }

        List<FolderDto> folderDtos = new ArrayList<>();
        for (Folder fd : folders) {
            FolderDto dto = new FolderDto().toDto(fd);
            folderDtos.add(dto);
        }
        return folderDtos;
    }

    public List<Folder> getPersonalFolders(Member member){
    	return folderRepository.findByMember(member);
    }

    @Transactional
    public int createPersonalFolder(FolderDto dto, Long memNo) {
    	int result = -1;
    	try {
    		// 사용자 개인 드라이브 경로
    		String baseFolderPath = "C:/personal_drive/" + memNo;
    		String parentFolderPath = baseFolderPath;
    		
    		// 부모 폴더 경로 가져오기
    		if(dto.getParentFolder() != null) {
    			Folder parentFolder = dto.getParentFolder(); // 부모 폴더 가져오기
    			parentFolderPath = parentFolder.getFolderUploadPath();
    		}
    		
    		// 최종경로 = 부모 폴더 경로 + 새 폴더 이름
    		String folderPath = parentFolderPath + "/" + dto.getFolder_name();
    		File folderCr = new File(folderPath);
    		
    		if(!folderCr.exists()) {
    			boolean folderCreated = folderCr.mkdirs();
    			if(!folderCreated) {
    				return -1;
    			}
    		}
    		
    		// Member 조회 folder 설정
    		Member member = memberRepository.findById(memNo).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    		dto.setMember(member);
    		dto.setFolder_upload_path(folderPath);
    		dto.setDel_yn("N");
    		
    		// 엔티티 저장
    		Folder folder = dto.toEntity();
    		folderRepository.save(folder);
    		result = 1;
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return result;
    }
    
    public List<FolderDto> getFoldersByMemberNoAndParentFolderNo(Long memNo, Long parentFolderNo){
    	List<Folder> folders = folderRepository.findByMember_MemNoAndParentFolder_FolderNo(memNo, parentFolderNo);
    	List<FolderDto> folderDtos = folders.stream()
    			.map(folder -> new FolderDto().toDto(folder))
    			.collect(Collectors.toList());
    	return folderDtos;
    }
    
    public List<FolderDto> listPersonalFolders(Member member, Long parentFolderNo){
    	List<Folder> folders = folderRepository.findByMember_MemNoAndParentFolder_FolderNo(member.getMemNo(), parentFolderNo);
    	
    	List<FolderDto> folderDtos = folders.stream()
    			.map(folder -> new FolderDto().toDto(folder))
    			.collect(Collectors.toList());
    	return folderDtos;
    }
    
//    public List<Folder> getFoldersByMemberNoAndParentFolderNo(Long memNo, Long parentFolderNo){
//    	return folderRepository.findByMemNoAndParentFolderNo(memNo, parentFolderNo);
//    }
    
}
