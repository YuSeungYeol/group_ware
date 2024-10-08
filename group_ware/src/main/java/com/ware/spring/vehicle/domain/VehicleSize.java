package com.ware.spring.vehicle.domain;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_size")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_no")
    private Long sizeNo;

    @Column(name = "vehicle_size", nullable = false)
    private String vehicleSize;
}
