package com.ware.spring.drive.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.ware.spring.member.domain.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class FolderDto {
	
	private Long folder_no;
	private String folder_name;
	private LocalDateTime folder_reg_date;

	private Long mem_no;
	private String folder_upload_path;
	private String del_yn;
	
	private List<FolderDto> subFolders;
	private Member member;  // Member 필드 추가
	private Folder parentFolder;
	
    public Folder toEntity() {

        return Folder.builder()
                .folderNo(folder_no)
                .folderName(folder_name)
                .folderRegDate(folder_reg_date)
                .folderUploadPath(folder_upload_path)
                .member(member)  // member 필드 추가
                .delYn(del_yn)
                .parentFolder(parentFolder)
                .build();
    }
	
    public FolderDto toDto(Folder folder) {
    	
    	
        return FolderDto.builder()
        			.folder_no(folder.getFolderNo())
        			.folder_name(folder.getFolderName())
        			.folder_reg_date(folder.getFolderRegDate())
        			.folder_upload_path(folder.getFolderUploadPath())
        			.del_yn(folder.getDelYn())
        			.member(folder.getMember())
        			.parentFolder(folder.getParentFolder())
        			.build();
    }
    
}