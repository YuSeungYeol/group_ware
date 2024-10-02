package com.ware.spring.drive.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.ware.spring.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Files {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="file_no")
    private Long fileNo;
    
	@ManyToOne
	@JoinColumn(name = "folder_no", nullable = true)
	private Folder folder;
	
    @Column(name="file_reg_date")
    @CreationTimestamp
    private LocalDateTime fileRegDate;
    
    @ManyToOne
    @JoinColumn(name = "mem_no", nullable = true)
    private Member member;
    
    @Column(name="file_ori_name")
    private String fileOriName;
    
    @Column(name="file_new_name")
    private String fileNewName;
    
    @Column(name="file_path")
    private String filePath;
    
    @Column(name="del_yn")
    private String delYn = "N";
}
