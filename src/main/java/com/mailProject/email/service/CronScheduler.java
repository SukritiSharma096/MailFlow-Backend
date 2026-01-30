package com.mailProject.email.service;

import com.mailProject.email.dto.SchedulerRequestDto;
import com.mailProject.email.repository.MailSchedulerRepository;
import com.mailProject.email.service.impl.ClickUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component

public class CronScheduler {
    @Autowired
    private MailSchedulerRepository repository;

    @Scheduled(cron = "0 * * * * *")
    public void checkAndRun() {

        LocalDateTime now = LocalDateTime.now().withSecond(0);

        repository.findAll().forEach(job -> {

            if (!"ENABLED".equals(job.getStatus())) return;

            CronExpression cron =
                    CronExpression.parse(job.getCronExpression());

            LocalDateTime next =
                    cron.next(now.minusMinutes(1));

            if (next != null && next.equals(now)) {
                System.out.println(" Executed: " + job.getName());
            }
        });
    }
}


