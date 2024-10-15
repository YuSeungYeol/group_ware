package com.ware.spring.authorization.domain;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import com.ware.spring.authLate.domain.AuthLateDto;
import com.ware.spring.authOff.domain.AuthOffDto;
import com.ware.spring.authTrip.domain.AuthTripDto;
import com.ware.spring.member.domain.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ware.spring.approval_route.domain.ApprovalRoute;
import com.ware.spring.approval_route.domain.ApprovalRouteDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AuthorizationDto {
    private Long authorNo;
    private Long memNo;
    private String memName;
    private String empNo;  // String 타입으로 유지
    private Long distributorNo;
    private String authorName;
    private String authorStatus;
    private LocalDateTime authorRegDate;
    private LocalDateTime authorModDate;
    private String authorOriThumbnail;
    private String authorNewThumbnail;
    private String authTitle;
    private String authContent;
    private String doctype;
    
    // 추가된 필드
    private String isApprover; // 결재자 여부
    private String isReferer;  // 참조자 여부
    private String signature;  // 결재 관련 사인 첨부 내용

    // 문서 유형별 추가된 필드
    private String leaveType;    // 휴가 구분
    private String startDate;    // 시작 일정, 지각 일시, 해외 출장 시작 일정, 외근 시작 일시, 야근 시작 일시
    private String endDate;      // 종료 일정, 해외 출장 종료 일정, 외근 종료 일시, 야근 종료 일시
    private Double startEndDate; // 기간이나 일수
    private String lateType;     // 지각 사유
    private String tripType;     // 출장 구분
    private String outsideType;  // 외근 구분
    private String overtimeType; // 야근 구분
    
    private List<ApprovalRouteDto> approvalRoutes;
    
    private List<ApprovalRouteDto> approvers = new ArrayList<>(); // 초기화 추가
    private List<ApprovalRouteDto> referers = new ArrayList<>(); // 초기화 추가

    public Authorization toEntity(Member member, List<ApprovalRoute> approvalRoute) {
        return Authorization.builder()
                .authorNo(null)
                .authorName(authorName)
                .authorStatus(authorStatus)
                .authorRegDate(authorRegDate)
                .authorModDate(authorModDate)
                .authorOriThumbnail(authorOriThumbnail != null ? deserialize(authorOriThumbnail) : null)
                .authorNewThumbnail(authorNewThumbnail != null ? deserialize(authorNewThumbnail) : null)
                .authTitle(authTitle)
                .authContent(authContent) // 추가된 필드 반영
                .doctype(doctype)
                .folderPrivateNo(1)  // 기본 값 설정 또는 DTO에서 받아온 값으로 설정
                .signature(signature)  // 서명 필드를 엔티티에 반영
                .member(member)
                .empNo(empNo)  // String 타입으로 설정
                .leaveType(leaveType)    // 휴가 구분
                .startDate(startDate)    // 시작 일정
                .endDate(endDate)        // 종료 일정
                .startEndDate(startEndDate) // 기간이나 일수
                .lateType(lateType)      // 지각 사유
                .tripType(tripType)      // 출장 구분
                .outsideType(outsideType) // 외근 구분
                .overtimeType(overtimeType) // 야근 구분
                .approvalRoutes(approvalRoute != null ? approvalRoute : new ArrayList<>())
                .build();
    }

    public static AuthorizationDto toDto(Authorization authorization) {
            // ApprovalRoute 리스트를 DTO로 변환하여 추가
            List<ApprovalRouteDto> approvalRoutes = authorization.getApprovalRoutes().stream()
                .map(ApprovalRouteDto::toDto)
                .collect(Collectors.toList());

            // 결재자 리스트 필터링
            List<ApprovalRouteDto> approvers = approvalRoutes.stream()
                    .filter(route -> "Y".equals(route.getIsApprover()))  // "Y"를 사용해 필터링
                    .collect(Collectors.toList());

            // 참조자 리스트 필터링
            List<ApprovalRouteDto> referers = approvalRoutes.stream()
                    .filter(route -> "Y".equals(route.getIsReferer()))  // "Y"를 사용해 필터링
                    .collect(Collectors.toList());

            return AuthorizationDto.builder()
                    .authorNo(authorization.getAuthorNo())
                    .memNo(authorization.getMember() != null ? authorization.getMember().getMemNo() : null)
                    .memName(authorization.getMember() != null ? authorization.getMember().getMemName() : null)
                    .empNo(authorization.getMember() != null ? authorization.getMember().getEmpNo() : null)
                    .distributorNo(authorization.getMember() != null && authorization.getMember().getDistributor() != null ? 
                            authorization.getMember().getDistributor().getDistributorNo() : null)
                    .authorName(authorization.getAuthorName())
                    .authorStatus(authorization.getAuthorStatus())
                    .authorRegDate(authorization.getAuthorRegDate())
                    .authorModDate(authorization.getAuthorModDate())
                    .authorOriThumbnail(authorization.getAuthorOriThumbnail())
                    .authorNewThumbnail(authorization.getAuthorNewThumbnail())
                    .doctype(authorization.getDoctype())
                    .authTitle(authorization.getAuthTitle())
                    .authContent(authorization.getAuthContent())
                    .signature(authorization.getSignature())
                    .leaveType(authorization.getLeaveType())
                    .startDate(authorization.getStartDate())
                    .endDate(authorization.getEndDate())
                    .startEndDate(authorization.getStartEndDate())
                    .lateType(authorization.getLateType())
                    .tripType(authorization.getTripType())
                    .outsideType(authorization.getOutsideType())
                    .overtimeType(authorization.getOvertimeType())
                    .approvalRoutes(approvalRoutes)
                    .approvers(approvers)
                    .referers(referers)
                    .build();
        }


    public String deserialize(String serializedData) {
        try {
            // ObjectStream 대신 바이트를 바로 사용하도록 수정 (직렬화된 데이터를 바이트 배열로 직접 역직렬화)
            byte[] data = Base64.getDecoder().decode(serializedData);  // Base64 인코딩된 직렬화 데이터가 있다고 가정
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object deserializedObject = ois.readObject();
            
            // 타입에 따라 적절하게 처리
            if (deserializedObject instanceof String) {
                return (String) deserializedObject;
            } else {
                return deserializedObject.toString();  // 기타 객체를 문자열로 변환
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // 역직렬화가 실패했을 때 처리
        }
    }
    
    public void setApproversFromJson(String approversJson) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // JSON 문자열을 List<ApprovalRouteDto>로 변환하여 필드에 할당
            this.approvers = Arrays.asList(mapper.readValue(approversJson, ApprovalRouteDto[].class));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert String to ApprovalRouteDto list", e);
        }
    }

    // 기존 setApprovers 메서드가 필요할 경우 추가
    public void setApprovers(List<ApprovalRouteDto> approvers) {
        this.approvers = approvers;
    }

}
