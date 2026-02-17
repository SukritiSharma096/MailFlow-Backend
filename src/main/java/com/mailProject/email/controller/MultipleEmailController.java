package com.mailProject.email.controller;

import com.mailProject.email.dto.*;
import com.mailProject.email.entity.SentMails;
import com.mailProject.email.repository.MultipleEmailRepository;
import com.mailProject.email.repository.ReceiveEmailRepository;
import com.mailProject.email.repository.SentEmailRepository;
import com.mailProject.email.service.MultipleEmailService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin("*")
public class MultipleEmailController {

    @Autowired
    private SentEmailRepository sentEmailRepository;

    @Autowired
    private MultipleEmailRepository multipleEmailRepository;

    @Autowired
    private ReceiveEmailRepository receiveEmailRepository;

    private final MultipleEmailService service;

    @Autowired
    public MultipleEmailController(MultipleEmailService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<?> create(@RequestBody MultipleEmailRequest req) {
        if (req.getPassword() == null || !StringUtils.hasText(req.getPassword())) {
            return ResponseEntity.badRequest().body("Password is required and must not be blank");
        }
        try {
            MultipleEmailResponse resp = service.createAccount(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to create account: " + ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<MultipleEmailResponse>> list() {
        return ResponseEntity.ok(service.listAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MultipleEmailResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAccount(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MultipleEmailRequest req) {
        try {
            MultipleEmailResponse resp = service.updateAccount(id, req);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update account: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<String> send(@PathVariable Long id, @RequestBody SendEmailRequest req) {
        try {
            service.sendEmail(id, req);
            return ResponseEntity.ok("Email sent");
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Send failed: " + ex.getMessage());
        }
    }

    @PostMapping(value = "/sendWithAttachments/{accountId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendEmailWithAttachments(
            @PathVariable Long accountId,
            @ModelAttribute SendEmailRequest request) {

        try {
            service.sendEmailWithAttachments(accountId, request);
            return ResponseEntity.ok("Email sent successfully with multiple attachments!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed: " + e.getMessage());
        }
    }

    @GetMapping("/{accountId}/sent")
    public List<SentMails> getSentMails(@PathVariable Long accountId) {
        return sentEmailRepository.findByAccountIdOrderBySentAtDesc(accountId);
    }

    @GetMapping("/{accountId}/sent/{mailId}")
    public SentMails getSentMailById(
            @PathVariable Long accountId,
            @PathVariable Long mailId) {

        return sentEmailRepository.findById(mailId)
                .orElseThrow(() -> new RuntimeException("Sent mail not found"));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inbox/{accountId}")
    public ResponseEntity<?> getInbox(@PathVariable Long accountId) {
        try {
            return ResponseEntity.ok(service.fetchInbox(accountId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/move/google/{id}")
    public ResponseEntity<?> moveGoogle(@PathVariable Long id) {
        service.moveGoogleMails(id);
        return ResponseEntity.ok("Google mails moved!");
    }

    @PostMapping("/move/youtube/{id}")
    public ResponseEntity<?> moveYouTube(@PathVariable Long id) {
        service.moveYouTubeMails(id);
        return ResponseEntity.ok("YouTube mails moved!");
    }

    @PostMapping("/move/netflix/{id}")
    public ResponseEntity<?> moveNetflix(@PathVariable Long id) {
        service.moveNetflixMails(id);
        return ResponseEntity.ok("Netflix mails moved!");
    }

    @PostMapping("/move/pokemongo/{id}")
    public ResponseEntity<?> movePokemonGo(@PathVariable Long id) {
        service.movePokemonGoMails(id);
        return ResponseEntity.ok("PokemonGo mails moved!");
    }

    @PostMapping("/move/hdfc/{id}")
    public ResponseEntity<?> moveHDFCBankMails(@PathVariable Long id) {
        service.moveHDFCBankMails(id);
        return ResponseEntity.ok("HDFC bank mails moved");
    }

    @PostMapping("/move/linkedIn/{id}")
    public ResponseEntity<?> moveLinkedInMails(@PathVariable Long id) {
        service.moveLinkedInMails(id);
        return ResponseEntity.ok("Linkedin mails moved");
    }

    @DeleteMapping("/{accountId}/delete/{emailId}")
    public ResponseEntity<String> deleteEmail(
            @PathVariable Long accountId,
            @PathVariable Long emailId) {

        service.deleteEmailFromDb(accountId, emailId);
        return ResponseEntity.ok(
                "Email with ID " + emailId + " deleted successfully from account " + accountId
        );
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(
            @RequestBody MultipleEmailRequest request) {

        boolean valid = service.verifyPassword(request.getUsername(), request.getPassword());

        if (valid) {
            return ResponseEntity.ok("Password verified");
        } else {
            return ResponseEntity.badRequest().body("Invalid password");
        }
    }

    @GetMapping("/email/{id}")
    public ResponseEntity<?> getEmailById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEmailById(id));
    }

    @GetMapping("mail/attachments")
    public void getAttachment(
            @RequestParam String file,
            HttpServletResponse response) throws Exception {

        Path filePath = Paths.get("email_attachments").resolve(file);

        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setHeader(
                "Content-Disposition",
                "inline; filename=\"" + file + "\""
        );

        try (InputStream in = Files.newInputStream(filePath)) {
            IOUtils.copy(in, response.getOutputStream());
        }
    }


    @GetMapping("/inbox/db/{accountId}")
    public ResponseEntity<?> getInboxFromDb(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "INBOX") String folder) {

        return ResponseEntity.ok(
                service.getInboxFromDb(accountId, folder)
        );
    }

    @GetMapping("/inbox/fetch/{accountId}")
    public ResponseEntity<?> fetchInbox(@PathVariable Long accountId) {
        try {
            List<ReceiveEmailResponse> inbox = service.fetchInbox(accountId);
            return ResponseEntity.ok(inbox);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to fetch inbox: " + e.getMessage());
        }
    }

    @PostMapping("/{accountId}/sync")
    public ResponseEntity<?> manualSyncInbox(@PathVariable Long accountId) {
        try {
            List<ReceiveEmailResponse> inbox = service.fetchInbox(accountId);
            return ResponseEntity.ok(inbox);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to sync inbox: " + e.getMessage());
        }
    }

    @PostMapping("/{accountId}/forward/{emailId}")
    public ResponseEntity<?> forwardMail(
            @PathVariable Long accountId,
            @PathVariable Long emailId,
            @RequestBody List<String> to
    ) throws Exception {

        service.forwardEmail(accountId, emailId, to);
        return ResponseEntity.ok("Mail forwarded successfully");
    }

    @DeleteMapping("/{accountId}/sent/delete/{emailId}")
    public ResponseEntity<String> deleteSentEmail(
            @PathVariable Long accountId,
            @PathVariable Long emailId) {

        service.deleteSentEmailFromDb(accountId, emailId);
        return ResponseEntity.ok(
                "Sent email with ID " + emailId + " deleted successfully from account " + accountId
        );
    }

    @GetMapping("/inbox/all")
    public ResponseEntity<?> fetchAllAccountsInbox() {
        try {
            return ResponseEntity.ok(service.fetchAllAccountsInbox());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to fetch all inboxes: " + e.getMessage());
        }
    }

}
