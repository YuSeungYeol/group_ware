package com.ware.spring.authTrip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ware.spring.authTrip.domain.AuthTrip;

public interface AuthTripRepository extends JpaRepository<AuthTrip, Long> {
}
