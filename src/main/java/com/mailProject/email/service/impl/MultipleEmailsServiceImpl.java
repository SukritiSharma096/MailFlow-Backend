package com.mailProject.email.service.impl;

import com.mailProject.email.dto.*;
import com.mailProject.email.entity.MultipleEmailAccounts;
import com.mailProject.email.entity.ReceivedEmails;
import com.mailProject.email.entity.SentMails;
import com.mailProject.email.repository.MultipleEmailRepository;
import com.mailProject.email.repository.ReceiveEmailRepository;
import com.mailProject.email.repository.SentEmailRepository;
import com.mailProject.email.service.MultipleEmailService;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultipleEmailsServiceImpl implements MultipleEmailService {

    @Autowired
    private SentEmailRepository sentMailRepository;


    @Autowired
    private ReceiveEmailRepository receiveEmailRepository;

    private final MultipleEmailRepository multipleEmailRepository;

    public MultipleEmailsServiceImpl(MultipleEmailRepository multipleEmailRepository) {
        this.multipleEmailRepository = multipleEmailRepository;
    }

    private MultipleEmailResponse toResponse(MultipleEmailAccounts e) {
        MultipleEmailResponse r = new MultipleEmailResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setUsername(e.getUsername());
        r.setSmtpHost(e.getSmtpHost());
        r.setSmtpPort(e.getSmtpPort());
        r.setImapHost(e.getImapHost());
        r.setImapPort(e.getImapPort());
        r.setImapSsl(e.getImapSsl());
        r.setSmtpTls(e.getSmtpTls());
        r.setProtocol(e.getProtocol());
        r.setActive(e.getActive());
        return r;
    }

    @Override
    public MultipleEmailResponse createAccount(MultipleEmailRequest request) {

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password must not be empty or blank");
        }

        if (multipleEmailRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        MultipleEmailAccounts e = new MultipleEmailAccounts();
        e.setName(request.getName());
        e.setUsername(request.getUsername());
        e.setPassword(request.getPassword().trim());
        e.setSmtpHost(request.getSmtpHost());
        e.setSmtpPort(request.getSmtpPort());
        e.setImapHost(request.getImapHost());
        e.setImapPort(request.getImapPort());
        e.setImapSsl(request.getImapSsl() == null ? true : request.getImapSsl());
        e.setSmtpTls(request.getSmtpTls() == null ? true : request.getSmtpTls());
        e.setProtocol(request.getProtocol());
        e.setActive(request.getActive());
        e = multipleEmailRepository.save(e);
        return toResponse(e);
    }


    @Override
    public List<MultipleEmailResponse> listAccounts() {
        return multipleEmailRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MultipleEmailResponse getAccount(Long id) {
        MultipleEmailAccounts e = multipleEmailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return toResponse(e);
    }

    @Override
    public MultipleEmailResponse updateAccount(Long id, MultipleEmailRequest request) {
        MultipleEmailAccounts e = multipleEmailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        e.setName(request.getName());
        e.setUsername(request.getUsername());
        if (request.getPassword() != null && StringUtils.hasText(request.getPassword())) {
            e.setPassword(request.getPassword().trim());
        }
        e.setSmtpHost(request.getSmtpHost());
        e.setSmtpPort(request.getSmtpPort());
        e.setImapHost(request.getImapHost());
        e.setImapPort(request.getImapPort());
        e.setImapSsl(request.getImapSsl());
        e.setSmtpTls(request.getSmtpTls());
        e.setProtocol(request.getProtocol());
        e.setActive(request.getActive());

        e = multipleEmailRepository.save(e);
        return toResponse(e);
    }

    @Override
    public void deleteAccount(Long id) {
        multipleEmailRepository.deleteById(id);
    }

    private Session createSmtpSession(MultipleEmailAccounts acc) {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", acc.getSmtpHost());
        props.put("mail.smtp.port", acc.getSmtpPort());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", acc.getSmtpTls());
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        return Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(acc.getUsername(), acc.getPassword());
            }
        });
    }

    @Override
    public void sendEmail(Long accountId, SendEmailRequest request) throws Exception {
        MultipleEmailAccounts acc = multipleEmailRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Session session = createSmtpSession(acc);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(acc.getUsername()));

        for (String r : request.getTo()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(r));
        }

        message.setSubject(request.getSubject());
        message.setText(request.getBody());

        Transport.send(message);
    }

    @Override
    public void sendEmailWithAttachments(Long accountId, SendEmailRequest request) throws Exception {

        MultipleEmailAccounts acc = multipleEmailRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Session session = createSmtpSession(acc);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(acc.getUsername()));

        for (String r : request.getTo()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(r));
        }

        message.setSubject(request.getSubject());
        Multipart multipart = new MimeMultipart();
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText(request.getBody());
        multipart.addBodyPart(bodyPart);
        List<String> attachmentNames = new ArrayList<>();

        if (request.getAttachments() != null) {
            for (MultipartFile mf : request.getAttachments()) {
                if (!mf.isEmpty()) {
                    MimeBodyPart filePart = new MimeBodyPart();
                    File f = convertMultipartToFile(mf);
                    filePart.attachFile(f);
                    multipart.addBodyPart(filePart);

                    attachmentNames.add(mf.getOriginalFilename());
                }
            }
        }

        message.setContent(multipart);
        Transport.send(message);
        SentMails sentMail = new SentMails();
        sentMail.setAccountId(accountId);
        sentMail.setToEmails(String.join(",", request.getTo()));
        sentMail.setSubject(request.getSubject());
        sentMail.setBody(request.getBody());
        sentMail.setSentAt(LocalDateTime.now());

        if (!attachmentNames.isEmpty()) {
            sentMail.setAttachments(String.join(",", attachmentNames));
        }

        sentMailRepository.save(sentMail);
    }

    private File convertMultipartToFile(MultipartFile file) throws Exception {
        File conv = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(conv);
        return conv;
    }

    private Store createImapStore(MultipleEmailAccounts acc) throws Exception {
        Properties props = new Properties();

        props.put("mail.store.protocol", acc.getProtocol());
        props.put("mail.imap.host", acc.getImapHost());
        props.put("mail.imap.port", acc.getImapPort());

        if (acc.getImapSsl()) {
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.starttls.enable", "true");
        }
        Session session = Session.getInstance(props);
        Store store = session.getStore(acc.getProtocol());
        store.connect(acc.getImapHost(), acc.getUsername(), acc.getPassword());
        return store;
    }

    @Override
    public List<ReceiveEmailResponse> fetchInbox(Long accountId) throws Exception {

        MultipleEmailAccounts acc = multipleEmailRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Store store = createImapStore(acc);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        int totalMessages = inbox.getMessageCount();

        if (totalMessages == 0) {
            inbox.close(false);
            store.close();
            return List.of();
        }

        Message[] messages = inbox.getMessages(1, totalMessages);
        List<ReceiveEmailResponse> list = new ArrayList<>();

        for (Message msg : messages) {
            MimeMessage mimeMessage = (MimeMessage) msg;
            String messageId = mimeMessage.getMessageID();
            ReceivedEmails email = receiveEmailRepository
                    .findByMessageId(messageId)
                    .orElseGet(() -> {
                        ReceivedEmails e = new ReceivedEmails();
                        e.setMessageId(messageId);

                        try {
                            e.setSender(msg.getFrom() != null ? msg.getFrom()[0].toString() : null);
                            e.setReceivers(String.join(",", getReceivers(msg)));
                            e.setSubject(msg.getSubject());
                            e.setBody(getBody(msg));
                            e.setAttachments(String.join(",", getAttachments(msg)));
                            Map<String, String> inlineImages = extractInlineImages(msg);
                            if (!inlineImages.isEmpty()) {
                                e.setInlineImages(
                                        new com.fasterxml.jackson.databind.ObjectMapper()
                                                .writeValueAsString(inlineImages)
                                );
                            }
                            e.setSentAt(
                                    msg.getSentDate() != null
                                            ? msg.getSentDate().toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime()
                                            : null);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        e.setReceived(true);
                        e.setFolder("INBOX");
                        e.setAccountId(accountId);
                        return receiveEmailRepository.save(e);
                    });

            ReceiveEmailResponse dto = new ReceiveEmailResponse();
            dto.setId(email.getId());
            dto.setSender(email.getSender());
            dto.setReceiver(
                    email.getReceivers() != null
                            ? List.of(email.getReceivers().split(","))
                            : List.of()
            );
            dto.setSubject(email.getSubject());
            dto.setBody(email.getBody());
            dto.setSentAt(email.getSentAt());
            dto.setReceived(true);
            dto.setFolder("INBOX");
            dto.setAttachments(
                    email.getAttachments() != null
                            ? List.of(email.getAttachments().split(","))
                            : List.of()
            );
            if (email.getInlineImages() != null) {
                dto.setInlineImages(
                        new com.fasterxml.jackson.databind.ObjectMapper()
                                .readValue(
                                        email.getInlineImages(),
                                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
                                )
                );
            } else {
                dto.setInlineImages(Map.of());
            }
            list.add(dto);
        }
        inbox.close(false);
        store.close();
        return list;
    }



    private List<String> getReceivers(Message msg) throws MessagingException {
        List<String> list = new ArrayList<>();
        Address[] arr = msg.getAllRecipients();
        if (arr != null) for (Address a : arr) list.add(a.toString());
        return list;
    }

    private String getBody(Message msg) throws Exception {
        if (msg.isMimeType("text/plain") || msg.isMimeType("text/html")) {
            return msg.getContent().toString();
        }
        if (msg.isMimeType("multipart/*")) {
            return extractMultipart((Multipart) msg.getContent());
        }
        return "";
    }

    private String extractMultipart(Multipart mp) throws Exception {
        String plainText = null;

        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart part = mp.getBodyPart(i);

            if (part.isMimeType("text/html")) {
                return part.getContent().toString();
            } else if (part.isMimeType("text/plain") && plainText == null) {
                plainText = part.getContent().toString();
            } else if (part.getContent() instanceof Multipart) {
                String nested = extractMultipart((Multipart) part.getContent());
                if (nested != null && !nested.isEmpty()) return nested;
            }
        }
        return plainText != null ? plainText : "";
    }


    private List<String> getAttachments(Message msg) throws Exception {

        List<String> attachments = new ArrayList<>();

        if (!msg.isMimeType("multipart/*")) {
            return attachments;
        }
        Multipart mp = (Multipart) msg.getContent();

        for (int i = 0; i < mp.getCount(); i++) {

            BodyPart part = mp.getBodyPart(i);

            String disposition = part.getDisposition();
            String filename = part.getFileName();
            if (disposition != null &&
                    disposition.equalsIgnoreCase(MimeBodyPart.INLINE)) {
                continue;
            }
            if (filename != null &&
                    disposition != null &&
                    disposition.equalsIgnoreCase(MimeBodyPart.ATTACHMENT)) {

                attachments.add(filename);

                Path folder = Paths.get("email_attachments");
                if (!Files.exists(folder)) Files.createDirectories(folder);

                Path filePath = folder.resolve(filename);

                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        return attachments;
    }

    @Override
    public void moveGoogleMails(Long id) { moveByFilter(id, "google.com", "Google"); }
    @Override
    public void moveYouTubeMails(Long id) { moveByFilter(id, "youtube.com", "YouTube"); }
    @Override
    public void moveNetflixMails(Long id) { moveByFilter(id, "netflix.com", "Netflix"); }
    @Override
    public void movePokemonGoMails(Long id) { moveByFilter(id, "email.nianticlabs.com", "PokemonGo"); }
    @Override
    public void moveHDFCBankMails(Long id){ moveByFilter(id, "mailers.hdfcbank.net", "HDFC"); }
    @Override
    public void moveLinkedInMails(Long id){ moveByFilter(id, "linkedin.com", "LinkedIn"); }


    private void moveByFilter(Long accountId, String domain, String label) {

        MultipleEmailAccounts acc = multipleEmailRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        try {
            Store store = createImapStore(acc);

            IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();

            for (Message msg : messages) {
                String from = msg.getFrom()[0].toString().toLowerCase();
                if (from.contains(domain)) {
                    move(inbox, msg, label);
                }
            }

            inbox.close(true);
            store.close();

        } catch (Exception ex) {
            throw new RuntimeException("Error moving mails: " + ex.getMessage());
        }
    }

    private void move(IMAPFolder inbox, Message msg, String targetFolder) throws Exception {

        Folder target = inbox.getStore().getFolder(targetFolder);

        if (!target.exists()) {
            target.create(Folder.HOLDS_MESSAGES);
        }

        target.open(Folder.READ_WRITE);

        inbox.copyMessages(new Message[]{msg}, target);
        msg.setFlag(Flags.Flag.DELETED, true);
        inbox.expunge();

        target.close(true);
    }


    @Override
    @Transactional
    public boolean deleteEmailFromDb(Long accountId, Long emailId) {
        ReceivedEmails email = receiveEmailRepository
                .findByIdAndAccountId(emailId, accountId)
                .orElseThrow(() -> new RuntimeException("Email not found with ID: " + emailId));
        MultipleEmailAccounts account = multipleEmailRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", account.getProtocol());
            props.put("mail.imap.host", account.getImapHost());
            props.put("mail.imap.port", account.getImapPort());

            if (Boolean.TRUE.equals(account.getImapSsl())) {
                props.put("mail.imap.ssl.enable", "true");
            }

            Session session = Session.getInstance(props);
            Store store = session.getStore(account.getProtocol());
            store.connect(account.getImapHost(), account.getUsername(), account.getPassword());
            Folder inbox = store.getFolder(email.getFolder() != null ? email.getFolder() : "INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
                MimeMessage mm = (MimeMessage) msg;

                if (mm.getMessageID() != null &&
                        mm.getMessageID().equals(email.getMessageId())) {

                    msg.setFlag(Flags.Flag.DELETED, true);
                    break;
                }
            }

            inbox.close(true);
            store.close();
            receiveEmailRepository.delete(email);

            return true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete email from server", e);
        }
    }


    @Override
    public boolean verifyPassword(String username, String password) {

        MultipleEmailAccounts account =
                multipleEmailRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("Email account not found"));

        return account.getPassword().equals(password);
    }


    @Override
    public ReceiveEmailResponse getEmailById(Long id) {

        ReceivedEmails e = receiveEmailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        ReceiveEmailResponse dto = new ReceiveEmailResponse();
        dto.setId(e.getId());
        dto.setSender(e.getSender());
        dto.setReceiver(
                e.getReceivers() != null
                        ? Arrays.asList(e.getReceivers().split(","))
                        : List.of()
        );
        dto.setSubject(e.getSubject());
        dto.setBody(e.getBody());
        dto.setSentAt(e.getSentAt());
        dto.setFolder(e.getFolder());
        dto.setReceived(e.isReceived());
        dto.setAttachments(
                e.getAttachments() != null
                        ? Arrays.asList(e.getAttachments().split(","))
                        : List.of()
        );

        if (e.getInlineImages() != null) {
            try {
                dto.setInlineImages(
                        new com.fasterxml.jackson.databind.ObjectMapper()
                                .readValue(
                                        e.getInlineImages(),
                                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
                                )
                );
            } catch (Exception ex) {
                dto.setInlineImages(Map.of());
            }
        } else {
            dto.setInlineImages(Map.of());
        }

        return dto;
    }


    public List<ReceiveEmailResponse> getInboxFromDb(Long accountId, String folder) {

        List<ReceivedEmails> emails =
                receiveEmailRepository
                        .findTop20ByAccountIdAndFolderOrderBySentAtDesc(accountId, folder);

        return emails.stream().map(e -> {
            ReceiveEmailResponse dto = new ReceiveEmailResponse();
            dto.setId(e.getId());
            dto.setSender(e.getSender());
            dto.setReceiver(
                    e.getReceivers() != null
                            ? Arrays.asList(e.getReceivers().split(","))
                            : List.of()
            );
            dto.setSubject(e.getSubject());
            dto.setBody(e.getBody());
            dto.setSentAt(e.getSentAt());
            dto.setReceived(e.isReceived());
            dto.setFolder(e.getFolder());
            dto.setAttachments(
                    e.getAttachments() != null
                            ? Arrays.asList(e.getAttachments().split(","))
                            : List.of()
            );
            return dto;
        }).toList();
    }


    private Map<String, String> extractInlineImages(Message msg) throws Exception {

        Map<String, String> inlineImages = new HashMap<>();

        if (!msg.isMimeType("multipart/*")) return inlineImages;

        Multipart mp = (Multipart) msg.getContent();

        for (int i = 0; i < mp.getCount(); i++) {

            BodyPart part = mp.getBodyPart(i);

            String disposition = part.getDisposition();
            String[] cids = part.getHeader("Content-ID");

            if (disposition != null
                    && disposition.equalsIgnoreCase(MimeBodyPart.INLINE)
                    && cids != null) {

                String cid = cids[0].replace("<", "").replace(">", "");

                String ext = part.getContentType().split("/")[1].split(";")[0];
                String fileName = cid + "." + ext;

                Path folder = Paths.get("email_attachments");
                if (!Files.exists(folder)) Files.createDirectories(folder);

                Path filePath = folder.resolve(fileName);

                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                inlineImages.put(cid, fileName);
            }
        }
        return inlineImages;
    }

    @Override
    public void forwardEmail(
            Long accountId,
            Long emailId,
            List<String> to
    ) throws Exception {

        MultipleEmailAccounts acc = multipleEmailRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        ReceivedEmails email = receiveEmailRepository
                .findByIdAndAccountId(emailId, accountId)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        Store store = createImapStore(acc);
        Folder inbox = store.getFolder(email.getFolder());
        inbox.open(Folder.READ_ONLY);

        for (Message msg : inbox.getMessages()) {
            MimeMessage original = (MimeMessage) msg;

            if (email.getMessageId().equals(original.getMessageID())) {

                Session smtpSession = createSmtpSession(acc);

                MimeMessage forward = new MimeMessage(smtpSession);
                forward.setFrom(new InternetAddress(acc.getUsername()));

                for (String r : to) {
                    forward.addRecipient(Message.RecipientType.TO, new InternetAddress(r));
                }
                forward.setSubject("Fwd: " + original.getSubject());
                forward.setContent(original.getContent(), original.getContentType());
                Transport.send(forward);
                break;
            }
        }

        inbox.close(false);
        store.close();
    }

    @Override
    @Transactional
    public boolean deleteSentEmailFromDb(Long accountId, Long emailId) {

        SentMails sentMail = sentMailRepository
                .findById(emailId)
                .orElseThrow(() -> new RuntimeException("Sent email not found with ID: " + emailId));

        if (!sentMail.getAccountId().equals(accountId)) {
            throw new RuntimeException("Sent email does not belong to this account");
        }

        sentMailRepository.delete(sentMail);

        return true;
    }

}