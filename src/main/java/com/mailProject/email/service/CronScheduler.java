package com.mailProject.email.service;

import com.mailProject.email.repository.MailSchedulerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class CronScheduler {

    private final MailSchedulerRepository repository;
    private final MailJobService mailJobService;

    @Scheduled(cron = "0 * * * * *")
    public void checkAndRun() {

        LocalDateTime now = LocalDateTime.now().withSecond(0);

        repository.findAll().forEach(job -> {


            if (!Boolean.TRUE.equals(job.getStatus())) return;

            CronExpression cron =
                    CronExpression.parse(job.getCronExpression());

            LocalDateTime next =
                    cron.next(now.minusMinutes(1));

            if (next != null && next.equals(now)) {

                mailJobService.runJob(job.getName());

            }
        });
    }
}


