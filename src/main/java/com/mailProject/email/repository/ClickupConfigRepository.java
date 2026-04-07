package com.mailProject.email.repository;

import com.mailProject.email.entity.Admin;
import com.mailProject.email.entity.ClickupConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClickupConfigRepository extends JpaRepository<ClickupConfig, Long> {

    Optional<ClickupConfig> findTopByOrderByIdDesc();
}
