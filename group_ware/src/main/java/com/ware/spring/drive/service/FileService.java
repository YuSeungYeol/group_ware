package com.ware.spring.drive.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.drive.domain.FileDto;
import com.ware.spring.drive.domain.Files;
import com.ware.spring.drive.domain.Folder;
import com.ware.spring.drive.repository.FileRepository;
import com.ware.spring.member.domain.Member;

import jakarta.transaction.Transactional;

@Service
public class FileService {
	
	private final FileRepository fileRepository;
	
	@Autowired
	public FileService(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}
	
	public String upload(MultipartFile file, Folder folder, Member member, boolean isPersonalDrive) {
	    String newFileName = null;
	    try {
	        String oriFileName = file.getOriginalFilename();
	        String fileExt = oriFileName.substring(oriFileName.lastIndexOf("."));
	        if (!isValidExtension(fileExt)) {
	            throw new IllegalArgumentException("허용되지 않은 파일 형식입니다.");
	        }

	        UUID uuid = UUID.randomUUID();
	        String uniqueName = uuid.toString().replaceAll("-", "") + fileExt;

	        // 개인 드라이브와 공용 드라이브 구분
	        String folderPath;
	        if (isPersonalDrive) {
	            folderPath = (folder != null) ? folder.getFolderUploadPath() : "C:/personal_drive/" + member.getMemNo();
	        } else {
	            folderPath = (folder != null) ? folder.getFolderUploadPath() : "C:/uploads";
	        }

	        File saveFile = new File(folderPath + File.separator + uniqueName);

	        if (!saveFile.exists()) {
	            saveFile.mkdirs();
	        }

	        file.transferTo(saveFile);

	        // 파일 정보 DB 저장
	        FileDto dto = FileDto.builder()
	            .folder(folder)
	            .file_ori_name(oriFileName)
	            .file_new_name(uniqueName)
	            .file_path(folderPath)
	            .del_yn("N")
	            .member(member)
	            .build();

	        fileRepository.save(dto.toEntity());
	        newFileName = uniqueName;

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return newFileName;
	}
	
	private boolean isValidExtension(String ext) {
		List<String> allowedExtensions = Arrays.asList(        ".jpg", ".jpeg", ".png", ".gif", // 이미지 파일
		        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".hwp", // 문서 파일
		        ".mp4", ".avi", ".mov", ".wmv", // 영상 파일
		        ".zip", ".rar", ".7z", // 압축 파일
		        ".mp3", ".wav"); // 오디오 파일);
		return allowedExtensions.contains(ext.toLowerCase());
	}
	
	public List<FileDto> getFilesByFolder(Long folderNo) {
	    List<Files> fileList;
	    if(folderNo == null || folderNo == 0) {
	    	System.out.println("Fetching files for the root folder...");
	    	fileList = fileRepository.findByFolderIsNullAndDelYn("N");
	    }else {
	    	
	    	fileList = fileRepository.findByFolderFolderNoAndDelYn(folderNo, "N");
	    }
		
	    System.out.println("Fetched files count: " + fileList.size());
	    // File 객체를 FileDto로 변환
	    List<FileDto> fileDtos = fileList.stream()
	            .map(files -> new FileDto().toDto(files))  // toDto 메서드로 변환
	            .collect(Collectors.toList());
	    
	    return fileDtos;
	}
	
	// 파일 번호로 파일 정보 가져오기
	public FileDto getFileByNo(Long fileNo) {
		// 파일을 데이터베이스에서 조회
		Optional<Files> optionalFileEntity = fileRepository.findById(fileNo);
		
		// 파일이 존재하지 않을 경우에 대한 처리
		Files fileEntity = optionalFileEntity.orElseThrow(() -> new RuntimeException("해당 파일이 존재하지 않습니다.")) ;
		
		//파일 엔티티를 DTO로 변환하여 반환
		return new FileDto().toDto(fileEntity);
				
	}
	@Transactional
	public int updateFile(FileDto dto) {
		int result = -1;
		try {
			// 기존 파일 데이터베이스에서 조회
			Optional<Files> optionalFile = fileRepository.findById(dto.getFile_no());
			
			if(optionalFile.isPresent()) {
				Files file = optionalFile.get();
				
				Files updatedFile = Files.builder()
						.fileNo(file.getFileNo())
						.fileOriName(file.getFileOriName())
						.fileNewName(file.getFileNewName())
						.filePath(file.getFilePath())
						.fileRegDate(file.getFileRegDate())
						.delYn("Y")
						.folder(file.getFolder())
						.build();
				
				fileRepository.save(updatedFile);
				result = 1;
			}else {
				result = -1;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public List<FileDto> getFilesByFolderAndMemberNo(Long folderNo, Long memNo) {
	    List<Files> files;

	    if (folderNo == null) {
	        // 최상위 폴더에 있는 파일들 조회
	    	files = fileRepository.findByFolderIsNullAndMember_MemNoAndDelYn(memNo, "N");
	    } else {
	        // 특정 폴더 안의 파일 조회
	    	files = fileRepository.findByFolder_FolderNoAndMember_MemNoAndDelYn(folderNo, memNo, "N");
	    }
	    
	 // 디버그 로그 추가
	    System.out.println("조회된 파일 개수: " + files.size());
	    
	    // File 엔티티를 FileDto로 변환
	    return files.stream()
	                .map(file -> new FileDto().toDto(file))
	                .collect(Collectors.toList());
	}
}
