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

    /**
     * 연간 개인 판매량 및 매출액을 조회합니다.
     * 설명: 특정 회원의 연간 개인 판매량 및 매출액 데이터를 월별로 집계하여 반환합니다.
     * 
     * @param year 조회할 연도
     * @param memNo 회원 번호
     * @return 월별 판매량 및 매출액 정보가 담긴 Map
     */
    @GetMapping("/yearly")
    public Map<String, Map<Integer, Integer>> getYearlySalesData(@RequestParam("year") int year, @RequestParam("memNo") Long memNo) {
        return vehicleService.getYearlyIndividualSalesData(year, memNo);
    }

    /**
     * 월간 개인 판매량 및 매출액을 조회합니다.
     * 설명: 특정 회원의 월간 개인 판매량 및 매출액 데이터를 반환합니다.
     * 
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param memNo 회원 번호
     * @return 월간 판매량 및 매출액 정보가 담긴 Map
     */
    @GetMapping("/monthly")
    public Map<String, Integer> getMonthlySalesData(@RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("memNo") Long memNo) {
        return vehicleService.getMonthlyIndividualSalesData(year, month, memNo);
    }

    /**
     * 상위 5개 부서별 판매 데이터를 조회합니다.
     * 설명: 주어진 연도와 월에 대한 상위 5개 부서의 판매량 및 매출액을 반환합니다.
     * 
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 상위 5개 부서의 판매 데이터가 담긴 VehicleDistributorSalesDto 리스트
     */
    @GetMapping("/top-distributors")
    public List<VehicleDistributorSalesDto> getTopDistributorsBySales(@RequestParam("year") int year, @RequestParam("month") int month) {
        return vehicleService.getTop5DistributorsBySales(year, month);
    }

    /**
     * 상위 5개 차량 판매 데이터를 조회합니다.
     * 설명: 주어진 연도와 월에 대한 상위 5개 차량의 판매량 및 매출액을 반환합니다.
     * 
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 상위 5개 차량의 판매 데이터가 담긴 VehicleSalesDto 리스트
     */
    @GetMapping("/top-vehicles")
    public List<VehicleSalesDto> getTop5VehiclesBySales(@RequestParam("year") int year, @RequestParam("month") int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Object[]> results = vehicleSalesRepository.findTop5VehiclesBySales(startDate, endDate);

        return results.stream().map(result -> {
            Long vehicleNo = ((Number) result[0]).longValue();
            int saleCount = ((Number) result[1]).intValue();
            int salePrices = ((Number) result[2]).intValue();

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

    /**
     * 부서의 월간 판매 데이터를 조회합니다.
     * 설명: 현재 로그인된 사용자의 부서에 대한 월간 판매량 및 매출액을 반환합니다.
     * 
     * @return 부서의 판매 데이터와 지점 이름이 담긴 Map
     */
    @GetMapping("/distributor-sales")
    public Map<String, Object> getDepartmentSales() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        Long distributorNo = userDetails.getMember().getDistributor().getDistributorNo();
        String distributorName = userDetails.getMember().getDistributor().getDistributorName();

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

        Map<String, Object> salesData = new HashMap<>();
        salesData.put("distributorName", distributorName);
        salesData.put("salesCount", totalSaleCount);
        salesData.put("salesRevenue", totalSalePrices);

        return salesData;
    }

    /**
     * 새로운 차량을 등록합니다.
     * 설명: 차량 정보와 이미지 파일을 받아서 차량을 저장하고, 이미지 파일은 지정된 경로에 저장합니다.
     * 
     * @param vehicleDto 차량 정보가 담긴 DTO
     * @param file 차량 이미지 파일
     * @param sizeNo 차량 크기 ID
     * @return 등록 성공 여부와 메시지를 담은 응답
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerVehicle(
            @ModelAttribute VehicleDto vehicleDto,
            @RequestParam("vehicleImage") MultipartFile file,
            @RequestParam("size_no") Long sizeNo) {     
        Map<String, Object> response = new HashMap<>();   
        try {
            VehicleSize vehicleSize = vehicleSizeRepository.findById(sizeNo)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sizeNo: " + sizeNo));
            vehicleDto.setVehicleSize(vehicleSize);          
            if (file != null && !file.isEmpty()) {
                String uploadDir = new File("src/main/resources/static/image/vehicles/").getAbsolutePath();
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }	
                String filePath = uploadDir + "/" + file.getOriginalFilename();
                File dest = new File(filePath);
                file.transferTo(dest);

                vehicleDto.setVehicleProfile("/image/vehicles/" + file.getOriginalFilename());
            }
            vehicleService.saveVehicle(vehicleDto, vehicleDto.getVehicleProfile());
            response.put("success", true);
            response.put("res_msg", "차량이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("res_msg", "차량 등록 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("res_msg", "차량 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
