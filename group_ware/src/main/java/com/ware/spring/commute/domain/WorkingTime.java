package com.ware.spring.commute.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "working_time")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timeNo;

    private Long memNo;

    private int totalHours;

    private int totalMinutes;

    private LocalDateTime lastUpdated;

    private int totalDate;
}
