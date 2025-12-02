package com.mailProject.email.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class EmailResponse {

    private Long id;
    private String sender;
    private String receiver;
    private String subject;
    private String body;
    private LocalDateTime sentAt;
    private boolean received;

}
