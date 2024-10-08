package com.ware.spring.vehicle.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.vehicle.domain.VehicleDistributorSales;

public interface VehicleDistributorSalesRepository extends JpaRepository<VehicleDistributorSales, Long> {

    // 특정 부서의 특정 날짜에 대한 판매 데이터를 조회합니다.
    Optional<VehicleDistributorSales> findByDistributorAndSaleDate(Distributor distributor, LocalDate saleDate);

    @Query("SELECT vds FROM VehicleDistributorSales vds WHERE YEAR(vds.saleDate) = :year AND MONTH(vds.saleDate) = :month ORDER BY vds.distributorSalePrices DESC")
    List<VehicleDistributorSales> findTop5DepartmentsByMonth(@Param("year") int year, @Param("month") int month);

    List<VehicleDistributorSales> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT vds FROM VehicleDistributorSales vds WHERE vds.saleDate BETWEEN :startDate AND :endDate ORDER BY vds.distributorSalePrices DESC")
    List<VehicleDistributorSales> findTop5BySaleDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 특정 distributorNo의 모든 판매 기록을 가져오는 메서드
    List<VehicleDistributorSales> findByDistributor_DistributorNo(Long distributorNo);

    // 특정 부서의 특정 날짜 범위 내의 판매 데이터를 조회합니다.
    List<VehicleDistributorSales> findByDistributor_DistributorNoAndSaleDateBetween(
            Long distributorNo,
            LocalDate startDate,
            LocalDate endDate
    );
    @Query("SELECT SUM(vds.distributorSaleCount), SUM(vds.distributorSalePrices) " +
    	       "FROM VehicleDistributorSales vds " +
    	       "WHERE vds.distributor.distributorNo = :distributorNo " +
    	       "AND vds.saleDate BETWEEN :startDate AND :endDate")
    	Object[] findSalesCountAndRevenueByDistributorAndDateRange(@Param("distributorNo") Long distributorNo,
    	                                                           @Param("startDate") LocalDate startDate,
    	                                                           @Param("endDate") LocalDate endDate);
    Optional<VehicleDistributorSales> findByDistributor_DistributorNoAndSaleDate(Long distributorNo, LocalDate saleDate);

    @Query("SELECT ds FROM VehicleDistributorSales ds WHERE YEAR(ds.saleDate) = :year AND MONTH(ds.saleDate) = :month")
    List<VehicleDistributorSales> findByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(ds.distributorSaleCount) FROM VehicleDistributorSales ds WHERE YEAR(ds.saleDate) = :year AND MONTH(ds.saleDate) = :month")
    Integer getTotalSaleCountByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(ds.distributorSalePrices) FROM VehicleDistributorSales ds WHERE YEAR(ds.saleDate) = :year AND MONTH(ds.saleDate) = :month")
    Integer getTotalSalePricesByMonth(@Param("year") int year, @Param("month") int month);
}
