package com.mailProject.email.repository;

import com.mailProject.email.entity.MailScheduler;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface MailSchedulerRepository
        extends JpaRepository<MailScheduler, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE MailScheduler s SET s.status = false WHERE s.id <> :id")
    void disableAllExcept(@Param("id") Long id);
    Optional<MailScheduler> findByStatusTrue();
}
