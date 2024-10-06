package com.ware.spring.vehicle.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.vehicle.domain.Vehicle;
import com.ware.spring.vehicle.domain.VehicleDistributorSales;
import com.ware.spring.vehicle.domain.VehicleDto;
import com.ware.spring.vehicle.domain.VehicleSales;
import com.ware.spring.vehicle.repository.VehicleDistributorSalesRepository;
import com.ware.spring.vehicle.repository.VehicleRepository;
import com.ware.spring.vehicle.repository.VehicleSalesRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleSalesRepository vehicleSalesRepository;
    private final VehicleDistributorSalesRepository vehicleDistributorSalesRepository;
    private final MemberRepository memberRepository;

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

        // 분배자 판매 내역 업데이트
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
    public Map<String, Integer> getYearlyIndividualSalesData(int year, Long memNo) {
        List<VehicleSales> salesList = vehicleSalesRepository.findByMember_MemNoAndSaleDateBetween(
                memNo,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
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
}