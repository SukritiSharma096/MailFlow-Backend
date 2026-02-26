package com.mailProject.email.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReceiveEmailRequest {

    private String sender;
    private List<String> receiver;
    private String subject;
    @Column(columnDefinition = "LONGTEXT")
    private String body;
    private String folder;
    private List<String> attachments;
}
