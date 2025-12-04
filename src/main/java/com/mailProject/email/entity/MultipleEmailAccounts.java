package com.mailProject.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Email_accounts")
public class MultipleEmailAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
