package com.mailProject.email.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {

    private  String to;

    private List<String> toMultiple;

    private String subject;

    private String message;

}
