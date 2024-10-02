package com.ware.spring.member.domain;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rank {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="rank_no")
    private Long rankNo;
    
    @Column(name="rank_name")
    private String rankName;
}
