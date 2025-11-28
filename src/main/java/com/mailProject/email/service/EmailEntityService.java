package com.mailProject.email.service;

import com.mailProject.email.dto.EmailEntityRequest;
import com.mailProject.email.dto.EmailResponse;

import java.util.List;

public interface EmailEntityService {

    EmailResponse saveEmail(EmailEntityRequest request);

    List<EmailResponse> getAllEmails();

    EmailResponse getEmailById(Long id);

    EmailResponse updateEmail(Long id, EmailEntityRequest request);

    String deleteEmail(Long id);
}
