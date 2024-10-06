package com.ware.spring.vehicle.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_no")
    private Long vehicleNo;

    @ManyToOne
    @JoinColumn(name = "size_no", nullable = false)
    private VehicleSize vehicleSize;

    @Column(name = "vehicle_model", nullable = false)
    private String vehicleModel;

    @Column(name = "vehicle_reg", nullable = false)
    private String vehicleReg;

    @Column(name = "vehicle_inventory", nullable = false)
    private int vehicleInventory;

    @Column(name = "vehicle_fuel", nullable = false)
    private String vehicleFuel;

    @Column(name = "vehicle_efficiency", nullable = false)
    private String vehicleEfficiency;

    @Column(name = "vehicle_displacement", nullable = false)
    private String vehicleDisplacement;

    @Column(name = "vehicle_sales", nullable = false)
    private int vehicleSales;

    @Column(name = "vehicle_status", nullable = false)
    private String vehicleStatus;

    @Column(name = "vehicle_profile", nullable = false)
    private String vehicleProfile;

    @Column(name = "vehicle_rpm", nullable = false)
    private String vehicleRpm;

    @Column(name = "vehicle_price")
    private Integer vehiclePrice;
    
}
