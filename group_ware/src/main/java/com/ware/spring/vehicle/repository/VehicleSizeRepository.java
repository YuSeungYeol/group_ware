package com.ware.spring.vehicle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.vehicle.domain.VehicleSize;

public interface VehicleSizeRepository extends JpaRepository<VehicleSize, Long> {

}
