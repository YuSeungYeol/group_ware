package com.ware.spring.vehicle.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSizeDto {

    private Long sizeNo;
    private String vehicleSize;

    public VehicleSize toEntity() {
        return VehicleSize.builder()
                .sizeNo(sizeNo)
                .vehicleSize(vehicleSize)
                .build();
    }

    public static VehicleSizeDto toDto(VehicleSize vehicleSize) {
        return VehicleSizeDto.builder()
                .sizeNo(vehicleSize.getSizeNo())
                .vehicleSize(vehicleSize.getVehicleSize())
                .build();
    }
}
