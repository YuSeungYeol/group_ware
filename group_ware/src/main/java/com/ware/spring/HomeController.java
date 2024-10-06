package com.ware.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ware.spring.security.vo.SecurityUser;
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
    @GetMapping({"/",""})
    public String getHome(Model model) {
        // 현재 로그인한 사용자의 mem_no 가져오기
        SecurityUser userDetails = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memNo = userDetails.getMember().getMemNo();

        // 개인 월별 판매량 집계
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

        // 모델에 추가
        model.addAttribute("monthlyIndividualSales", monthlyIndividualSales);
        model.addAttribute("monthlyIndividualSalePrices", monthlyIndividualSalePrices);

        return "home";
    }

}
