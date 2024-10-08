package com.ware.spring.vehicle.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.vehicle.domain.Vehicle;
import com.ware.spring.vehicle.domain.VehicleDistributorSales;
import com.ware.spring.vehicle.domain.VehicleDistributorSalesDto;
import com.ware.spring.vehicle.domain.VehicleDto;
import com.ware.spring.vehicle.domain.VehicleSales;
import com.ware.spring.vehicle.domain.VehicleSalesDto;
import com.ware.spring.vehicle.domain.VehicleSize;
import com.ware.spring.vehicle.repository.VehicleDistributorSalesRepository;
import com.ware.spring.vehicle.repository.VehicleRepository;
import com.ware.spring.vehicle.repository.VehicleSalesRepository;
import com.ware.spring.vehicle.repository.VehicleSizeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

	@Autowired
    private final VehicleRepository vehicleRepository;
    @Autowired
    private final VehicleSalesRepository vehicleSalesRepository;
    @Autowired
    private final VehicleDistributorSalesRepository vehicleDistributorSalesRepository;
    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final VehicleSizeRepository vehicleSizeRepository;
    public Vehicle saveVehicle(VehicleDto vehicleDto, String imagePath) {
        // VehicleSize 찾기
        VehicleSize vehicleSize = vehicleSizeRepository.findById(vehicleDto.getVehicleSize().getSizeNo())
                .orElseThrow(() -> new IllegalArgumentException("차량 크기를 찾을 수 없습니다."));

        // Vehicle 엔티티 생성 및 설정
        Vehicle vehicle = vehicleDto.toEntity();
        vehicle.setVehicleSize(vehicleSize);
        vehicle.setVehicleProfile(imagePath); // 이미지 경로 설정

        // Vehicle 저장
        return vehicleRepository.save(vehicle);
    }
    public List<VehicleDto> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleDto::toDto)
                .collect(Collectors.toList());
    }

    public VehicleDto getVehicleDetail(Long vehicleNo) {
        Vehicle vehicle = vehicleRepository.findById(vehicleNo)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleNo));

        return VehicleDto.toDto(vehicle);
    }

    @Transactional
    public void processSale(Long vehicleNo, int saleCount, Long memNo) {
        // 차량 조회
        Vehicle vehicle = vehicleRepository.findById(vehicleNo)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleNo));

        // 회원 조회
        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memNo));

        // 재고가 충분한지 확인
        if (vehicle.getVehicleInventory() < saleCount) {
            throw new IllegalArgumentException("Insufficient vehicle inventory");
        }

        // 재고 감소
        vehicle.setVehicleInventory(vehicle.getVehicleInventory() - saleCount);
        vehicleRepository.save(vehicle);

        // 개인 판매 내역 업데이트
        int totalSalePrice = vehicle.getVehiclePrice() * saleCount;
        VehicleSales vehicleSales = VehicleSales.builder()
                .vehicle(vehicle)
                .member(member)
                .saleCount(saleCount)
                .salePrices(totalSalePrice)
                .saleDate(LocalDate.now())
                .build();
        vehicleSalesRepository.save(vehicleSales);

        // 부서 판매 내역 업데이트
        Distributor distributor = member.getDistributor();
        LocalDate today = LocalDate.now();
        VehicleDistributorSales distributorSales = vehicleDistributorSalesRepository
                .findByDistributorAndSaleDate(distributor, today)
                .orElseGet(() -> VehicleDistributorSales.builder()
                        .distributor(distributor)
                        .distributorSaleCount(0)
                        .distributorSalePrices(0)
                        .saleDate(today)
                        .build());

        distributorSales.setDistributorSaleCount(distributorSales.getDistributorSaleCount() + saleCount);
        distributorSales.setDistributorSalePrices(distributorSales.getDistributorSalePrices() + totalSalePrice);

        vehicleDistributorSalesRepository.save(distributorSales);
    }

    public Map<String, Integer> getCurrentMonthSales(Long memNo) {
        LocalDate now = LocalDate.now();
        List<VehicleSales> salesList = vehicleSalesRepository.findByMember_MemNoAndSaleDateBetween(
                memNo,
                now.withDayOfMonth(1),
                now.withDayOfMonth(now.lengthOfMonth())
        );

        int totalSaleCount = 0;
        int totalSalePrice = 0;

        for (VehicleSales sale : salesList) {
            totalSaleCount += sale.getSaleCount();
            totalSalePrice += sale.getSalePrices();
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("totalSaleCount", totalSaleCount);
        result.put("totalSalePrices", totalSalePrice);
        return result;
    }

    public Map<String, Map<Integer, Integer>> getYearlyIndividualSalesData(int year, Long memNo) {
        List<VehicleSales> salesList = vehicleSalesRepository.findByMember_MemNoAndSaleDateBetween(
                memNo,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );

        Map<Integer, Integer> monthlySales = new HashMap<>();
        Map<Integer, Integer> monthlySalePrices = new HashMap<>();

        // 월별 집계 초기화
        for (int i = 1; i <= 12; i++) {
            monthlySales.put(i, 0);
            monthlySalePrices.put(i, 0);
        }

        // 판매량 및 매출액 집계
        for (VehicleSales sale : salesList) {
            int month = sale.getSaleDate().getMonthValue();
            monthlySales.put(month, monthlySales.get(month) + sale.getSaleCount());
            monthlySalePrices.put(month, monthlySalePrices.get(month) + sale.getSalePrices());
        }

        // 결과 반환
        Map<String, Map<Integer, Integer>> salesData = new HashMap<>();
        salesData.put("monthlySales", monthlySales);
        salesData.put("monthlySalePrices", monthlySalePrices);

        return salesData;
    }


    // 월별 개인 판매량 및 매출액 데이터 가져오기
    public Map<String, Integer> getMonthlyIndividualSalesData(int year, int month, Long memNo) {
        List<VehicleSales> salesList = vehicleSalesRepository.findByMember_MemNoAndSaleDateBetween(
                memNo,
                LocalDate.of(year, month, 1),
                LocalDate.of(year, month, LocalDate.of(year, month, 1).lengthOfMonth())
        );

        Map<String, Integer> salesData = new HashMap<>();
        int totalSalesCount = 0;
        int totalSalePrices = 0;

        for (VehicleSales sale : salesList) {
            totalSalesCount += sale.getSaleCount();
            totalSalePrices += sale.getSalePrices();
        }

        salesData.put("totalSalesCount", totalSalesCount);
        salesData.put("totalSalePrices", totalSalePrices);

        return salesData;
    }

    // 부서별 이번 달 판매량 및 매출액 가져오기
    public List<VehicleDistributorSales> getTop5DepartmentsByMonthlySales(int year, int month) {
        return vehicleDistributorSalesRepository.findTop5DepartmentsByMonth(year, month);
    }
    public List<VehicleDistributorSalesDto> getTop5DistributorsBySales(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 전체 부서별 판매 데이터를 가져옵니다.
        List<VehicleDistributorSales> salesList = vehicleDistributorSalesRepository.findBySaleDateBetween(startDate, endDate);

        // 부서별로 판매 대수와 매출액을 합산하여 Map으로 변환합니다.
        Map<Long, VehicleDistributorSalesDto> aggregatedSalesMap = new HashMap<>();
        for (VehicleDistributorSales sales : salesList) {
            Long distributorNo = sales.getDistributor().getDistributorNo();

            VehicleDistributorSalesDto existingDto = aggregatedSalesMap.getOrDefault(distributorNo,
                VehicleDistributorSalesDto.builder()
                    .distributor(sales.getDistributor())
                    .distributorSaleCount(0)
                    .distributorSalePrices(0)
                    .build()
            );

            existingDto.setDistributorSaleCount(existingDto.getDistributorSaleCount() + sales.getDistributorSaleCount());
            existingDto.setDistributorSalePrices(existingDto.getDistributorSalePrices() + sales.getDistributorSalePrices());

            aggregatedSalesMap.put(distributorNo, existingDto);
        }

        // Map의 값을 리스트로 변환하고 상위 5개의 부서를 반환합니다.
        return aggregatedSalesMap.values().stream()
                .sorted(Comparator.comparingInt(VehicleDistributorSalesDto::getDistributorSaleCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<VehicleSalesDto> getTop5VehiclesBySales(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = vehicleSalesRepository.findTop5VehiclesBySales(startDate, endDate);

        // Object[] 배열을 DTO로 변환
        return results.stream()
                .map(result -> {
                    Long vehicleNo = ((Number) result[0]).longValue();
                    int totalSaleCount = ((Number) result[1]).intValue();
                    int totalSalePrices = ((Number) result[2]).intValue();

                    VehicleDto vehicleDto = new VehicleDto();
                    vehicleDto.setVehicleNo(vehicleNo);

                    return VehicleSalesDto.builder()
                            .vehicle(vehicleDto)
                            .saleCount(totalSaleCount)
                            .salePrices(totalSalePrices)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
}
