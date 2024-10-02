package com.ware.spring.member.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distributor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Distributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="distributor_no")
    private Long distributorNo;

    @Column(name="distributor_name")
    private String distributorName;

    @Column(name="distributor_phone")
    private String distributorPhone;

    @Column(name="distributor_addr")
    private String distributorAddr;

    @Column(name="distributor_latitude")
    private double distributorLatitude;

    @Column(name="distributor_longitude")
    private double distributorLongitude;

    @Column(name="distributor_status")
    private int distributorStatus;
    @OneToMany(mappedBy = "distributor")
    private List<Member> members; 
}
