package com.mailProject.email.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "received_emails")
public class ReceivedEmails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true)
    private String messageId;


    private String sender;

    @Column(columnDefinition = "TEXT")
    private String receivers;

    private String subject;

    @Column(columnDefinition = "LONGTEXT")
    private String body;

    private LocalDateTime sentAt;

    private boolean isReceived;

    @Column(columnDefinition = "TEXT")
    private String attachments;

    private String folder;

    @Column(name = "account_id")
    private Long accountId;
}
