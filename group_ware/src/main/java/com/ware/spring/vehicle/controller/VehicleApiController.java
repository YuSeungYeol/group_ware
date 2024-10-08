package com.ware.spring.vehicle.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.security.vo.SecurityUser;
import com.ware.spring.vehicle.domain.Vehicle;
import com.ware.spring.vehicle.domain.VehicleDistributorSales;
import com.ware.spring.vehicle.domain.VehicleDistributorSalesDto;
import com.ware.spring.vehicle.domain.VehicleDto;
import com.ware.spring.vehicle.domain.VehicleSalesDto;
import com.ware.spring.vehicle.domain.VehicleSize;
import com.ware.spring.vehicle.repository.VehicleDistributorSalesRepository;
import com.ware.spring.vehicle.repository.VehicleRepository;
import com.ware.spring.vehicle.repository.VehicleSalesRepository;
import com.ware.spring.vehicle.repository.VehicleSizeRepository;
import com.ware.spring.vehicle.service.VehicleService;

@RestController
@RequestMapping("/api/vehicle")
public class VehicleApiController {

    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private VehicleSalesRepository vehicleSalesRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private VehicleSizeRepository vehicleSizeRepository;
    @Autowired
    private VehicleDistributorSalesRepository vehicleDistributorSalesRepository;

    // 개인별 판매량 순위를 가져오는 API - 매출액 기준 상위 5명 반환

 


    // 연도별 개인 판매량 및 매출액을 가져오는 API
    @GetMapping("/yearly")
    public Map<String, Map<Integer, Integer>> getYearlySalesData(@RequestParam("year") int year, @RequestParam("memNo") Long memNo) {
        return vehicleService.getYearlyIndividualSalesData(year, memNo);
    }

    // 월별 개인 판매량 및 매출액을 가져오는 API
    @GetMapping("/monthly")
    public Map<String, Integer> getMonthlySalesData(@RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("memNo") Long memNo) {
        return vehicleService.getMonthlyIndividualSalesData(year, month, memNo);
    }

    // 부서별 상위 5개 판매량 및 매출액을 가져오는 API
    @GetMapping("/top-distributors")
    public List<VehicleDistributorSalesDto> getTopDistributorsBySales(@RequestParam("year") int year, @RequestParam("month") int month) {
        return vehicleService.getTop5DistributorsBySales(year, month);
    }

    // 상위 5개 판매된 차량 정보를 가져오는 API (차량 모델로)
    @GetMapping("/top-vehicles")
    public List<VehicleSalesDto> getTop5VehiclesBySales(@RequestParam("year") int year, @RequestParam("month") int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Fetch data from repository
        List<Object[]> results = vehicleSalesRepository.findTop5VehiclesBySales(startDate, endDate);

        // Convert the result to DTO list
        return results.stream().map(result -> {
            Long vehicleNo = ((Number) result[0]).longValue();
            int saleCount = ((Number) result[1]).intValue();
            int salePrices = ((Number) result[2]).intValue();

            // Fetch vehicle model by vehicleNo
            Vehicle vehicle = vehicleRepository.findById(vehicleNo).orElse(null);
            String vehicleModel = (vehicle != null) ? vehicle.getVehicleModel() : "Unknown Model";

            VehicleDto vehicleDto = VehicleDto.builder()
                    .vehicleNo(vehicleNo)
                    .vehicleModel(vehicleModel)
                    .build();

            return VehicleSalesDto.builder()
                    .vehicle(vehicleDto)
                    .saleCount(saleCount)
                    .salePrices(salePrices)
                    .build();
        }).collect(Collectors.toList());
    }

    // 부서별 월별 판매량 및 매출액을 반환하는 API
 // 부서별 월별 판매량 및 매출액을 반환하는 API
    @GetMapping("/distributor-sales")
    public Map<String, Object> getDepartmentSales() {
        // 현재 로그인된 사용자의 부서 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        Long distributorNo = userDetails.getMember().getDistributor().getDistributorNo();
        String distributorName = userDetails.getMember().getDistributor().getDistributorName(); // 지점 이름 가져오기

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        List<VehicleDistributorSales> salesList = vehicleDistributorSalesRepository.findByDistributor_DistributorNoAndSaleDateBetween(
                distributorNo,
                LocalDate.of(currentYear, currentMonth, 1),
                LocalDate.of(currentYear, currentMonth, LocalDate.of(currentYear, currentMonth, 1).lengthOfMonth())
        );

        int totalSaleCount = 0;
        int totalSalePrices = 0;

        for (VehicleDistributorSales sales : salesList) {
            totalSaleCount += sales.getDistributorSaleCount();
            totalSalePrices += sales.getDistributorSalePrices();
        }

        // 지점 이름과 판매 데이터 함께 반환
        Map<String, Object> salesData = new HashMap<>();
        salesData.put("distributorName", distributorName);
        salesData.put("salesCount", totalSaleCount);
        salesData.put("salesRevenue", totalSalePrices);

        return salesData;
    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerVehicle(
            @ModelAttribute VehicleDto vehicleDto, // DTO를 @ModelAttribute로 처리
            @RequestParam("vehicleImage") MultipartFile file,
            @RequestParam("size_no") Long sizeNo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // VehicleSize 객체 찾기
            VehicleSize vehicleSize = vehicleSizeRepository.findById(sizeNo)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sizeNo: " + sizeNo));
            
            // VehicleDto에 VehicleSize 설정
            vehicleDto.setVehicleSize(vehicleSize);
            
            // 이미지 저장 경로 설정 및 저장
            if (file != null && !file.isEmpty()) {
                String uploadDir = new File("src/main/resources/static/image/vehicles/").getAbsolutePath();
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs(); // 경로가 없으면 생성
                }
                String filePath = uploadDir + "/" + file.getOriginalFilename();
                File dest = new File(filePath);
                file.transferTo(dest);

                // 이미지 경로를 vehicleDto에 설정
                vehicleDto.setVehicleProfile("/image/vehicles/" + file.getOriginalFilename());
            }

            // vehicleDto를 통해 vehicle 저장
            vehicleService.saveVehicle(vehicleDto, vehicleDto.getVehicleProfile());

            // 성공 응답 설정
            response.put("success", true);
            response.put("res_msg", "차량이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            // 예외 처리: 이미지 저장 중 오류
            e.printStackTrace();
            response.put("success", false);
            response.put("res_msg", "차량 등록 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            // 기타 예외 처리
            e.printStackTrace();
            response.put("success", false);
            response.put("res_msg", "차량 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

