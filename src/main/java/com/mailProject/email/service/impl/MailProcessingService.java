package com.mailProject.email.service.impl;

import com.mailProject.email.dto.TaskRequest;
import com.mailProject.email.dto.TaskResponse;
import com.mailProject.email.entity.ClickupConfig;
import com.mailProject.email.entity.ReceivedEmails;
import com.mailProject.email.exception.ClickupListDeletedException;
import com.mailProject.email.exception.ClickupListNotSelectedException;
import com.mailProject.email.exception.ClickupSpaceNotSelectedException;
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
    public int processNewMails(Long accountId, boolean fetchInbox) throws Exception {

        if (!clickupConfigService.isConfigured()) {
            log.warn("ClickUp not configured for account {}", accountId);
            return 0;
        }

        if (fetchInbox) {
            emailService.fetchInbox(accountId);
        }

        List<ReceivedEmails> newMails =
                receiveRepo.findTop50ByAccountIdAndTaskCreatedFalse(accountId);

        int count = 0;

        for (ReceivedEmails mail : newMails) {

            try {
                ClickupConfig config = clickupConfigService.getConfig();

                String listId = config.getListId();
                if (config.getSpaceId() == null || config.getSpaceId().isBlank()) {
                    throw new ClickupSpaceNotSelectedException("ClickUp Space not selected");
                }

                if (config.getListId() == null || config.getListId().isBlank()) {
                    throw new ClickupListNotSelectedException("ClickUp List not selected");
                }

                String token = AESUtil.decrypt(config.getToken());
                ClickupContext.setToken(token);

                TaskRequest req = new TaskRequest();
                req.setName(
                        mail.getSubject() != null && !mail.getSubject().isBlank()
                                ? mail.getSubject()
                                : "(No Subject)"
                );

                String cleanBody = mail.getBody() != null
                        ? org.jsoup.Jsoup.parse(mail.getBody()).text()
                        : "";

                if (cleanBody.length() > 3000)
                    cleanBody = cleanBody.substring(0, 3000);

                req.setDescription(
                        " Account ID: " + accountId +
                                "\nSender: " + mail.getSender() +
                                "\nDate: " + mail.getSentAt() +
                                "\n\n" + cleanBody
                );

                mail.setTaskCreated(true);
                receiveRepo.save(mail);

                try {
                    TaskResponse response = clickupClient.createTask(listId, req);

                    uploadAttachments(response.getId(), mail);

                    count++;

                } catch (Exception e) {

                    if (e.getMessage() != null && e.getMessage().contains("List deleted")) {

                        log.error(" ClickUp list deleted!");

                        config.setListId(null);
                        clickupConfigService.saveConfig(config);

                        throw new ClickupListDeletedException("Selected ClickUp list is deleted. Please reconfigure.");
                    }

                    else if (e.getMessage() != null && e.getMessage().contains("duplicate")) {
                        log.warn("Duplicate ignored for mail {}", mail.getId());
                    }

                    else {
                        log.error("Error mail {}: {}", mail.getId(), e.getMessage());

                        mail.setTaskCreated(false);
                        receiveRepo.save(mail);
                    }
                }

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
                    log.warn("Attachment not found: {}", file.getAbsolutePath());
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
                log.error("Attachment upload failed for mail {}: {}", mail.getId(), e.getMessage());
            }
        }
    }
}