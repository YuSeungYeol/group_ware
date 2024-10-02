package com.ware.spring.drive.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ware.spring.drive.domain.Folder;
import com.ware.spring.member.domain.Member;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    
    // 부모 폴더에 의해 하위 폴더 찾기
    List<Folder> findByParentFolder(Folder parentFolder);

    // 폴더 이름으로 폴더 찾기
    List<Folder> findByFolderName(String folderName);

    // 특정 사용자의 폴더 찾기
    List<Folder> findByMember(Member member);

    // 폴더 이름으로 최상위 폴더 찾기 (부모 폴더가 없는 경우)
    @Query("SELECT f FROM Folder f WHERE f.folderName = :folderName AND f.parentFolder IS NULL")
    Optional<Folder> findRootFolderByName(@Param("folderName") String folderName);

    // 폴더 번호로 하위 폴더 조회
    List<Folder> findByParentFolder_FolderNo(Long parentFolderNo);
    
    // 최상위 폴더 조회
    List<Folder> findByParentFolderIsNull();
    
    // 폴더 번호로 폴더 찾기
    Folder findByfolderNo(Long folderNo);
    
  //  List<Folder> findByMemNoAndParentFolderNo(Long memNo, Long parentFolderNo);
    
    // 특정 사용자의 특정 부모 폴더 아래 폴더 조회
    List<Folder> findByMember_MemNoAndParentFolder_FolderNo(Long memNo, Long parentFolderNo);
    
    // 최상위 폴더일 경우
    List<Folder> findByMember_MemNoAndParentFolderIsNull(Long memNo);
   
    
    // 폴더 삭제 (delYn 업데이트)
    @Modifying
    @Query("UPDATE Folder f SET f.delYn = 'Y' WHERE f.folderNo = :folderNo")
    int updateDelYnByFolderNo(@Param("folderNo") Long folderNo);
}
