package com.ware.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.security.vo.SecurityUser;
import com.ware.spring.vehicle.domain.VehicleDistributorSales;
import com.ware.spring.vehicle.domain.VehicleSales;
import com.ware.spring.vehicle.repository.VehicleDistributorSalesRepository;
import com.ware.spring.vehicle.repository.VehicleSalesRepository;

@Controller
public class HomeController {

    private final VehicleSalesRepository vehicleSalesRepository;
    private final VehicleDistributorSalesRepository vehicleDistributorSalesRepository;

    public HomeController(VehicleSalesRepository vehicleSalesRepository, VehicleDistributorSalesRepository vehicleDistributorSalesRepository) {
        this.vehicleSalesRepository = vehicleSalesRepository;
        this.vehicleDistributorSalesRepository = vehicleDistributorSalesRepository;
    }

    @GetMapping({"/", ""})
    public String getHome(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 사용자가 인증되지 않은 경우
            System.out.println("사용자가 인증되지 않았습니다. 로그인 페이지로 리디렉션합니다.");
            return "redirect:/login";
        }

        SecurityUser userDetails;
        try {
            userDetails = (SecurityUser) authentication.getPrincipal();
        } catch (ClassCastException e) {
            System.out.println("사용자 정보를 가져올 수 없습니다. 예외: " + e.getMessage());
            return "redirect:/login";
        }

        if (userDetails == null || userDetails.getMember() == null) {
            System.out.println("사용자 정보가 올바르지 않습니다. 로그인 페이지로 리디렉션합니다.");
            return "redirect:/login";
        }

        Long memNo = userDetails.getMember().getMemNo();
        Long distributorNo = userDetails.getMember().getDistributor().getDistributorNo(); // 사용자가 속한 지점의 번호 가져오기

        if (memNo == null || distributorNo == null) {
            System.out.println("memNo 또는 distributorNo 값이 null입니다. 로그인 페이지로 리디렉션합니다.");
            return "redirect:/login";
        }

        // 개인 월별 판매량 및 매출액 집계
        List<VehicleSales> salesList = vehicleSalesRepository.findByMember_MemNo(memNo);
        Map<Integer, Integer> monthlyIndividualSales = new HashMap<>();
        Map<Integer, Integer> monthlyIndividualSalePrices = new HashMap<>();

        // 월별 집계 초기화
        for (int i = 1; i <= 12; i++) {
            monthlyIndividualSales.put(i, 0);
            monthlyIndividualSalePrices.put(i, 0);
        }

        // 판매량 및 매출액 집계
        for (VehicleSales sale : salesList) {
            int month = sale.getSaleDate().getMonthValue();
            monthlyIndividualSales.put(month, monthlyIndividualSales.get(month) + sale.getSaleCount());
            monthlyIndividualSalePrices.put(month, monthlyIndividualSalePrices.get(month) + sale.getSalePrices());
        }

        // 부서별 상위 5개 판매량 및 매출액 집계
        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        List<VehicleDistributorSales> topDistributors = vehicleDistributorSalesRepository.findTop5BySaleDateBetween(
                java.time.LocalDate.of(currentYear, currentMonth, 1),
                java.time.LocalDate.of(currentYear, currentMonth, java.time.YearMonth.of(currentYear, currentMonth).lengthOfMonth())
        );

        // 현재 사용자가 속한 지점의 월별 판매량 및 매출액 집계
        List<VehicleDistributorSales> distributorSalesList = vehicleDistributorSalesRepository.findByDistributor_DistributorNo(distributorNo);
        Map<Integer, Integer> monthlyDistributorSales = new HashMap<>();
        Map<Integer, Integer> monthlyDistributorSalesPrices = new HashMap<>();

        // 월별 집계 초기화
        for (int i = 1; i <= 12; i++) {
            monthlyDistributorSales.put(i, 0);
            monthlyDistributorSalesPrices.put(i, 0);
        }

        // 지점 판매량 및 매출액 집계
        for (VehicleDistributorSales sale : distributorSalesList) {
            int month = sale.getSaleDate().getMonthValue();
            monthlyDistributorSales.put(month, monthlyDistributorSales.get(month) + sale.getDistributorSaleCount());
            monthlyDistributorSalesPrices.put(month, monthlyDistributorSalesPrices.get(month) + sale.getDistributorSalePrices());
        }

        // 모델에 데이터 추가
        model.addAttribute("monthlyIndividualSales", monthlyIndividualSales); // 개인 월별 판매량 추가
        model.addAttribute("monthlyIndividualSalePrices", monthlyIndividualSalePrices); // 개인 월별 매출액 추가
        model.addAttribute("topDistributors", topDistributors); // 부서별 상위 5개 판매량 및 매출액 추가
        model.addAttribute("monthlyDistributorSales", monthlyDistributorSales); // 지점의 월별 판매량 추가
        model.addAttribute("monthlyDistributorSalesPrices", monthlyDistributorSalesPrices); // 지점의 월별 매출액 추가
        model.addAttribute("memNo", memNo); // 사용자 memNo 추가

        return "home";
    }
}
