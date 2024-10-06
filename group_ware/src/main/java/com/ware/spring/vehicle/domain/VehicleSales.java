package com.ware.spring.vehicle.domain;

import java.time.LocalDate;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_no")
    private Long salesNo;

    @ManyToOne
    @JoinColumn(name = "distributor_no", nullable = false)
    private Distributor distributor; // Distributor 객체로 설정

    @ManyToOne
    @JoinColumn(name = "vehicle_no", nullable = false)
    private Vehicle vehicle; // Vehicle 객체 유지

    @ManyToOne
    @JoinColumn(name = "mem_no", nullable = false)
    private Member member; // Member 객체로 설정

    @Column(name = "sale_count", nullable = false)
    private Integer saleCount;

    @Column(name = "sale_prices", nullable = false)
    private Integer salePrices;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;
}
