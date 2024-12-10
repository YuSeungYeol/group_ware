package com.ware.spring.member.repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ware.spring.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 멤버 ID로 조회
    Optional<Member> findByMemId(String memId);
    boolean existsByMemId(String memId);

    // 비밀번호 변경 필요 MyPage 관련
    @Query("SELECT m.memPw FROM Member m WHERE m.memNo = :memNo")
    String findPasswordById(@Param("memNo") Long memNo);

    // 지점별 회원 조회
    List<Member> findByDistributor_DistributorNo(Long distributorNo);

    // 조직도 관련
    @Query("SELECT m FROM Member m WHERE m.distributor.distributorNo = :distributorNo")
    List<Member> findMembersByDistributorNo(@Param("distributorNo") Long distributorNo);

    // 프로필 사진 업데이트
    @Modifying
    @Query("UPDATE Member m SET m.profileSaved = :profileSaved WHERE m.memNo = :memNo")
    void updateProfilePicture(@Param("memNo") int memNo, @Param("profileSaved") String profileSaved);

    // mem_leave에 따른 필터링 (퇴사 여부)
    @EntityGraph(attributePaths = {"rank", "distributor"})
    Page<Member> findAllByMemLeaveOrderByEmpNoAsc(String memLeave, Pageable pageable);
    @Query("SELECT m FROM Member m " +
    	       "WHERE m.distributor.distributorNo = :distributorNo AND m.memLeave = :memLeave AND " +
    	       "(m.memName LIKE %:searchText% OR m.rank.rankName LIKE %:searchText% OR " +
    	       " CAST(m.memRegDate AS string) LIKE %:searchText% OR m.distributor.distributorName LIKE %:searchText% OR " +
    	       " m.empNo LIKE %:searchText%)")
    	Page<Member> findByDistributor_DistributorNoAndMemLeaveAndSearchText(
    	        @Param("distributorNo") Long distributorNo,
    	        @Param("memLeave") String memLeave,
    	        @Param("searchText") String searchText,
    	        Pageable pageable);
    // 모든 상태의 회원을 사번 오름차순으로 조회
    Page<Member> findAllByOrderByEmpNoAsc(Pageable pageable);

    // 특정 지점에 속한 회원 조회
    Page<Member> findByDistributor_DistributorNo(Long distributorNo, Pageable pageable);

    // 검색 조건과 memLeave에 따른 필터링 (동적 쿼리)
    @Query("SELECT m FROM Member m WHERE " +
           "(m.memLeave = :memLeave OR :memLeave IS NULL) AND " +
           "(CASE WHEN :searchType = 'name' THEN m.memName " +
           "      WHEN :searchType = 'rank' THEN m.rank.rankName " +
           "      WHEN :searchType = 'hireDate' THEN CAST(m.memRegDate AS string) " +
           "      WHEN :searchType = 'branch' THEN m.distributor.distributorName " +
           "      WHEN :searchType = 'empNo' THEN m.empNo " +
           " ELSE NULL END) LIKE CONCAT('%', :searchText, '%')")
    Page<Member> findBySearchTextAndMemLeave(
            @Param("searchText") String searchText,
            @Param("searchType") String searchType,
            @Param("memLeave") String memLeave,
            Pageable pageable);
    // 검색 조건 없이 특정 지점과 memLeave로 필터링
    @Query("SELECT m FROM Member m WHERE m.distributor.distributorNo = :distributorNo AND m.memLeave = :memLeave")
    Page<Member> findByDistributor_DistributorNoAndMemLeave(
            @Param("distributorNo") Long distributorNo,
            @Param("memLeave") String memLeave,
            Pageable pageable);

    // 근태 (채팅과 관련된 필터링)
    @Query(value = "SELECT m FROM Member m " +
                   "WHERE m.memId != ?1 " +
                   "AND m.memName != ?1 " +
                   "AND (m.memId NOT IN (SELECT cr.fromId FROM ChatRoom cr WHERE cr.toId = ?1)) " +
                   "AND (m.memId NOT IN (SELECT cr.toId FROM ChatRoom cr WHERE cr.fromId = ?1))")
    List<Member> findAllForChat(String memId);

    // 개별 검색 필터링 (필요에 따라 유지)
    Page<Member> findByMemNameContaining(String name, Pageable pageable);
    Page<Member> findByMemEmailContaining(String email, Pageable pageable);
    Page<Member> findByRankRankNameContaining(String rank, Pageable pageable);
    Page<Member> findByMemRegDate(LocalDate memRegDate, Pageable pageable);
    Page<Member> findByDistributorDistributorNameContaining(String distributorName, Pageable pageable);
    Page<Member> findByEmpNoContaining(String empNo, Pageable pageable);

    // 검색어와 memLeave에 따른 필터링
    Page<Member> findByMemNameContainingAndMemLeave(String name, String memLeave, Pageable pageable);
    Page<Member> findByRankRankNameContainingAndMemLeave(String rank, String memLeave, Pageable pageable);
    Page<Member> findByMemRegDateAndMemLeave(LocalDate memRegDate, String memLeave, Pageable pageable);
    Page<Member> findByDistributorDistributorNameContainingAndMemLeave(String distributorName, String memLeave, Pageable pageable);
    Page<Member> findByEmpNoContainingAndMemLeave(String empNo, String memLeave, Pageable pageable);

    // 회원 번호로 회원 조회
    Optional<Member> findByMemNo(Long memNo);
    Optional<Member> findByMemName(String memName);

    @Query("SELECT m FROM Member m WHERE LOWER(m.rank.rankName) LIKE LOWER(CONCAT('%', :rankName, '%')) AND " +
           "(:memLeave IS NULL OR m.memLeave = :memLeave)")
    Page<Member> findByRank(@Param("rankName") String rankName,
                            @Param("memLeave") String memLeave,
                            Pageable pageable);
    @Query("SELECT m FROM Member m JOIN m.distributor d WHERE LOWER(d.distributorName) LIKE LOWER(CONCAT('%', :distributorName, '%')) AND " +
           "(:memLeave IS NULL OR m.memLeave = :memLeave)")
    Page<Member> findByDistributor(@Param("distributorName") String distributorName,
                                   @Param("memLeave") String memLeave,
                                   Pageable pageable);
    @Query("SELECT m FROM Member m WHERE CAST(m.empNo AS string) LIKE CONCAT('%', :empNo, '%') AND " +
           "(:memLeave IS NULL OR m.memLeave = :memLeave)")
    Page<Member> findByEmpNo(@Param("empNo") String empNo,
                             @Param("memLeave") String memLeave,
                             Pageable pageable);
    @Query("SELECT m FROM Member m JOIN m.distributor d " +
    	       "WHERE (:searchText IS NULL OR LOWER(m.memName) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
    	       "ORDER BY LOWER(d.distributorName) ASC")
    	Page<Member> findAllWithDistributorNameSorted(
    	        @Param("searchText") String searchText,
    	        Pageable pageable);
    @Query("SELECT m FROM Member m JOIN m.distributor d " +
    	       "WHERE (:searchText IS NULL OR LOWER(m.memName) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
    	       "ORDER BY LOWER(d.distributorName) ASC")
    	Page<Member> findAllWithDistributorNameAsc(
    	        @Param("searchText") String searchText,
    	        Pageable pageable);

    	@Query("SELECT m FROM Member m JOIN m.distributor d " +
    	       "WHERE (:searchText IS NULL OR LOWER(m.memName) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
    	       "ORDER BY LOWER(d.distributorName) DESC")
    	Page<Member> findAllWithDistributorNameDesc(
    	        @Param("searchText") String searchText,
    	        Pageable pageable);
}
