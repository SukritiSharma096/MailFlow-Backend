package com.mailProject.email.service;

import com.mailProject.email.dto.SchedulerRequestDto;
import com.mailProject.email.dto.SchedulerResponseDto;
import com.mailProject.email.entity.MailScheduler;
import com.mailProject.email.repository.MailSchedulerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SchedulerService {

    @Autowired
    private MailSchedulerRepository repository;

    public SchedulerResponseDto create(SchedulerRequestDto dto) {

        CronExpression.parse(dto.getCronExpression());

        MailScheduler entity = new MailScheduler();
        entity.setName(dto.getName());
        entity.setCronExpression(dto.getCronExpression());
        entity.setDescription(dto.getDescription());
        entity.setStatus(false);

        MailScheduler saved = repository.save(entity);
        return mapToResponse(saved);
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
    public void delete(Long id) {
        repository.deleteById(id);
    }
    public SchedulerResponseDto toggleStatus(Long id, boolean status) {
        MailScheduler entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduler not found"));

        entity.setStatus(!entity.getStatus());

        MailScheduler updated = repository.save(entity);
        return mapToResponse(updated);
    }

    private SchedulerResponseDto mapToResponse(MailScheduler e) {
        SchedulerResponseDto dto = new SchedulerResponseDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCronExpression(e.getCronExpression());
        dto.setDescription(e.getDescription());
        dto.setStatus(e.getStatus());
        return dto;
    }
}
