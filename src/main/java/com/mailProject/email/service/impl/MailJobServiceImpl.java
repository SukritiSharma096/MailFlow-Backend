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
    public void runJob(String runType) {

        MailJobHistory history = new MailJobHistory();
        history.setRunType(runType);
        history.setStartTime(LocalDateTime.now());

        try {

            List<MultipleEmailAccounts> accounts = accountRepo.findByActiveTrue();

            // Parallel processing of accounts
            int totalTasks = accounts.parallelStream()
                    .mapToInt(account -> {
                        try {
                            return processingService.processNewMails(account.getId());
                        } catch (Exception e) {
                            log.error("Failed processing account {}: {}", account.getUsername(), e.getMessage(), e);
                            return 0;
                        }
                    })
                    .sum();

            history.setMailCount(totalTasks);
            history.setTaskCreated(totalTasks);
            history.setStatus("SUCCESS");

        } catch (Exception e) {
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
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
