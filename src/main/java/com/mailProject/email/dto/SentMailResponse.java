package com.mailProject.email.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SentMailResponse {

    private Long id;
    private Long accountId;
    private String toEmails;
    private String subject;
    private String body;
    private LocalDateTime sentAt;
    private List<String> attachments;

}
