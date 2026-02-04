package com.mailProject.email.repository;

import com.mailProject.email.entity.MailScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailSchedulerRepository
        extends JpaRepository<MailScheduler, Long> {
}
