package com.mailProject.email.service.impl;

import com.mailProject.email.dto.TaskRequest;
import com.mailProject.email.dto.TaskResponse;
import com.mailProject.email.entity.ReceivedEmails;
import com.mailProject.email.feignInterface.ClickupClient;
import com.mailProject.email.repository.MailJobHistoryRepository;
import com.mailProject.email.repository.MultipleEmailRepository;
import com.mailProject.email.repository.ReceiveEmailRepository;
import com.mailProject.email.service.MultipleEmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailProcessingService {

    private final MultipleEmailRepository accountRepo;
    private final ReceiveEmailRepository receiveRepo;
    private final MultipleEmailService emailService;
    private final ClickupClient clickupClient;
    private final MailJobHistoryRepository historyRepo;

    @Value("${clickup.list-id}")
    private String LIST_ID;

    @Transactional
    public int processNewMails(Long accountId) throws Exception {

        LocalDateTime lastRun = historyRepo
                .findTopByOrderByStartTimeDesc()
                .map(h -> {
                    log.info("Last job ended at: {}", h.getEndTime());
                    return h.getEndTime();
                })
                .orElse(LocalDateTime.now().minusDays(7));

        emailService.fetchInbox(accountId, 0, 100, "sentAt", "desc");
        List<ReceivedEmails> newMails =
                receiveRepo.findByAccountIdAndTaskCreatedFalse(accountId);

        log.info("Found {} new mails to process for account {}", newMails.size(), accountId);

        int count = 0;

        for (ReceivedEmails mail : newMails) {

            if (Boolean.TRUE.equals(mail.getTaskCreated())) {
                log.info("Skipping mail id={} because task already created", mail.getId());
                continue;
            }

            log.info("Processing mail id={}, subject={}", mail.getId(), mail.getSubject());

            try {
                TaskRequest req = new TaskRequest();

                String subject = mail.getSubject() != null && !mail.getSubject().isBlank()
                        ? mail.getSubject()
                        : "(No Subject)";

                req.setName(subject);
                String cleanBody = "";
                if (mail.getBody() != null) {
                    cleanBody = org.jsoup.Jsoup.parse(mail.getBody()).text();
                }
                if (cleanBody.length() > 3000) {
                    cleanBody = cleanBody.substring(0, 3000);
                }
                String description =
                        "📩 Account ID: " + accountId + "\n" +
                        "📩 Sender: " + mail.getSender() + "\n" +
                                "🕒 Date: " + mail.getSentAt() + "\n\n" +
                                "----------------------------------\n\n" +
                                cleanBody;

                req.setDescription(description);

                TaskResponse response = clickupClient.createTask(LIST_ID, req);
                log.info("ClickUp task created successfully! Task ID={}", response.getId());

                uploadAttachments(response.getId(), mail);

                mail.setTaskCreated(true);
                receiveRepo.save(mail);

                count++;

            } catch (Exception e) {
                log.error("❌ Failed to create ClickUp task for mail id {}: {}", mail.getId(), e.getMessage(), e);
            }
        }

        log.info("Total ClickUp tasks created in this run: {}", count);
        return count;
    }

    private void uploadAttachments(String taskId, ReceivedEmails mail) {

        if (mail.getAttachments() == null || mail.getAttachments().isEmpty()) {
            return;
        }

        String[] files = mail.getAttachments().split(",");

        for (String fileName : files) {
            try {
                File file = new File("email_attachments/" + fileName);
                if (!file.exists()) {
                    log.warn("Attachment file not found: {}", file.getAbsolutePath());
                    continue;
                }

                MultipartFile multipartFile =
                        new MockMultipartFile(
                                file.getName(),
                                file.getName(),
                                Files.probeContentType(file.toPath()),
                                new FileInputStream(file)
                        );

                clickupClient.uploadAttachment(taskId, multipartFile);
                log.info("Uploaded attachment {} for task {}", fileName, taskId);

            } catch (Exception e) {
                log.error("Attachment upload failed for mail id: {}. Error: {}", mail.getId(), e.getMessage(), e);
            }
        }
    }
}
