package com.ware.spring.vehicle.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ware.spring.vehicle.service.VehicleService;

@RestController
@RequestMapping("/api/vehicle")
public class VehicleApiController {

    @Autowired
    private VehicleService vehicleService;

    // 연도별 개인 판매량 및 매출액을 가져오는 API
    @GetMapping("/yearly")
    public Map<String, Integer> getYearlySalesData(@RequestParam("year") int year, @RequestParam("memNo") Long memNo) {
        return vehicleService.getYearlyIndividualSalesData(year, memNo);
    }

    // 월별 개인 판매량 및 매출액을 가져오는 API
    @GetMapping("/monthly")
    public Map<String, Integer> getMonthlySalesData(@RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("memNo") Long memNo) {
        return vehicleService.getMonthlyIndividualSalesData(year, month, memNo);
    }

}
