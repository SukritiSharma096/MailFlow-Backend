package com.mailProject.email.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailEntityRequest {

    private String sender;
    private  String receiver;
    private String subject;
    private String body;

}