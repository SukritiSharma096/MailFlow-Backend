package com.mailProject.email.repository;

import com.mailProject.email.entity.MailJobHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailJobHistoryRepository extends JpaRepository<MailJobHistory, Long> {

    Optional<MailJobHistory> findTopByOrderByStartTimeDesc();

}
