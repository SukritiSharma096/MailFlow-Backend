package com.mailProject.email.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveEmailDto {

    private Long id;
    private String sender;
    private List<String> receiver;
    private String subject;
    @Column(columnDefinition = "LONGTEXT")
    private String body;
    private LocalDateTime sentAt;
    private boolean isReceived;
    private List<String> attachments;
    private String folder;
}
