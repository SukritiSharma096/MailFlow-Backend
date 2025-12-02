package com.mailProject.email.service.impl;

import com.mailProject.email.entity.Email;
import com.mailProject.email.repository.EmailRepo;
import com.mailProject.email.entity.MessageMail;
import com.mailProject.email.service.EmailService;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
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

    private Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Autowired
    private EmailRepo emailRepo;


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



    @Override
    public void sendEmail(String to, String subject, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        simpleMailMessage.setFrom("sukh81223@gmail.com");
        mailSender.send(simpleMailMessage);
    }

    @Override
    public void sendMultipleEmail(String[] toMultiple, String subject, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(toMultiple);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        simpleMailMessage.setFrom("sukh81223@gmail.com");
        mailSender.send(simpleMailMessage);
    }

    @Override
    public void sendEmailWithFile(String to, String subject, String message, File file) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("sukh81223@gmail.com");
            helper.setTo(to);
            helper.setText(message);
            helper.setSubject(subject);
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            helper.addAttachment(fileSystemResource.getFilename(), file);
            mailSender.send(mimeMessage);
            logger.info("Email send success");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailWIthHtml(String to, String subject, String htmlContent) {
        MimeMessage simpleMailMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(simpleMailMessage, true, "Utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("sukh81223@gmail.com");
            helper.setText(htmlContent, true);
            mailSender.send(simpleMailMessage);
            logger.info("Email has been sent..");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MessageMail> getInboxMessage() {

        Properties configurations = new Properties();
        configurations.setProperty("mail.store.protocol", protocol);
        configurations.setProperty("mail.imap.host", host);
        configurations.setProperty("mail.imap.port", port);

        Session session = Session.getDefaultInstance(configurations);

        try {
            Store store = session.getStore(protocol);
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            jakarta.mail.Message[] messages = inbox.getMessages();

            List<MessageMail> list = new ArrayList<>();

            for (jakarta.mail.Message message : messages) {

                String content = getContentFromEmailMessage(message);
                List<String> files = getFileFromEmailMessage(message);

                Email email = new Email();
                email.setSender(message.getFrom()[0].toString());
                email.setReceiver(username);
                email.setSubject(message.getSubject());
                email.setBody(content);
                email.setSentAt(LocalDateTime.now());
                email.setReceived(true);

                email.setFiles(files);

                emailRepo.save(email);

                list.add(
                        MessageMail.builder()
                                .subject(message.getSubject())
                                .content(content)
                                .files(files)
                                .from(message.getFrom()[0].toString())
                                .build()
                );
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private List<String> getFileFromEmailMessage(jakarta.mail.Message message)
            throws MessagingException, IOException {

        List<String> files = new ArrayList<>();

        if (message.isMimeType("multipart/*")) {

            Multipart content = (Multipart) message.getContent();

            for (int i = 0; i < content.getCount(); i++) {
                BodyPart bodyPart = content.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    String fileName = bodyPart.getFileName();
                    Path folder = Paths.get("email_attachments");
                    if (!Files.exists(folder)) {
                        Files.createDirectories(folder);
                    }

                    Path filePath = folder.resolve(fileName);

                    try (InputStream inputStream = bodyPart.getInputStream()) {
                        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    files.add(filePath.toAbsolutePath().toString());
                }
            }
        }
        return files;
    }


    private String getContentFromEmailMessage(jakarta.mail.Message message) throws MessagingException, IOException {

        if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
            return (String) message.getContent();

        } else if (message.isMimeType("multipart/*")) {

            Multipart part = (Multipart) message.getContent();

            for (int i = 0; i < part.getCount(); i++) {
                BodyPart bodyPart = part.getBodyPart(i);

                if (bodyPart.isMimeType("text/plain")) {
                    return (String) bodyPart.getContent();
                }
            }
        }
        return null;
    }

    @Override
    public boolean deleteEmail(Long id) {

        Email email = emailRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found with ID: " + id));

        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", protocol);
            props.setProperty("mail.imap.host", host);
            props.setProperty("mail.imap.port", port);

            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);

            // Get all folders
            Folder defaultFolder = store.getDefaultFolder();
            Folder[] folders = defaultFolder.list("*");

            boolean deleted = false;

            for (Folder folder : folders) {
                if (!folder.exists()) continue;

                if ((folder.getType() & Folder.HOLDS_MESSAGES) == 0) continue;

                folder.open(Folder.READ_WRITE);

                jakarta.mail.Message[] messages = folder.getMessages();

                for (jakarta.mail.Message msg : messages) {

                    String from = msg.getFrom()[0].toString();
                    String subject = msg.getSubject();

                    if (from.contains(email.getSender()) &&
                            subject.equalsIgnoreCase(email.getSubject())) {

                        msg.setFlag(Flags.Flag.DELETED, true);
                        deleted = true;
                        break;
                    }
                }

                folder.close(true);
            }

            store.close();

            emailRepo.deleteById(id);

            return deleted;

        } catch (Exception e) {
            throw new RuntimeException("Error while deleting email: " + e.getMessage(), e);
        }
    }



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
            Properties config = new Properties();
            config.setProperty("mail.store.protocol", protocol);
            config.setProperty("mail.imap.host", host);
            config.setProperty("mail.imap.port", port);

            Session session = Session.getDefaultInstance(config);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);

            IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();

            for (Message msg : messages) {
                String from = msg.getFrom()[0].toString().toLowerCase();

                if (from.contains(domain.toLowerCase())) {
                    move(inbox, msg, label);
                }
            }

            inbox.close(true);
            store.close();

        } catch (Exception e) {
            throw new RuntimeException("Error moving mails to " + label + ": " + e.getMessage());
        }
    }


    private void move(IMAPFolder source, Message msg, String targetLabel) throws Exception {

        Folder target = source.getStore().getFolder(targetLabel);

        if (!target.exists()) {
            target.create(Folder.HOLDS_MESSAGES);
        }
        target.open(Folder.READ_WRITE);

        source.copyMessages(new Message[]{msg}, target);

        source.setFlags(new Message[]{msg}, new Flags(Flags.Flag.DELETED), true);

        source.expunge();

        target.close(true);
    }


    @Override
    public void moveReadMails() {
        moveReadsToLabel("ReadMails");
    }


    private void moveReadsToLabel(String labelName) {

        try {
            Properties config = new Properties();
            config.setProperty("mail.store.protocol", protocol);
            config.setProperty("mail.imap.host", host);
            config.setProperty("mail.imap.port", port);

            Session session = Session.getDefaultInstance(config);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);

            IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();

            for (Message msg : messages) {

                if (msg.isSet(Flags.Flag.SEEN)) {

                    applyLabel(inbox, msg, labelName);
                }
            }

            inbox.close(true);
            store.close();

        } catch (Exception e) {
            throw new RuntimeException("Error moving read mails: " + e.getMessage());
        }
    }

    private void applyLabel(IMAPFolder inbox, Message msg, String labelName) throws Exception {

        Folder target = inbox.getStore().getFolder(labelName);

        if (!target.exists()) {
            target.create(Folder.HOLDS_MESSAGES);
        }

        target.open(Folder.READ_WRITE);

        inbox.copyMessages(new Message[]{msg}, target);

        target.close(true);
    }
}
