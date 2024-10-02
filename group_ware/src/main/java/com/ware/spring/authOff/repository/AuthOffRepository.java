package com.ware.spring.authOff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ware.spring.authOff.domain.AuthOff;

public interface AuthOffRepository extends JpaRepository<AuthOff, Long> {
}
