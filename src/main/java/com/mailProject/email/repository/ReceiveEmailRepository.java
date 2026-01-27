package com.mailProject.email.repository;

import com.mailProject.email.entity.ReceivedEmails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceiveEmailRepository extends JpaRepository<ReceivedEmails, Long> {

    List<ReceivedEmails> findByAccountId(Long accountId);

    Optional<ReceivedEmails> findByIdAndAccountId(Long id, Long accountId);
}
