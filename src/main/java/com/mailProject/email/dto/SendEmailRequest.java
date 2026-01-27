package com.mailProject.email.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class SendEmailRequest {

    private List<String> to;
    private String subject;
    private String body;
    private List<MultipartFile> attachments;
}
