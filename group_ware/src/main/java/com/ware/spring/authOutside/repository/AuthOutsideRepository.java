package com.ware.spring.authOutside.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.authOutside.domain.AuthOutside;

public interface AuthOutsideRepository extends JpaRepository<AuthOutside, Long> {

}
