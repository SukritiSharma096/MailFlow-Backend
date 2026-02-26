package com.mailProject.email.service;

import com.mailProject.email.dto.MailJobHistoryResponseDto;

import java.util.List;

public interface MailJobService {

    void runJob(String runType);
    MailJobHistoryResponseDto getLatest();
    List<MailJobHistoryResponseDto> getHistory();

}
