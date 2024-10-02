package com.ware.spring.member.repository;

import com.ware.spring.member.domain.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankRepository extends JpaRepository<Rank, Long> {

    @Query("SELECT r.rankName FROM Rank r WHERE r.rankNo = :rankNo")
    String findRankNameByRankNo(@Param("rankNo") Long rankNo);  // rank_no로 rank_name 조회
}
