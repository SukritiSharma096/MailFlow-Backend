package com.mailProject.email.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipleEmailRequest {

    private String name;
    private String username;
    private String password;
    private String smtpHost;
    private Integer smtpPort;
    private String imapHost;
    private Integer imapPort;
    private Boolean imapSsl = true;
    private Boolean smtpTls = true;
    private String protocol = "imap";
    private Boolean active = true;
}
