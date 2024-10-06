package com.ware.spring.member.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributorDto {

    private Long distributorNo;
    private String distributorName;
    private String distributorPhone;
    private String distributorAddr;
    private String distributorAddrDetail;
    private double distributorLatitude;
    private double distributorLongitude;
    private int distributorStatus;

    // 추가: 해당 지점에 속한 멤버 리스트
    private List<MemberDto> members;
    // toEntity 메서드 추가
    public Distributor toEntity() {
        return Distributor.builder()
                .distributorNo(distributorNo)
                .distributorName(distributorName)
                .distributorPhone(distributorPhone)
                .distributorAddr(distributorAddr)
                .distributorAddrDetail(distributorAddrDetail)
                .distributorLatitude(distributorLatitude)
                .distributorLongitude(distributorLongitude)
                .distributorStatus(distributorStatus)
                .build();
    }
    // toDto 메서드 추가
    public static DistributorDto toDto(Distributor distributor) {
        return DistributorDto.builder()
                .distributorNo(distributor.getDistributorNo())
                .distributorName(distributor.getDistributorName())
                .distributorPhone(distributor.getDistributorPhone())
                .distributorAddr(distributor.getDistributorAddr())
                .distributorAddrDetail(distributor.getDistributorAddrDetail())
                .distributorLatitude(distributor.getDistributorLatitude())
                .distributorLongitude(distributor.getDistributorLongitude())
                .distributorStatus(distributor.getDistributorStatus())
                .build();
    }

}
