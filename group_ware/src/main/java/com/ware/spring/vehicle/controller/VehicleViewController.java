package com.ware.spring.vehicle.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ware.spring.member.service.DistributorService;
import com.ware.spring.vehicle.domain.VehicleDto;
import com.ware.spring.vehicle.repository.VehicleSalesRepository;
import com.ware.spring.vehicle.service.VehicleService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VehicleViewController {
	private final VehicleSalesRepository vehicleSalesRepository;
    private final VehicleService vehicleService;
    private final DistributorService distributorService;
    
    // 차량 목록 조회 (차량 리스트 페이지)
    @GetMapping("/vehicle/list")
    public String getVehicleList(Model model) {
        List<VehicleDto> vehicles = vehicleService.getAllVehicles();
        model.addAttribute("vehicles", vehicles);
        return "vehicle/vehicle_list"; 
    }
    
    
    @GetMapping("/vehicle/{vehicleNo}/detail")
    public String getVehicleDetail(@PathVariable("vehicleNo") Long vehicleNo, Model model) {
        VehicleDto vehicleDetail = vehicleService.getVehicleDetail(vehicleNo);
        int vehicleSalesCount = vehicleSalesRepository.sumSaleCountByVehicleNo(vehicleNo); // 판매량 합계 계산

        model.addAttribute("vehicle", vehicleDetail);
        model.addAttribute("vehicleSalesCount", vehicleSalesCount); // 판매량 추가

        return "vehicle/vehicle_detail"; 
    }

}
