package com.mailProject.email.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MailJobHistoryResponseDto {

    private Long id;
    private String runType;
    private Integer mailCount;
    private Integer taskCreated;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

