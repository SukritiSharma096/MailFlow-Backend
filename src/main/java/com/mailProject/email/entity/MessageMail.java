package com.mailProject.email.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageMail {

    private String from;
    private String content;
    private List<String> files;
    private String subject;
}
