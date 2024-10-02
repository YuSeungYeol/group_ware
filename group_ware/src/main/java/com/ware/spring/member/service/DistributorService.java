package com.ware.spring.member.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}




