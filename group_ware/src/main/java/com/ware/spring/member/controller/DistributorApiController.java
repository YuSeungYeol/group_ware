package com.ware.spring.member.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 모든 지점 정보를 멤버 정보와 함께 반환합니다.
     * 설명: 각 지점에 속한 멤버 정보를 포함하여 모든 지점의 정보를 반환합니다.
     * 
     * @return DistributorDto 리스트 - 모든 지점과 그 지점의 멤버 리스트가 포함된 DTO 리스트
     */
    @GetMapping
    public List<DistributorDto> getAllDistributors() {
        return distributorService.getAllDistributorsWithMembers();
    }

    /**
     * 특정 지점의 멤버 리스트를 반환합니다.
     * 설명: 주어진 지점 번호에 해당하는 멤버 리스트를 반환합니다.
     * 
     * @param distributorNo 지점 번호
     * @return MemberDto 리스트 - 해당 지점에 속한 멤버 정보가 포함된 DTO 리스트
     */
    @GetMapping("/members")
    public List<MemberDto> getMembersByDistributor(@RequestParam("distributorNo") Long distributorNo) {
        return distributorService.getMembersByDistributor(distributorNo);
    }

    /**
     * 모든 지점 리스트를 간단한 정보로 반환합니다 (결재 관련 리스트 용도).
     * 설명: 모든 지점을 간단한 정보(지점 번호, 지점 이름)로 반환하여 결재 관련 리스트에 사용합니다.
     * 
     * @return ResponseEntity - DistributorDto 리스트와 함께 반환
     */
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

    /**
     * 새로운 지점을 등록합니다.
     * 설명: 요청 본문으로부터 받은 DistributorDto 객체를 사용하여 새로운 지점을 등록합니다.
     * 성공 시 성공 메시지를, 실패 시 오류 메시지를 반환합니다.
     * 
     * @param distributorDto 등록할 지점 정보를 담은 DTO
     * @return ResponseEntity - 등록 성공 또는 실패 메시지
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerDistributor(@RequestBody DistributorDto distributorDto) {
        try {
            distributorService.registerDistributor(distributorDto);
            return ResponseEntity.ok("지점 등록이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("지점 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
