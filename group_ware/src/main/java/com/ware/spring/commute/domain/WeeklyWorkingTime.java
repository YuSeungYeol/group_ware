package com.ware.spring.commute.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "weekly_working_time")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyWorkingTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weekNo;

    private Long memNo;

    private int weekHours;

    private int weekMinutes;

    private LocalDate startOfWeek;

    private LocalDate endOfWeek;
}
