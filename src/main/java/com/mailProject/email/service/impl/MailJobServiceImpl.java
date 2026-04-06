package com.mailProject.email.service.impl;

import com.mailProject.email.dto.MailJobHistoryResponseDto;
import com.mailProject.email.entity.MailJobHistory;
import com.mailProject.email.entity.MultipleEmailAccounts;
import com.mailProject.email.repository.MailJobHistoryRepository;
import com.mailProject.email.repository.MultipleEmailRepository;
import com.mailProject.email.service.MailJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public void runJob(String runType) {

        MailJobHistory history = new MailJobHistory();
        history.setRunType(runType);
        history.setStartTime(LocalDateTime.now());

        int totalTasks = 0;
        boolean anyFailure = false;

        List<MultipleEmailAccounts> accounts = accountRepo.findByActiveTrue();

        for (MultipleEmailAccounts account : accounts) {
            try {
                totalTasks += processingService.processNewMails(account.getId(), true);
            } catch (Exception e) {
                anyFailure = true;
                log.error("Error in job execution for account {}: {}", account.getId(), e.getMessage(), e);
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