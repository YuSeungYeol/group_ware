package com.ware.spring.vehicle.domain;

import java.time.LocalDate;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDistributorSalesDto {

    private Long distributorSalesNo; // Primary key of distributor sales
    private Distributor distributor; // Distributor 객체로 변경
    private Member member; // Member 객체로 변경
    private Integer distributorSaleCount; // Total number of sales for the distributor
    private Integer distributorSalePrices; // Total sales price for the distributor
    private LocalDate saleDate;

    // 엔티티로 변환하는 메서드
    public VehicleDistributorSales toEntity() {
        return VehicleDistributorSales.builder()
                .distributorSalesNo(distributorSalesNo)
                .distributor(distributor) // Distributor 객체 설정
                .member(member) // Member 객체 설정
                .distributorSaleCount(distributorSaleCount)
                .distributorSalePrices(distributorSalePrices)
                .saleDate(saleDate)
                .build();
    }

    // DTO로 변환하는 메서드
    public static VehicleDistributorSalesDto toDto(VehicleDistributorSales distributorSales) {
        return VehicleDistributorSalesDto.builder()
                .distributorSalesNo(distributorSales.getDistributorSalesNo())
                .distributor(distributorSales.getDistributor()) // Distributor 객체 설정
                .member(distributorSales.getMember()) // Member 객체 설정
                .distributorSaleCount(distributorSales.getDistributorSaleCount())
                .distributorSalePrices(distributorSales.getDistributorSalePrices())
                .saleDate(distributorSales.getSaleDate())
                .build();
    }
}
