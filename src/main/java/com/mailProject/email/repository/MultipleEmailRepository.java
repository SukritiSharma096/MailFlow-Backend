package com.mailProject.email.repository;

import com.mailProject.email.entity.MultipleEmailAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MultipleEmailRepository extends JpaRepository<MultipleEmailAccounts, Long> {

    boolean existsByUsername(String username);

    Optional<MultipleEmailAccounts> findById(Long id);

    Optional<MultipleEmailAccounts> findByUsername(String username);

}
