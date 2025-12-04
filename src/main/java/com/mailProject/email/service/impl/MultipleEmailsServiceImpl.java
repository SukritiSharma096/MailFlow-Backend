package com.mailProject.email.service.impl;

import com.mailProject.email.dto.*;
import com.mailProject.email.entity.MultipleEmailAccounts;
import com.mailProject.email.entity.ReceivedEmails;
import com.mailProject.email.repository.MultipleEmailRepository;
import com.mailProject.email.repository.ReceiveEmailRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultipleEmailsServiceImpl implements MultipleEmailService {

    @Autowired
    private ReceiveEmailRepository receiveEmailRepository;

    private final MultipleEmailRepository multipleEmailRepository;

    public MultipleEmailsServiceImpl(MultipleEmailRepository multipleEmailRepository) {
        this.multipleEmailRepository = multipleEmailRepository;
    }

    // -------------------- Convert Entity → Response DTO --------------------
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
        MultipleEmailAccounts e = new MultipleEmailAccounts();
        e.setName(request.getName());
        e.setUsername(request.getUsername());
        e.setPassword(request.getPassword());
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
        if (request.getPassword() != null) e.setPassword(request.getPassword());
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


    // SMTP (Send Email)

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

        if (request.getAttachments() != null) {
            for (MultipartFile mf : request.getAttachments()) {
                if (!mf.isEmpty()) {
                    MimeBodyPart filePart = new MimeBodyPart();
                    File f = convertMultipartToFile(mf);
                    filePart.attachFile(f);
                    multipart.addBodyPart(filePart);
                }
            }
        }

        message.setContent(multipart);
        Transport.send(message);
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

        Message[] messages = inbox.getMessages();
        List<ReceiveEmailResponse> list = new ArrayList<>();

        for (Message msg : messages) {

            ReceivedEmails e = new ReceivedEmails();
            e.setSender(msg.getFrom()[0].toString());
            e.setReceivers(String.join(",", getReceivers(msg)));
            e.setSubject(msg.getSubject());
            e.setBody(getBody(msg));
            e.setSentAt(msg.getSentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            e.setReceived(true);
            e.setAttachments(String.join(",", getAttachments(msg)));
            e.setFolder("INBOX");
            e.setAccountId(accountId);

            receiveEmailRepository.save(e);

            ReceiveEmailResponse dto = new ReceiveEmailResponse();
            dto.setId(e.getId());
            dto.setSender(e.getSender());
            dto.setReceiver(getReceivers(msg));
            dto.setSubject(e.getSubject());
            dto.setBody(e.getBody());
            dto.setSentAt(e.getSentAt());
            dto.setReceived(true);
            dto.setAttachments(getAttachments(msg));
            dto.setFolder("INBOX");

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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart part = mp.getBodyPart(i);
            if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
                sb.append(part.getContent());
            } else if (part.getContent() instanceof Multipart) {
                sb.append(extractMultipart((Multipart) part.getContent()));
            }
        }
        return sb.toString();
    }

    private List<String> getAttachments(Message msg) throws Exception {
        List<String> f = new ArrayList<>();
        if (msg.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) msg.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart p = mp.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition()) || p.getFileName() != null) {
                    f.add(p.getFileName());
                }
            }
        }
        return f;
    }


    // MOVE FILTER

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
                .orElseThrow(() -> new RuntimeException(
                        "Email not found with ID: " + emailId + " for Account ID: " + accountId
                ));

        receiveEmailRepository.delete(email);
        return true;
    }


}
