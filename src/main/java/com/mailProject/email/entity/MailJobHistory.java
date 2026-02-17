package com.mailProject.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_job_history")
@Getter
@Setter
public class MailJobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String runType;

    private Integer mailCount;

    private Integer taskCreated;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
