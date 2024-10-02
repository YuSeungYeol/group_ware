package com.ware.spring.member.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.DistributorDto;
import com.ware.spring.member.domain.MemberDto;
import com.ware.spring.member.repository.DistributorRepository;
import com.ware.spring.member.service.DistributorService;

@RestController
@RequestMapping("/api/distributors")
public class DistributorApiController {

    private final DistributorService distributorService;
    private final DistributorRepository distributorRepository;

    @Autowired
    public DistributorApiController(DistributorService distributorService, DistributorRepository distributorRepository) {
        this.distributorService = distributorService;
        this.distributorRepository = distributorRepository;
    }

    @GetMapping
    public List<DistributorDto> getAllDistributors() {
        // 멤버 포함된 distributor 정보 반환
        return distributorService.getAllDistributorsWithMembers();
    }

    // 특정 지점의 멤버 가져오기
    @GetMapping("/members")
    public List<MemberDto> getMembersByDistributor(@RequestParam("distributorNo") Long distributorNo) {
        return distributorService.getMembersByDistributor(distributorNo);
    }
    
    // 결재 관련하여 리스트 업을 위해 생성
    @GetMapping("/getAll")
    public ResponseEntity<List<DistributorDto>> getAllDistributorsList() {
        List<Distributor> distributors = distributorRepository.findAll();
        List<DistributorDto> distributorDtos = distributors.stream()
            .map(distributor -> DistributorDto.builder()
                .distributorNo(distributor.getDistributorNo())
                .distributorName(distributor.getDistributorName())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(distributorDtos);
    }
}

