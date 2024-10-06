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
public class VehicleSalesDto {

    private Long salesNo;
    private Distributor distributor; // Distributor 객체로 설정
    private VehicleDto vehicle; // VehicleDto 사용
    private Member member; // Member 객체로 설정
    private int saleCount;
    private int salePrices;
    private LocalDate saleDate;

    // 엔티티로 변환하는 메서드
    public VehicleSales toEntity() {
        return VehicleSales.builder()
                .salesNo(salesNo)
                .distributor(distributor) // Distributor 객체로 설정
                .vehicle(vehicle != null ? vehicle.toEntity() : null) // VehicleDto -> Vehicle
                .member(member) // Member 객체로 설정
                .saleCount(saleCount)
                .salePrices(salePrices)
                .saleDate(saleDate)
                .build();
    }

    // DTO로 변환하는 메서드
    public static VehicleSalesDto toDto(VehicleSales vehicleSales) {
        return VehicleSalesDto.builder()
                .salesNo(vehicleSales.getSalesNo())
                .distributor(vehicleSales.getDistributor()) // Distributor 객체 설정
                .vehicle(vehicleSales.getVehicle() != null ? VehicleDto.toDto(vehicleSales.getVehicle()) : null) // Vehicle -> VehicleDto
                .member(vehicleSales.getMember()) // Member 객체 설정
                .saleCount(vehicleSales.getSaleCount())
                .salePrices(vehicleSales.getSalePrices())
                .saleDate(vehicleSales.getSaleDate())
                .build();
    }
}
