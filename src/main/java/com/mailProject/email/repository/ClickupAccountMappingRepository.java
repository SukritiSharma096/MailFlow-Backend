package com.mailProject.email.repository;

import com.mailProject.email.entity.ClickupAccountMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClickupAccountMappingRepository  extends JpaRepository<ClickupAccountMapping, Long> {

    Optional<ClickupAccountMapping> findByAccountId(Long accountId);
}
