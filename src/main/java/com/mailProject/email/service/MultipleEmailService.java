package com.mailProject.email.service;

import com.mailProject.email.dto.*;

import java.util.List;
import java.util.Map;

public interface MultipleEmailService {

    MultipleEmailResponse createAccount(MultipleEmailRequest request);
    List<MultipleEmailResponse> listAccounts();
    MultipleEmailResponse getAccount(Long id);
    MultipleEmailResponse updateAccount(Long id, MultipleEmailRequest request);
    void deleteAccount(Long id);

    void sendEmail(Long accountId, SendEmailRequest request) throws Exception;
    void sendEmailWithAttachments(Long accountId, SendEmailRequest request) throws Exception;
    Map<String, Object> fetchInbox(Long accountId, int page, int size, String sort, String direction) throws Exception;
    void moveGoogleMails(Long accountId);
    void moveYouTubeMails(Long accountId);
    void moveNetflixMails(Long accountId);
    void movePokemonGoMails(Long accountId);
    void moveHDFCBankMails(Long accountId);
    void moveLinkedInMails(Long accountId);
    boolean deleteEmailFromDb(Long accountId, Long emailId);
    boolean verifyPassword(String username, String password);
    ReceiveEmailResponse getEmailById(Long id);
    List<ReceiveEmailResponse> getInboxFromDb(Long accountId, String folder);
    void forwardEmail(Long accountId, Long emailId, List<String> to) throws Exception;
    boolean deleteSentEmailFromDb(Long accountId, Long emailId);
    Map<String, Object> fetchAllAccountsInbox(int page, int size, String sort, String direction);

}
