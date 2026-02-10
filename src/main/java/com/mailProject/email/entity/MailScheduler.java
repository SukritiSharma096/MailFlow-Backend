package com.mailProject.email.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private Boolean status;
}