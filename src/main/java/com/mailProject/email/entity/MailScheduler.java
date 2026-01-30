package com.mailProject.email.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "mail_schedule")
public class MailScheduler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(name = "cron_expression")
    private String cronExpression;
    private String description;
    private Boolean status;
}