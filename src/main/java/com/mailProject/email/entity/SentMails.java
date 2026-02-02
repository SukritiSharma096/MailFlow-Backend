package com.mailProject.email.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "sent_mails")
public class SentMails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountId;
    @Column(length = 500)
    private String toEmails;
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String body;
    private LocalDateTime sentAt;
    private String attachments;

}
