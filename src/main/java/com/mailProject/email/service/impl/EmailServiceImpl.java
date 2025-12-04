package com.mailProject.email.service.impl;

import com.mailProject.email.entity.Email;
import com.mailProject.email.entity.MessageMail;
import com.mailProject.email.repository.EmailRepo;
import com.mailProject.email.service.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    private EmailRepo emailRepo;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${mail.store.protocol}")
    String protocol;

    @Value("${mail.imap.host}")
    String host;

    @Value("${mail.imap.port}")
    String port;

    @Value("${spring.mail.username}")
    String username;

    @Value("${spring.mail.password}")
    String password;

    // -------------------- SEND EMAILS --------------------

    @Override
    public void sendEmail(String to, String subject, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        simpleMailMessage.setFrom(username);
        mailSender.send(simpleMailMessage);
    }

    @Override
    public void sendMultipleEmail(String[] toMultiple, String subject, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(toMultiple);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        simpleMailMessage.setFrom(username);
        mailSender.send(simpleMailMessage);
    }

    @Override
    public void sendEmailWithFile(String to, String subject, String message, File file) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(username);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message);
            FileSystemResource resource = new FileSystemResource(file);
            helper.addAttachment(resource.getFilename(), file);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailWIthHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(username);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- FETCH INBOX --------------------

    @Override
    public List<MessageMail> getInboxMessage() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", protocol);
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", port);

        List<MessageMail> list = new ArrayList<>();

        try {
            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            for (Message message : messages) {

                String from = message.getFrom()[0].toString();
                String subject = message.getSubject();

                if (emailRepo.existsBySenderAndSubject(from, subject)) continue;

                String content = getContentFromEmail(message);
                List<String> files = getFiles(message);

                Email email = new Email();
                email.setSender(from);
                email.setReceiver(username);
                email.setSubject(subject);
                email.setBody(content);
                email.setSentAt(LocalDateTime.now());
                email.setReceived(true);
                email.setFiles(files);

                // messageId
                if (message instanceof MimeMessage) {
                    email.setMessageId(((MimeMessage) message).getMessageID());
                }

                emailRepo.save(email);

                list.add(MessageMail.builder()
                        .from(from)
                        .subject(subject)
                        .content(content)
                        .files(files)
                        .build());
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }


    private String getContentFromEmail(Message message) throws Exception {
        if (message.isMimeType("text/plain"))
            return message.getContent().toString();

        if (message.isMimeType("text/html"))
            return Jsoup.parse(message.getContent().toString()).text();

        if (message.isMimeType("multipart/*"))
            return getTextFromMultipart((Multipart) message.getContent());

        return "";
    }


    private String getTextFromMultipart(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);

            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) continue;

            if (part.isMimeType("text/plain")) return part.getContent().toString();

            if (part.isMimeType("text/html")) return Jsoup.parse(part.getContent().toString()).text();

            if (part.getContent() instanceof Multipart) {
                String nested = getTextFromMultipart((Multipart) part.getContent());
                if (!nested.isEmpty()) return nested;
            }
        }
        return "";
    }


    private List<String> getFiles(Message message) throws Exception {
        List<String> list = new ArrayList<>();

        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {

                BodyPart part = multipart.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    String fileName = part.getFileName();
                    Path dir = Paths.get("email_attachments");
                    Files.createDirectories(dir);

                    Path path = dir.resolve(fileName);

                    try (InputStream is = part.getInputStream()) {
                        Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
                    }

                    list.add(path.toString());
                }
            }
        }
        return list;
    }
    // -------------------- DELETE EMAIL --------------------

    @Override
    public boolean deleteEmail(Long id) {
        Email email = emailRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", protocol);
            props.setProperty("mail.imap.host", host);
            props.setProperty("mail.imap.port", port);

            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
                String from = msg.getFrom()[0].toString();
                String subject = msg.getSubject();
                if (from.contains(email.getSender()) && subject.equalsIgnoreCase(email.getSubject())) {
                    msg.setFlag(Flags.Flag.DELETED, true);
                }
            }

            inbox.close(true);
            store.close();
            emailRepo.deleteById(id);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- MOVE EMAILS BY FILTER --------------------

    @Override
    public void moveGoogleMails() {
        moveByFilter("google.com", "Google");
    }

    @Override
    public void moveYouTubeMails() {
        moveByFilter("youtube.com", "YouTube");
    }

    @Override
    public void moveNetflixMails() {
        moveByFilter("netflix.com", "Netflix");
    }

    @Override
    public void movePokemonGoMails() {
        moveByFilter("email.nianticlabs.com", "PokemonGo");
    }

    private void moveByFilter(String domain, String label) {
        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", protocol);
            props.setProperty("mail.imap.host", host);
            props.setProperty("mail.imap.port", port);

            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
                String from = msg.getFrom()[0].toString().toLowerCase();
                if (from.contains(domain.toLowerCase())) move(inbox, msg, label);
            }

            inbox.close(true);
            store.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void move(Folder inbox, Message msg, String targetLabel) throws Exception {
        Folder target = inbox.getStore().getFolder(targetLabel);
        if (!target.exists()) target.create(Folder.HOLDS_MESSAGES);
        target.open(Folder.READ_WRITE);
        inbox.copyMessages(new Message[]{msg}, target);
        msg.setFlag(Flags.Flag.DELETED, true);
        target.close(true);
    }

    @Override
    public void moveReadMails() {
        moveEmailToRead("ReadMails");
    }

    void moveEmailToRead(String messageId) {
        try {
            if (messageId == null) return;

            Properties props = new Properties();
            props.setProperty("mail.store.protocol", protocol);
            props.setProperty("mail.imap.host", host);
            props.setProperty("mail.imap.port", port);

            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
                if (msg instanceof MimeMessage) {
                    String mid = ((MimeMessage) msg).getMessageID();
                    if (messageId.equals(mid)) {
                        Folder target = store.getFolder("ReadMails");
                        if (!target.exists()) target.create(Folder.HOLDS_MESSAGES);
                        target.open(Folder.READ_WRITE);
                        inbox.copyMessages(new Message[]{msg}, target);
                        msg.setFlag(Flags.Flag.DELETED, true);
                        target.close(true);
                    }
                }
            }

            inbox.close(true);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyLabel(Folder inbox, Message msg, String labelName) throws Exception {
        Folder target = inbox.getStore().getFolder(labelName);
        if (!target.exists()) target.create(Folder.HOLDS_MESSAGES);
        target.open(Folder.READ_WRITE);
        inbox.copyMessages(new Message[]{msg}, target);
        target.close(true);
    }
}
