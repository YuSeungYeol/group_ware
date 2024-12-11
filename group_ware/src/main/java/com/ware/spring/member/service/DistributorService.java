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

    /**
     * 모든 지점과 해당 지점의 멤버 정보를 포함한 리스트를 반환합니다.
     * 기술: Spring Data JPA
     * 설명: 각 지점에 포함된 멤버 정보를 함께 조회하여 DTO로 변환한 후 반환합니다.
     *
     * @return 모든 지점과 그 지점의 멤버 리스트가 포함된 DistributorDto 리스트
     */
    public List<DistributorDto> getAllDistributorsWithMembers() {
        return distributorRepository.findAll().stream().map(distributor -> {
            List<MemberDto> members = memberRepository.findMembersByDistributorNo(distributor.getDistributorNo())
                                        .stream()
                                        .map(MemberDto::toDto)
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
                members
            );
        }).collect(Collectors.toList());
    }

    /**
     * 특정 지점에 속한 멤버들을 반환합니다.
     * 기술: Spring Data JPA
     * 설명: 주어진 지점 번호에 해당하는 멤버 리스트를 조회하고 DTO로 변환하여 반환합니다.
     *
     * @param distributorNo 지점 번호
     * @return 해당 지점의 멤버 리스트가 포함된 MemberDto 리스트
     */
    public List<MemberDto> getMembersByDistributor(Long distributorNo) {
        return memberRepository.findMembersByDistributorNo(distributorNo)
                               .stream()
                               .map(MemberDto::toDto)
                               .collect(Collectors.toList());
    }

    /**
     * 검색 조건 및 필터를 사용하여 지점 리스트를 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 지점 이름, 주소, 상태 필터에 따라 검색어에 맞는 지점 리스트를 페이징된 형태로 반환합니다.
     *
     * @param searchType 검색 유형 ('name', 'address', 'status')
     * @param searchText 검색어
     * @param statusFilter 상태 필터 ('operating', 'closed', 'all')
     * @param pageable 페이징 정보
     * @return 페이징된 지점 리스트
     */
    public Page<Distributor> searchDistributorsByCriteria(String searchType, String searchText, String statusFilter, Pageable pageable) {
        if ((searchText == null || searchText.isEmpty()) && ("all".equals(statusFilter) || statusFilter == null)) {
            return distributorRepository.findAll(pageable);
        }

        if ("name".equals(searchType)) {
            if ("all".equals(statusFilter)) {
                return distributorRepository.findByDistributorNameContaining(searchText, pageable);
            } else {
                int status = "operating".equals(statusFilter) ? 1 : 0;
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

        return distributorRepository.findAll(pageable);
    }

    /**
     * 특정 상태의 지점들을 페이징된 형태로 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 주어진 상태에 해당하는 지점들을 페이징된 리스트로 반환합니다.
     *
     * @param status 지점 상태 (1: 운영 중, 0: 폐점)
     * @param pageable 페이징 정보
     * @return 페이징된 지점 리스트
     */
    public Page<Distributor> findAllByStatus(int status, Pageable pageable) {
        return distributorRepository.findByDistributorStatus(status, pageable);
    }

    /**
     * 모든 지점 리스트를 페이징된 형태로 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 모든 지점을 페이징된 리스트로 반환합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 모든 지점 리스트
     */
    public Page<Distributor> findAllDistributors(Pageable pageable) {
        return distributorRepository.findAll(pageable);
    }

    /**
     * 새로운 지점을 등록합니다.
     * 기술: Spring Data JPA
     * 설명: DTO로부터 지점 엔티티를 생성하여 데이터베이스에 저장합니다. 기본 상태는 '운영 중'으로 설정됩니다.
     *
     * @param distributorDto 등록할 지점 정보를 담은 DTO
     */
    public void registerDistributor(DistributorDto distributorDto) {
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
        distributorRepository.save(distributor);
    }

    /**
     * 특정 지점을 조회하여 DTO로 반환합니다.
     * 기술: Spring Data JPA
     * 설명: 지점 번호로 지점을 조회하고, 해당 지점이 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param distributorNo 지점 번호
     * @return 조회된 지점 정보를 담은 DistributorDto
     */
    public DistributorDto getDistributorById(Long distributorNo) {
        Distributor distributor = distributorRepository.findById(distributorNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 지점을 찾을 수 없습니다. 지점 번호: " + distributorNo));
        return DistributorDto.toDto(distributor);
    }

    /**
     * 검색, 필터링 및 정렬을 통합 처리한 지점 리스트를 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 검색 조건, 상태 필터, 정렬 조건을 사용하여 페이징된 지점 리스트를 반환합니다.
     *
     * @param searchType 검색 유형 ('name', 'address')
     * @param searchText 검색어
     * @param statusFilter 상태 필터 ('operating', 'closed', 'all')
     * @param sortField 정렬 기준 필드
     * @param sortDirection 정렬 방향 ('asc', 'desc')
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 지점 리스트
     */
    public Page<Distributor> getDistributorsWithSorting(String searchType, String searchText, String statusFilter, String sortField, String sortDirection, Pageable pageable) {
        int status = -1; // 기본값: 전체
        if ("operating".equals(statusFilter)) {
            status = 1;
        } else if ("closed".equals(statusFilter)) {
            status = 0;
        }

        // 검색어가 없으면 상태 필터만 적용
        if (searchText == null || searchText.isEmpty()) {
            return status == -1
                    ? distributorRepository.findAll(pageable)
                    : distributorRepository.findByDistributorStatus(status, pageable);
        }

        // 검색 조건과 상태 필터를 조합하여 처리
        return distributorRepository.findBySearchTypeAndTextAndStatus(searchType, searchText, status, pageable);
    }
}
