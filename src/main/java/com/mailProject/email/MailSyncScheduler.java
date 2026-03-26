package com.mailProject.email;

import com.mailProject.email.entity.MultipleEmailAccounts;
import com.mailProject.email.repository.MultipleEmailRepository;
import com.mailProject.email.service.MultipleEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MailSyncScheduler {

    @Autowired
    private MultipleEmailRepository multipleEmailRepository;

    @Autowired
    private MultipleEmailService multipleEmailService;


    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void syncInboxMails() {

        List<MultipleEmailAccounts> accounts =
                multipleEmailRepository.findAll()
                        .stream()
                        .filter(MultipleEmailAccounts::getActive)
                        .toList();

        for (MultipleEmailAccounts acc : accounts) {
            try {
                multipleEmailService.fetchAllAccountsInbox(0, 100, "sentAt", "desc");
                System.out.println("All accounts synced successfully");
            } catch (Exception e) {
                System.err.println("Global mail sync failed: " + e.getMessage());
            }
        }
    }
}
