package com.ware.spring.authLate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ware.spring.authLate.domain.AuthLate;

public interface AuthLateRepository extends JpaRepository<AuthLate, Long> {
}
