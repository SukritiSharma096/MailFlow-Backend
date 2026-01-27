package com.mailProject.email.repository;

import com.mailProject.email.entity.MultipleEmailAccounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultipleEmailRepository extends JpaRepository<MultipleEmailAccounts, Long> {
}
