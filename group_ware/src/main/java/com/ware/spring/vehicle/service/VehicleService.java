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

    /**
     * 차량 정보를 저장합니다.
     * 설명: VehicleDto와 이미지 경로를 받아 차량 크기를 설정한 후 차량을 저장합니다.
     * 
     * @param vehicleDto 저장할 차량의 정보가 담긴 DTO
     * @param imagePath 차량 이미지 경로
     * @return 저장된 Vehicle 객체
     */
    public Vehicle saveVehicle(VehicleDto vehicleDto, String imagePath) {
        VehicleSize vehicleSize = vehicleSizeRepository.findById(vehicleDto.getVehicleSize().getSizeNo())
                .orElseThrow(() -> new IllegalArgumentException("차량 크기를 찾을 수 없습니다."));

        Vehicle vehicle = vehicleDto.toEntity();
        vehicle.setVehicleSize(vehicleSize);
        vehicle.setVehicleProfile(imagePath);

        return vehicleRepository.save(vehicle);
    }

    /**
     * 모든 차량 목록을 반환합니다.
     * 설명: 모든 차량 정보를 DTO로 변환하여 반환합니다.
     * 
     * @return 모든 VehicleDto 리스트
     */
    public List<VehicleDto> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleDto::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 차량의 상세 정보를 조회합니다.
     * 설명: 차량 번호로 특정 차량의 상세 정보를 조회하고, DTO로 반환합니다.
     * 
     * @param vehicleNo 차량 번호
     * @return VehicleDto 차량 정보가 담긴 DTO
     */
    public VehicleDto getVehicleDetail(Long vehicleNo) {
        Vehicle vehicle = vehicleRepository.findById(vehicleNo)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleNo));
        return VehicleDto.toDto(vehicle);
    }

    /**
     * 차량 판매를 처리합니다.
     * 설명: 차량 재고를 감소시키고 판매 내역을 기록합니다. 또한, 부서별 판매 내역을 업데이트합니다.
     * 
     * @param vehicleNo 판매된 차량 번호
     * @param saleCount 판매 수량
     * @param memNo 판매자 회원 번호
     */
    @Transactional
    public void processSale(Long vehicleNo, int saleCount, Long memNo) {
        Vehicle vehicle = vehicleRepository.findById(vehicleNo)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleNo));

        Member member = memberRepository.findById(memNo)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memNo));

        if (vehicle.getVehicleInventory() < saleCount) {
            throw new IllegalArgumentException("Insufficient vehicle inventory");
        }

        vehicle.setVehicleInventory(vehicle.getVehicleInventory() - saleCount);
        vehicleRepository.save(vehicle);

        int totalSalePrice = vehicle.getVehiclePrice() * saleCount;
        VehicleSales vehicleSales = VehicleSales.builder()
                .vehicle(vehicle)
                .member(member)
                .saleCount(saleCount)
                .salePrices(totalSalePrice)
                .saleDate(LocalDate.now())
                .build();
        vehicleSalesRepository.save(vehicleSales);

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

    /**
     * 현재 달의 개인 판매량 및 매출액을 조회합니다.
     * 설명: 주어진 회원 번호에 대한 현재 달의 판매 내역을 조회하여 판매량과 매출액을 반환합니다.
     * 
     * @param memNo 회원 번호
     * @return 개인 판매량 및 매출액 정보가 담긴 Map
     */
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

    /**
     * 연간 개인 판매 데이터를 조회합니다.
     * 설명: 주어진 회원 번호와 연도에 대한 월별 판매량 및 매출액을 반환합니다.
     * 
     * @param year 조회할 연도
     * @param memNo 회원 번호
     * @return 월별 판매량 및 매출액 정보를 담은 Map
     */
    public Map<String, Map<Integer, Integer>> getYearlyIndividualSalesData(int year, Long memNo) {
        List<VehicleSales> salesList = vehicleSalesRepository.findByMember_MemNoAndSaleDateBetween(
                memNo,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );

        Map<Integer, Integer> monthlySales = new HashMap<>();
        Map<Integer, Integer> monthlySalePrices = new HashMap<>();

        for (int i = 1; i <= 12; i++) {
            monthlySales.put(i, 0);
            monthlySalePrices.put(i, 0);
        }

        for (VehicleSales sale : salesList) {
            int month = sale.getSaleDate().getMonthValue();
            monthlySales.put(month, monthlySales.get(month) + sale.getSaleCount());
            monthlySalePrices.put(month, monthlySalePrices.get(month) + sale.getSalePrices());
        }

        Map<String, Map<Integer, Integer>> salesData = new HashMap<>();
        salesData.put("monthlySales", monthlySales);
        salesData.put("monthlySalePrices", monthlySalePrices);

        return salesData;
    }

    /**
     * 월간 개인 판매 데이터를 조회합니다.
     * 설명: 주어진 회원 번호와 연, 월에 대한 개인 판매량 및 매출액을 반환합니다.
     * 
     * @param year 연도
     * @param month 월
     * @param memNo 회원 번호
     * @return 개인 판매량 및 매출액 정보가 담긴 Map
     */
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

    /**
     * 부서별 월간 판매 상위 5개를 조회합니다.
     * 설명: 특정 연도와 월에 대한 부서별 월간 판매 상위 5개를 반환합니다.
     * 
     * @param year 연도
     * @param month 월
     * @return 상위 5개 부서의 VehicleDistributorSales 리스트
     */
    public List<VehicleDistributorSales> getTop5DepartmentsByMonthlySales(int year, int month) {
        return vehicleDistributorSalesRepository.findTop5DepartmentsByMonth(year, month);
    }

    /**
     * 월간 상위 5개 부서를 조회합니다.
     * 설명: 특정 연도와 월에 대한 상위 5개 부서의 판매 대수와 매출액을 반환합니다.
     * 
     * @param year 연도
     * @param month 월
     * @return VehicleDistributorSalesDto 상위 5개 부서의 판매 데이터
     */
    public List<VehicleDistributorSalesDto> getTop5DistributorsBySales(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<VehicleDistributorSales> salesList = vehicleDistributorSalesRepository.findBySaleDateBetween(startDate, endDate);

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

        return aggregatedSalesMap.values().stream()
                .sorted(Comparator.comparingInt(VehicleDistributorSalesDto::getDistributorSaleCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 특정 기간 동안 판매된 상위 5개 차량을 조회합니다.
     * 설명: 특정 기간 동안 판매된 차량을 판매 대수와 매출액 기준으로 상위 5개를 반환합니다.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return VehicleSalesDto 상위 5개 차량의 판매 데이터
     */
    public List<VehicleSalesDto> getTop5VehiclesBySales(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = vehicleSalesRepository.findTop5VehiclesBySales(startDate, endDate);

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
