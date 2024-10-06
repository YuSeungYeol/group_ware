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
@Table(name = "vehicle_distributor_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDistributorSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "distributor_sales_no")
    private Long distributorSalesNo;

    @ManyToOne
    @JoinColumn(name = "distributor_no", nullable = false)
    private Distributor distributor; // Distributor 객체로 설정

    @ManyToOne
    @JoinColumn(name = "mem_no", nullable = false)
    private Member member; // Member 객체로 설정

    @Column(name = "distributor_sale_count", nullable = false)
    private Integer distributorSaleCount;

    @Column(name = "distributor_sale_prices", nullable = false)
    private Integer distributorSalePrices;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    // getter 메서드 추가 (필요 시)
}
