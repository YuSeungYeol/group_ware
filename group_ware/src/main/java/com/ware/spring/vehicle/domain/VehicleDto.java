package com.ware.spring.vehicle.domain;

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
public class VehicleDto {

    private Long vehicleNo;
    private VehicleSize vehicleSize;
    private String vehicleModel;
    private String vehicleReg;
    private int vehicleInventory;
    private String vehicleFuel;
    private String vehicleEfficiency;
    private String vehicleDisplacement;
    private int vehicleSales;
    private String vehicleStatus;
    private String vehicleProfile;
    private String vehicleRpm;
    private Integer vehiclePrice;

    public Vehicle toEntity() {
        return Vehicle.builder()
                .vehicleNo(vehicleNo)
                .vehicleSize(vehicleSize) // VehicleSize 객체 그대로 설정
                .vehicleModel(vehicleModel)
                .vehicleReg(vehicleReg)
                .vehicleInventory(vehicleInventory)
                .vehicleFuel(vehicleFuel)
                .vehicleEfficiency(vehicleEfficiency)
                .vehicleDisplacement(vehicleDisplacement)
                .vehicleSales(vehicleSales)
                .vehicleStatus(vehicleStatus)
                .vehicleProfile(vehicleProfile)
                .vehicleRpm(vehicleRpm)
                .vehiclePrice(vehiclePrice)
                .build();
    }

    public static VehicleDto toDto(Vehicle vehicle) {
        return VehicleDto.builder()
                .vehicleNo(vehicle.getVehicleNo())
                .vehicleSize(vehicle.getVehicleSize()) // VehicleSize 객체 그대로 설정
                .vehicleModel(vehicle.getVehicleModel())
                .vehicleReg(vehicle.getVehicleReg())
                .vehicleInventory(vehicle.getVehicleInventory())
                .vehicleFuel(vehicle.getVehicleFuel())
                .vehicleEfficiency(vehicle.getVehicleEfficiency())
                .vehicleDisplacement(vehicle.getVehicleDisplacement())
                .vehicleSales(vehicle.getVehicleSales())
                .vehicleStatus(vehicle.getVehicleStatus())
                .vehicleProfile(vehicle.getVehicleProfile())
                .vehicleRpm(vehicle.getVehicleRpm())
                .vehiclePrice(vehicle.getVehiclePrice())
                .build();
    }
}
