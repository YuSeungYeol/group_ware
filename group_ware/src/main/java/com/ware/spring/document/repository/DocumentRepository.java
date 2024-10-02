package com.ware.spring.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ware.spring.document.domain.Document;

public interface DocumentRepository extends JpaRepository<Document,Long> {

}
