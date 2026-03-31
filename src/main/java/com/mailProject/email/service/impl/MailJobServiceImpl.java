package com.mailProject.email.service.impl;

import com.mailProject.email.dto.MailJobHistoryResponseDto;
import com.mailProject.email.entity.Admin;
import com.mailProject.email.entity.MailJobHistory;
import com.mailProject.email.entity.MultipleEmailAccounts;
import com.mailProject.email.repository.AdminRepository;
import com.mailProject.email.repository.MailJobHistoryRepository;
import com.mailProject.email.repository.MultipleEmailRepository;
import com.mailProject.email.service.ClickupConfigService;
import com.mailProject.email.service.MailJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailJobServiceImpl implements MailJobService {

    private final MailProcessingService processingService;
    private final MailJobHistoryRepository historyRepo;
    private final MultipleEmailRepository accountRepo;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ClickupConfigService clickupConfigService;

    @Override
    @Transactional
    public void runJob(String runType) {

        MailJobHistory history = new MailJobHistory();
        history.setRunType(runType);
        history.setStartTime(LocalDateTime.now());

        int totalTasks = 0;
        boolean anyFailure = false;

        if (!clickupConfigService.isConfigured()) {
            log.warn("ClickUp not configured. Skipping job...");

            history.setStatus("FAILED");
            history.setEndTime(LocalDateTime.now());
            historyRepo.save(history);
            return;
        }

        List<Admin> admins = adminRepository.findAllByActiveTrue();

        for (Admin admin : admins) {

            try {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                admin.getUsername(), null, List.of()
                        )
                );

                List<MultipleEmailAccounts> accounts =
                        accountRepo.findByActiveTrue();

                for (MultipleEmailAccounts account : accounts) {

                    totalTasks += processingService.processNewMails(account.getId());
                }

            } catch (Exception e) {
                anyFailure = true;
                log.error("Error in job execution: {}", e.getMessage(), e);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        history.setMailCount(totalTasks);
        history.setTaskCreated(totalTasks);

        if (totalTasks > 0 && !anyFailure) {
            history.setStatus("SUCCESS");
        } else if (totalTasks > 0) {
            history.setStatus("PARTIAL_SUCCESS");
        } else {
            history.setStatus("NO_NEW_MAILS");
        }

        history.setEndTime(LocalDateTime.now());

        historyRepo.save(history);
    }

    @Override
    public MailJobHistoryResponseDto getLatest() {
        return historyRepo
                .findTopByOrderByStartTimeDesc()
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    public List<MailJobHistoryResponseDto> getHistory() {
        return historyRepo.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private MailJobHistoryResponseDto mapToDto(MailJobHistory history) {
        return new MailJobHistoryResponseDto(
                history.getId(),
                history.getRunType(),
                history.getMailCount(),
                history.getTaskCreated(),
                history.getStatus(),
                history.getStartTime(),
                history.getEndTime()
        );
    }
}