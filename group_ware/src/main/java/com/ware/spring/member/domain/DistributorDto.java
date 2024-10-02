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
    private double distributorLatitude;
    private double distributorLongitude;
    private int distributorStatus;
    
    // 추가: 해당 지점에 속한 멤버 리스트
    private List<MemberDto> members;
}
