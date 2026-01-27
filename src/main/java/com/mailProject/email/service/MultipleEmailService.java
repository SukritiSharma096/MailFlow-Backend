package com.mailProject.email.service;

import com.mailProject.email.dto.*;

import java.util.List;

public interface MultipleEmailService {

    MultipleEmailResponse createAccount(MultipleEmailRequest request);
    List<MultipleEmailResponse> listAccounts();
    MultipleEmailResponse getAccount(Long id);
    MultipleEmailResponse updateAccount(Long id, MultipleEmailRequest request);
    void deleteAccount(Long id);

    void sendEmail(Long accountId, SendEmailRequest request) throws Exception;
    void sendEmailWithAttachments(Long accountId, SendEmailRequest request) throws Exception;
    List<ReceiveEmailResponse> fetchInbox(Long accountId) throws Exception;
    void moveGoogleMails(Long accountId);
    void moveYouTubeMails(Long accountId);
    void moveNetflixMails(Long accountId);
    void movePokemonGoMails(Long accountId);
    void moveHDFCBankMails(Long accountId);
    void moveLinkedInMails(Long accountId);
    boolean deleteEmailFromDb(Long accountId, Long emailId);


}
