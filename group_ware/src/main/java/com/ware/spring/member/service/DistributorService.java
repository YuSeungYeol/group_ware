package com.ware.spring.member.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.DistributorDto;
import com.ware.spring.member.domain.MemberDto;
import com.ware.spring.member.repository.DistributorRepository;
import com.ware.spring.member.repository.MemberRepository;
@Service
public class DistributorService {

    private final DistributorRepository distributorRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public DistributorService(DistributorRepository distributorRepository, MemberRepository memberRepository) {
        this.distributorRepository = distributorRepository;
        this.memberRepository = memberRepository;
    }

    public List<DistributorDto> getAllDistributorsWithMembers() {
        // 지점 정보와 해당 지점의 멤버 정보를 함께 포함한 distributor 반환
        return distributorRepository.findAll().stream().map(distributor -> {
            List<MemberDto> members = memberRepository.findMembersByDistributorNo(distributor.getDistributorNo())
                                        .stream()
                                        .map(member -> MemberDto.toDto(member))  // MemberDto의 toDto 사용
                                        .collect(Collectors.toList());
            return new DistributorDto(
                distributor.getDistributorNo(),
                distributor.getDistributorName(),
                distributor.getDistributorPhone(),
                distributor.getDistributorAddr(),
                distributor.getDistributorAddrDetail(),
                distributor.getDistributorLatitude(),
                distributor.getDistributorLongitude(),
                distributor.getDistributorStatus(),
                members // 멤버 추가
            );
        }).collect(Collectors.toList());
    }

    public List<MemberDto> getMembersByDistributor(Long distributorNo) {
        return memberRepository.findMembersByDistributorNo(distributorNo)  // JPQL 메서드 사용
                               .stream()
                               .map(member -> MemberDto.toDto(member))  // MemberDto의 toDto 사용
                               .collect(Collectors.toList());
    }
    public Page<Distributor> searchDistributorsByCriteria(String searchType, String searchText, String statusFilter, Pageable pageable) {
        // 검색어와 필터가 모두 비어있을 경우 전체 조회
        if ((searchText == null || searchText.isEmpty()) && ("all".equals(statusFilter) || statusFilter == null)) {
            return distributorRepository.findAll(pageable);
        }

        if ("name".equals(searchType)) {
            if ("all".equals(statusFilter)) {
                return distributorRepository.findByDistributorNameContaining(searchText, pageable);
            } else {
                int status = "operating".equals(statusFilter) ? 1 : 0; // 운영중(1) 또는 폐점(0)
                return distributorRepository.findByDistributorNameContainingAndDistributorStatus(searchText, status, pageable);
            }
        } else if ("address".equals(searchType)) {
            if ("all".equals(statusFilter)) {
                return distributorRepository.findByDistributorAddrContaining(searchText, pageable);
            } else {
                int status = "operating".equals(statusFilter) ? 1 : 0;
                return distributorRepository.findByDistributorAddrContainingAndDistributorStatus(searchText, status, pageable);
            }
        } else if ("status".equals(searchType)) {
            int status = "operating".equals(statusFilter) ? 1 : 0;
            return distributorRepository.findByDistributorStatus(status, pageable);
        }

        // 기본적으로 전체 조회
        return distributorRepository.findAll(pageable);
    }

    public Page<Distributor> findAllByStatus(int status, Pageable pageable) {
        return distributorRepository.findByDistributorStatus(status, pageable);
    }

    public Page<Distributor> findAllDistributors(Pageable pageable) {
        return distributorRepository.findAll(pageable);
    }


    public void registerDistributor(DistributorDto distributorDto) {
        // DTO의 각 필드를 직접 사용하여 Distributor 엔티티 생성
        Distributor distributor = Distributor.builder()
                .distributorNo(distributorDto.getDistributorNo())
                .distributorName(distributorDto.getDistributorName())
                .distributorPhone(distributorDto.getDistributorPhone())
                .distributorAddr(distributorDto.getDistributorAddr())
                .distributorAddrDetail(distributorDto.getDistributorAddrDetail())
                .distributorLatitude(distributorDto.getDistributorLatitude())
                .distributorLongitude(distributorDto.getDistributorLongitude())
                .distributorStatus(1)
                .build();

        // 데이터베이스에 저장
        distributorRepository.save(distributor);
    }
    public DistributorDto getDistributorById(Long distributorNo) {
        Distributor distributor = distributorRepository.findById(distributorNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 지점을 찾을 수 없습니다. 지점 번호: " + distributorNo));
        return DistributorDto.toDto(distributor);
    }
}



