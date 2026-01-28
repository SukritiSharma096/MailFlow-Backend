package com.mailProject.email.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipleEmailResponse {

    private Long id;
    private String name;
    @Column(unique = true)
    private String username;
    private String smtpHost;
    private Integer smtpPort;
    private String imapHost;
    private Integer imapPort;
    private Boolean imapSsl;
    private Boolean smtpTls;
    private String protocol;
    private Boolean active;
}
