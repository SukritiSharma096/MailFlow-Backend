package com.mailProject.email.service;

import com.mailProject.email.entity.MessageMail;

import java.io.File;
import java.util.List;

public interface EmailService {


    void sendEmail(String to, String subject, String message);

    void sendMultipleEmail(String []toMultiple, String subject, String message);

    void sendEmailWithFile(String to, String subject, String message, File file);


    void sendEmailWIthHtml(String to, String subject, String htmlContent);

    List<MessageMail> getInboxMessage();

    boolean deleteEmail(Long id);

    void moveGoogleMails();
    void moveYouTubeMails();
    void moveNetflixMails();
    void movePokemonGoMails();
    void moveReadMails();
}
