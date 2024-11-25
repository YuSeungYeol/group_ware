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

   
    Optional<Member> findByMemId(String memId);
    boolean existsByMemId(String memId);
    
    // 부서별 회원 조회
    List<Member> findByDistributor_DistributorNo(Long distributorNo);

    // 조직도 관련
    @Query("SELECT m FROM Member m WHERE m.distributor.distributorNo = :distributorNo")
    List<Member> findMembersByDistributorNo(@Param("distributorNo") Long distributorNo);

    // 수정 관련
    @Modifying
    @Query("UPDATE Member m SET m.memPw = :memPw WHERE m.memNo = :memNo")
    void updatePassword(@Param("memNo") int memNo, @Param("memPw") String memPw);

    @Modifying
    @Query("UPDATE Member m SET m.memName = :memName, m.memPhone = :memPhone, m.memEmail = :memEmail WHERE m.memNo = :memNo")
    void updateMemberInfo(@Param("memNo") int memNo, @Param("memName") String memName, 
                          @Param("memPhone") String memPhone, @Param("memEmail") String memEmail);

    @Modifying
    @Query("UPDATE Member m SET m.profileSaved = :profileSaved WHERE m.memNo = :memNo")
    void updateProfilePicture(@Param("memNo") int memNo, @Param("profileSaved") String profileSaved);

    // mem_leave에 따른 필터링
    @EntityGraph(attributePaths = {"rank", "distributor"})
    Page<Member> findAllByMemLeaveOrderByEmpNoAsc(String memLeave, Pageable pageable);
    // 모든 상태의 사원번호 순 조회
    Page<Member> findAllByOrderByEmpNoAsc(Pageable pageable);
    // 검색어에 따른 필터링 (재직/퇴사 여부 상관없이)
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
    // 지점별 필터링
    Page<Member> findByDistributor_DistributorNo(Long distributorNo, Pageable pageable);
    @Query("SELECT m FROM Member m WHERE m.distributor.distributorNo = :distributorNo AND m.memLeave = 'N'")
    Page<Member> findMembersByDistributorNo(@Param("distributorNo") Long distributorNo, Pageable pageable);
    // 검색어와 함께 지점별 필터링
    @Query("SELECT m FROM Member m WHERE m.distributor.distributorNo = :distributorNo AND m.memLeave = :memLeave AND " +
           "(m.memName LIKE %:searchText% OR m.rank.rankName LIKE %:searchText% OR " +
           "CAST(m.memRegDate AS string) LIKE %:searchText% OR m.distributor.distributorName LIKE %:searchText% OR " +
           "m.empNo LIKE %:searchText%)")
    Page<Member> findByDistributor_DistributorNoAndMemLeaveAndSearchText(
            @Param("distributorNo") Long distributorNo,
            @Param("memLeave") String memLeave,
            @Param("searchText") String searchText,
            Pageable pageable);

    // 검색어 없이 지점과 상태별 필터링
    @Query("SELECT m FROM Member m WHERE m.distributor.distributorNo = :distributorNo AND m.memLeave = :memLeave")
    Page<Member> findByDistributor_DistributorNoAndMemLeave(
            @Param("distributorNo") Long distributorNo,
            @Param("memLeave") String memLeave,
            Pageable pageable);
    
    //근태
    @Query(value="SELECT m "
			+ "FROM Member m "
			+ "WHERE m.memId != ?1 "
			+ "AND m.memName != ?1 "
			+ "AND(m.memId NOT IN (SELECT cr.fromId "
			+ "FROM ChatRoom cr "
			+ "WHERE cr.toId = ?1 )) "
			+ "AND(m.memId NOT IN (SELECT cr.toId "
			+ "FROM ChatRoom cr "
			+ "WHERE cr.fromId = ?1 ))")
	List<Member> findAllForChat(String mem_id);
    
    Optional<Member> findByMemNo(Long memNo);
	Optional<Member> findByMemName(String memName);
}
