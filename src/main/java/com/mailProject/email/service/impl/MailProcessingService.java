package com.mailProject.email.service.impl;

import com.mailProject.email.dto.TaskRequest;
import com.mailProject.email.dto.TaskResponse;
import com.mailProject.email.entity.ClickupConfig;
import com.mailProject.email.entity.ReceivedEmails;
import com.mailProject.email.feignInterface.ClickupClient;
import com.mailProject.email.repository.ReceiveEmailRepository;
import com.mailProject.email.security.AESUtil;
import com.mailProject.email.security.ClickupContext;
import com.mailProject.email.service.ClickupConfigService;
import com.mailProject.email.service.MultipleEmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailProcessingService {

    private final ReceiveEmailRepository receiveRepo;
    private final MultipleEmailService emailService;
    private final ClickupClient clickupClient;
    private final ClickupConfigService clickupConfigService;

    @Transactional
    public int processNewMails(Long accountId) throws Exception {
        if (!clickupConfigService.isConfigured()) {
            log.warn("ClickUp not configured for account {}", accountId);
            return 0;
        }

        emailService.fetchInbox(accountId);
        List<ReceivedEmails> newMails = receiveRepo.findByAccountIdAndTaskCreatedFalse(accountId);
        int count = 0;

        for (ReceivedEmails mail : newMails) {
            try {
                ClickupConfig config = clickupConfigService.getConfig();
                String token = AESUtil.decrypt(config.getToken());
                String listId = config.getListId();

                if (listId == null || listId.isBlank()) {
                    log.warn("ListId missing, skipping task creation");
                    continue;
                }

                ClickupContext.setToken(token);

                TaskRequest req = new TaskRequest();
                req.setName(mail.getSubject() != null && !mail.getSubject().isBlank()
                        ? mail.getSubject()
                        : "(No Subject)");

                String cleanBody = mail.getBody() != null
                        ? org.jsoup.Jsoup.parse(mail.getBody()).text()
                        : "";

                if (cleanBody.length() > 3000) cleanBody = cleanBody.substring(0, 3000);

                req.setDescription("📩 Account ID: " + accountId +
                        "\n📩 Sender: " + mail.getSender() +
                        "\n🕒 Date: " + mail.getSentAt() +
                        "\n\n" + cleanBody);

                TaskResponse response = clickupClient.createTask(listId, req);

                uploadAttachments(response.getId(), mail);

                mail.setTaskCreated(true);
                receiveRepo.save(mail);
                count++;

            } catch (Exception e) {
                log.error("Error processing mail id {}: {}", mail.getId(), e.getMessage(), e);
            } finally {
                ClickupContext.clear();
            }
        }

        return count;
    }

    private void uploadAttachments(String taskId, ReceivedEmails mail) {
        if (mail.getAttachments() == null || mail.getAttachments().isEmpty()) return;

        String[] files = mail.getAttachments().split(",");
        for (String fileName : files) {
            try {
                File file = new File("email_attachments/" + fileName);
                if (!file.exists()) {
                    log.warn("Attachment file not found: {}", file.getAbsolutePath());
                    continue;
                }

                MultipartFile multipartFile = new MockMultipartFile(
                        file.getName(),
                        file.getName(),
                        Files.probeContentType(file.toPath()),
                        new FileInputStream(file)
                );

                clickupClient.uploadAttachment(taskId, multipartFile);
            } catch (Exception e) {
                log.error("Attachment upload failed for mail id: {}. Error: {}", mail.getId(), e.getMessage());
            }
        }
    }
}