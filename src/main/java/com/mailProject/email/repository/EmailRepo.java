package com.mailProject.email.repository;

import com.mailProject.email.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailRepo extends JpaRepository <Email, Long> {
    boolean existsBySenderAndSubject(String sender, String subject);
    List<Email> findByTaskCreatedFalse();
}
