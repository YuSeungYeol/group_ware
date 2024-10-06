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

    Optional<VehicleDistributorSales> findByDistributorAndSaleDate(Distributor distributor, LocalDate saleDate);

    // 기존 메서드도 유지
    Optional<VehicleDistributorSales> findByDistributor_DistributorNoAndSaleDate(Long distributorNo, LocalDate saleDate);
    
    @Query("SELECT ds FROM VehicleDistributorSales ds WHERE YEAR(ds.saleDate) = :year AND MONTH(ds.saleDate) = :month")
    List<VehicleDistributorSales> findByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(ds.distributorSaleCount) FROM VehicleDistributorSales ds WHERE YEAR(ds.saleDate) = :year AND MONTH(ds.saleDate) = :month")
    Integer getTotalSaleCountByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(ds.distributorSalePrices) FROM VehicleDistributorSales ds WHERE YEAR(ds.saleDate) = :year AND MONTH(ds.saleDate) = :month")
    Integer getTotalSalePricesByMonth(@Param("year") int year, @Param("month") int month);
}