package com.mailProject.email.repository;

import com.mailProject.email.entity.SentMails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SentEmailRepository extends JpaRepository<SentMails, Long> {

    List<SentMails> findByAccountIdOrderBySentAtDesc(Long accountId);

}
