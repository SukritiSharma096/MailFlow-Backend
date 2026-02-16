package com.mailProject.email.service;

import com.mailProject.email.dto.SchedulerRequestDto;
import com.mailProject.email.dto.SchedulerResponseDto;
import com.mailProject.email.entity.MailScheduler;
import com.mailProject.email.repository.MailSchedulerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
public class SchedulerService {

    @Autowired
    private MailSchedulerRepository repository;
    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ClickUpService clickUpService;

    private ScheduledFuture<?> scheduledTask;

    @PostConstruct
    public void init() {
        startEnabledScheduler();
    }

    public void startEnabledScheduler() {

        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        Optional<MailScheduler> optional = repository.findByStatusTrue();

        if (optional.isEmpty()) {
            System.out.println("No active scheduler found");
            return;
        }

        MailScheduler scheduler = optional.get();

        if (scheduler.getCronExpression() == null ||
                scheduler.getCronExpression().trim().isEmpty()) {

            System.out.println("Active scheduler has empty cron expression. Skipping scheduling.");
            return;
        }

        CronExpression.parse(scheduler.getCronExpression());

        scheduledTask = taskScheduler.schedule(
                () -> executeTask(scheduler.getId()),
                new CronTrigger(scheduler.getCronExpression())
        );

        System.out.println("Scheduler started: " + scheduler.getName());
    }

    private void executeTask(Long schedulerId) {
        System.out.println("Running scheduler ID: " + schedulerId);
        clickUpService.pushEmailsToClickUp(schedulerId);
    }

    public SchedulerResponseDto create(SchedulerRequestDto dto) {

        CronExpression.parse(dto.getCronExpression());

        MailScheduler entity = new MailScheduler();
        entity.setName(dto.getName());
        entity.setCronExpression(dto.getCronExpression());
        entity.setDescription(dto.getDescription());
        entity.setStatus(false);

        return mapToResponse(repository.save(entity));
    }

    public List<SchedulerResponseDto> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SchedulerResponseDto update(Long id, SchedulerRequestDto dto) {

        MailScheduler entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduler not found"));

        CronExpression.parse(dto.getCronExpression());

        entity.setName(dto.getName());
        entity.setCronExpression(dto.getCronExpression());
        entity.setDescription(dto.getDescription());

        return mapToResponse(repository.save(entity));
    }
    @Transactional
    public SchedulerResponseDto toggleStatus(Long id) {
        List<MailScheduler> allSchedulers = repository.findAll();

        for (MailScheduler s : allSchedulers) {
            s.setStatus(s.getId().equals(id));
        }

        repository.saveAll(allSchedulers);

        startEnabledScheduler();

        return mapToResponse(
                repository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Scheduler not found"))
        );
    }

    public void delete(Long id) {
        repository.deleteById(id);
        startEnabledScheduler();
    }

    private SchedulerResponseDto mapToResponse(MailScheduler e) {
        SchedulerResponseDto dto = new SchedulerResponseDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCronExpression(e.getCronExpression());
        dto.setDescription(e.getDescription());
        dto.setStatus(Boolean.TRUE.equals(e.getStatus()));
        return dto;
    }
}