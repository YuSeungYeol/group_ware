package com.ware.spring.drive.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ware.spring.drive.domain.Files;
import com.ware.spring.drive.domain.Folder;

import jakarta.transaction.Transactional;

public interface FileRepository extends JpaRepository<Files, Long>{
	
	List<Files> findByFolder(Folder folder);
	
	List<Files> findByFolderFolderNoAndDelYn(Long folderNo, String delYn);
	
	Optional<Files> findById(Long fileNo);
	
	List<Files> findByFolderIsNullAndDelYn(String delYn);
	
	// 특정 폴더 안에 있는 특정 사용자 파일 조회
	List<Files> findByFolder_FolderNoAndMember_MemNoAndDelYn(Long folderNo, Long memNo, String delYn);
	
	// 최상위 폴더에 있는 파일 조회
	List<Files> findByFolderIsNullAndMember_MemNoAndDelYn(Long memNo, String delYn);
	@Modifying
	@Transactional
	@Query("UPDATE Files f SET f.delYn = :delYn WHERE f.folder.folderNo = :folderNo")
	void updateDelYnByFolderNo(@Param("folderNo") Long folderNo, @Param("delYn") String delYn);
	
	List<Files> findByFolderIsNullAndMemberIsNullAndDelYn(String delYn);
	
	List<Files> findByFolder_FolderNoAndMemberIsNullAndDelYn(Long folderNo, String delYn);
}
