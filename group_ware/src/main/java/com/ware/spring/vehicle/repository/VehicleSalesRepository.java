package com.ware.spring.vehicle.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ware.spring.vehicle.domain.VehicleSales;

public interface VehicleSalesRepository extends JpaRepository<VehicleSales, Long> {
    List<VehicleSales> findByMember_MemNo(Long memNo);

    @Query("SELECT SUM(vs.saleCount) FROM VehicleSales vs WHERE vs.vehicle.vehicleNo = :vehicleNo")
    Integer sumSaleCountByVehicleNo(@Param("vehicleNo") Long vehicleNo);
    @Query("SELECT vs FROM VehicleSales vs WHERE YEAR(vs.saleDate) = :year AND MONTH(vs.saleDate) = :month")
    List<VehicleSales> findSalesByMonth(@Param("year") int year, @Param("month") int month);
    List<VehicleSales> findByMember_MemNoAndSaleDateBetween(Long memNo, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(vs.saleCount) FROM VehicleSales vs WHERE YEAR(vs.saleDate) = :year AND MONTH(vs.saleDate) = :month")
    Integer sumSaleCountByMonth(@Param("year") int year, @Param("month") int month);


    @Query(value = "SELECT v.vehicle.vehicleNo, SUM(v.saleCount), SUM(v.salePrices) " +
            "FROM VehicleSales v " +
            "WHERE v.saleDate BETWEEN :startDate AND :endDate " +
            "GROUP BY v.vehicle.vehicleNo " +
            "ORDER BY SUM(v.saleCount) DESC")
	List<Object[]> findTop5VehiclesBySales(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
	@Query("SELECT v.member.memNo, SUM(v.saleCount), SUM(v.salePrices) " +
		       "FROM VehicleSales v " +
		       "GROUP BY v.member.memNo " +
		       "ORDER BY SUM(v.saleCount) DESC")
		List<Object[]> findIndividualSalesRanking();
	@Query("SELECT v.member, SUM(v.saleCount), SUM(v.salePrices) " +
		       "FROM VehicleSales v " +
		       "GROUP BY v.member " +
		       "ORDER BY SUM(v.salePrices) DESC")
		List<Object[]> findTop5MembersByRevenue();


}

