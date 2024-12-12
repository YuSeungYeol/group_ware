package com.ware.spring.vehicle.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ware.spring.vehicle.domain.VehicleDto;
import com.ware.spring.vehicle.repository.VehicleSalesRepository;
import com.ware.spring.vehicle.repository.VehicleSizeRepository;
import com.ware.spring.vehicle.service.VehicleService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VehicleViewController {

    private final VehicleSalesRepository vehicleSalesRepository;
    private final VehicleService vehicleService;
    private final VehicleSizeRepository vehicleSizeRepository;
    private static final Logger logger = LoggerFactory.getLogger(VehicleViewController.class);

    /**
     * 차량 등록 페이지를 보여줍니다.
     * 설명: 차량 등록에 필요한 차량 크기 목록을 모델에 추가하여 차량 등록 페이지로 이동합니다.
     * 
     * @param model 뷰에 전달할 데이터
     * @return 차량 등록 페이지 뷰 (vehicle/vehicle_register)
     */
    @GetMapping("/vehicle/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("vehicleSize", vehicleSizeRepository.findAll());
        return "vehicle/vehicle_register";
    }

    /**
     * 차량 목록을 조회합니다.
     * 설명: 모든 차량 목록을 모델에 추가하여 차량 리스트 페이지로 이동합니다.
     * 
     * @param model 뷰에 전달할 데이터
     * @return 차량 리스트 페이지 뷰 (vehicle/vehicle_list)
     */
    @GetMapping("/vehicle/list")
    public String getVehicleList(Model model) {
        List<VehicleDto> vehicles = vehicleService.getAllVehicles();
        model.addAttribute("vehicles", vehicles);
        return "vehicle/vehicle_list";
    }

    /**
     * 특정 차량의 상세 정보를 조회합니다.
     * 설명: 차량 번호를 기반으로 특정 차량의 상세 정보를 조회하고, 차량의 총 판매량을 계산하여 모델에 추가합니다.
     * 
     * @param vehicleNo 조회할 차량 번호
     * @param model 뷰에 전달할 데이터
     * @return 차량 상세 정보 페이지 뷰 (vehicle/vehicle_detail)
     */
    @GetMapping("/vehicle/{vehicleNo}/detail")
    public String getVehicleDetail(@PathVariable("vehicleNo") Long vehicleNo, Model model) {
        VehicleDto vehicleDetail = vehicleService.getVehicleDetail(vehicleNo);
        Integer vehicleSalesCount = vehicleSalesRepository.sumSaleCountByVehicleNo(vehicleNo);
        if (vehicleSalesCount == null) {
            vehicleSalesCount = 0;
        }
        model.addAttribute("vehicle", vehicleDetail);
        model.addAttribute("vehicleSalesCount", vehicleSalesCount);
        return "vehicle/vehicle_detail";
    }
}
