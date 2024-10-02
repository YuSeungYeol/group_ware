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
@Table(name="folder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="folder_no")
    private Long folderNo;
    
    @Column(name="folder_name")
    private String folderName;
    
    @Column(name="folder_reg_date")
    @CreationTimestamp
    private LocalDateTime folderRegDate;
    
    @ManyToOne
    @JoinColumn(name = "parents_folder_id")
    private Folder parentFolder;
    
    @ManyToOne
    @JoinColumn(name = "mem_no", nullable = true)
    private Member member;

    
    @Column(name="folder_upload_path")
    private String folderUploadPath;
   
    
    @Column(name="del_yn")
    private String delYn = "N";
    
}
