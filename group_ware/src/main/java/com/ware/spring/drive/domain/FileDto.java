package com.ware.spring.drive.domain;

import java.time.LocalDateTime;

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
public class FileDto {

	private Long file_no;
	private Folder folder;
	private LocalDateTime file_reg_date; 
	private String file_ori_name;
	private String file_new_name;
	private String file_path;
	private String del_yn;
	private Member member;
	
	public void updateDelYn(String newDelYn) {
		this.del_yn = newDelYn;
	}
	
	public Files toEntity() {
		
		return Files.builder()
				.fileNo(file_no)
				.folder(folder)
				.fileRegDate(file_reg_date)
				.fileOriName(file_ori_name)
				.fileNewName(file_new_name)
				.filePath(file_path)
				.delYn(del_yn)
				.member(member)
				.build();
	}
	
	public FileDto toDto(Files files) {
		
		return FileDto.builder()
				.file_no(files.getFileNo())
				.folder(files.getFolder())
				.file_reg_date(files.getFileRegDate())
				.file_new_name(files.getFileNewName())
				.file_ori_name(files.getFileOriName())
				.file_path(files.getFilePath())
				.del_yn(files.getDelYn())
				.member(files.getMember())
				.build();
	}
	
}   
