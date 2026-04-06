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

    public int processNewMails(Long accountId, boolean fetchInbox) throws Exception {

        if (!clickupConfigService.isConfigured(accountId)) {
            log.warn("ClickUp not configured for account {}", accountId);
            return 0;
        }

        if (fetchInbox) {
            emailService.fetchInbox(accountId);
        }

        List<ReceivedEmails> newMails =
                receiveRepo.findTop50ByAccountIdAndTaskCreatedFalseAndProcessingFalse(accountId);

        if (newMails.isEmpty()) return 0;
        ClickupConfig global = clickupConfigService.getGlobal();
        var mapping = clickupConfigService.getAccountConfig(accountId);
        String token = AESUtil.decrypt(global.getToken());
        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);

        newMails.parallelStream().forEach(mail -> {
            try {
                ClickupContext.setToken(token);
                mail.setProcessing(true);
                receiveRepo.save(mail);
                String[] receiverList = mail.getReceivers() != null
                        ? mail.getReceivers().split(",")
                        : new String[]{};

                if (receiverList.length == 0) {
                    receiverList = new String[]{"UNKNOWN"};
                }

                String cleanBody = mail.getBody() != null
                        ? org.jsoup.Jsoup.parse(mail.getBody()).text()
                        : "";

                if (cleanBody.length() > 3000) {
                    cleanBody = cleanBody.substring(0, 3000);
                }

                for (String receiver : receiverList) {

                    TaskRequest req = new TaskRequest();

                    req.setName(
                            mail.getSubject() != null && !mail.getSubject().isBlank()
                                    ? mail.getSubject()
                                    : "(No Subject)"
                    );

                    req.setDescription(
                            "Account ID: " + accountId +
                                    "\nSender: " + mail.getSender() +
                                    "\nReceiver: " + receiver.trim() +
                                    "\n\n" + cleanBody
                    );

                    TaskResponse response =
                            clickupClient.createTask(mapping.getListId(), req);
                    uploadAttachments(response.getId(), mail);
                    count.incrementAndGet();
                }

                mail.setTaskCreated(true);
                mail.setProcessing(false);
                receiveRepo.save(mail);

            } catch (Exception e) {

                mail.setProcessing(false);
                receiveRepo.save(mail);

                log.error("Error processing mail {}: {}", mail.getId(), e.getMessage());

            } finally {
                ClickupContext.clear();
            }
        });

        return count.get();
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