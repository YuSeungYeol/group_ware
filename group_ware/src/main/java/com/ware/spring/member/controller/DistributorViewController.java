package com.ware.spring.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.service.DistributorService;

@Controller
public class DistributorViewController {

    @Autowired
    private DistributorService distributorService;

    /**
     * 지점 리스트 페이지를 반환합니다.
     * 설명: 지점의 상태 필터와 검색 조건을 기반으로 지점 목록을 조회하여 페이징 처리된 결과를 반환합니다.
     * 검색 조건이 있는 경우 검색어와 필터에 따라 지점 목록을 조회하고, 
     * 검색 조건이 없는 경우 상태 필터에 따라 지점 목록을 필터링합니다.
     * 
     * @param statusFilter 상태 필터 (1: 운영 중, 2: 폐점, all: 모든 지점)
     * @param searchType 검색 유형 ('name', 'address', 등)
     * @param searchText 검색어
     * @param page 페이지 번호 (0부터 시작)
     * @param model 뷰에 전달할 데이터
     * @return 지점 리스트 페이지 뷰 (distributor/distributor_list)
     */
    @GetMapping("/distributor/list")
    public String listDistributors(
            @RequestParam(value = "statusFilter", required = false, defaultValue = "all") String statusFilter,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "sortField", required = false, defaultValue = "distributorName") String sortField,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, 10, sort);
        Page<Distributor> distributors;
        if (searchText != null && !searchText.isEmpty()) {
            distributors = distributorService.searchDistributorsByCriteria(searchType, searchText, statusFilter, pageable);
        } else {
            if ("1".equals(statusFilter)) {
                distributors = distributorService.findAllByStatus(1, pageable);
            } else if ("2".equals(statusFilter)) {
                distributors = distributorService.findAllByStatus(2, pageable);
            } else {
                distributors = distributorService.findAllDistributors(pageable);
            }
        }
        int totalPages = distributors.getTotalPages();
        int pageNumber = distributors.getNumber();
        int pageGroupSize = 5;
        int currentGroup = (pageNumber / pageGroupSize);
        int startPage = currentGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);
        model.addAttribute("distributorList", distributors.getContent());
        model.addAttribute("page", distributors);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchType", searchType);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("searchText", searchText);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "distributor/distributor_list";
    }

    /**
     * 지점 등록 페이지를 반환합니다.
     * 설명: 지점 등록 폼이 있는 페이지로 이동합니다.
     * 
     * @return 지점 등록 페이지 뷰 (distributor/distributor_register)
     */
    @GetMapping("/distributor/register")
    public String showRegisterPage() {
        return "distributor/distributor_register";
    }
}
