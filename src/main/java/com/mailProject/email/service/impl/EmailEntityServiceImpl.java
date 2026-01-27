package com.mailProject.email.service.impl;

import com.mailProject.email.FeignInterface.ClickupClient;
import com.mailProject.email.dto.EmailEntityRequest;
import com.mailProject.email.dto.EmailResponse;
import com.mailProject.email.dto.TaskRequest;
import com.mailProject.email.dto.TaskResponse;
import com.mailProject.email.entity.Email;
import com.mailProject.email.repository.EmailRepo;
import com.mailProject.email.service.EmailEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailEntityServiceImpl implements EmailEntityService {

    @Autowired
    private EmailRepo emailRepository;


    @Override
    public EmailResponse saveEmail(EmailEntityRequest request) {

        Email email = new Email();
        email.setSender(request.getSender());
        email.setReceiver(request.getReceiver());
        email.setSubject(request.getSubject());
        email.setBody(request.getBody());
        email.setSentAt(LocalDateTime.now());
        email.setReceived(false);

        Email saved = emailRepository.save(email);

        return mapToResponse(saved);
    }

    @Override
    public List<EmailResponse> getAllEmails() {
        return emailRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EmailResponse getEmailById(Long id) {
        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));
        return mapToResponse(email);
    }

    @Override
    public EmailResponse updateEmail(Long id, EmailEntityRequest request) {
        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        email.setSubject(request.getSubject());
        email.setBody(request.getBody());

        Email updated = emailRepository.save(email);
        return mapToResponse(updated);
    }

    @Override
    public String deleteEmail(Long id) {
        emailRepository.deleteById(id);
        return "Email deleted successfully";
    }

    private EmailResponse mapToResponse(Email entity) {
        EmailResponse r = new EmailResponse();
        r.setId(entity.getId());
        r.setSender(entity.getSender());
        r.setReceiver(entity.getReceiver());
        r.setSubject(entity.getSubject());
        r.setBody(entity.getBody());
        r.setSentAt(entity.getSentAt());
        r.setReceived(entity.isReceived());
        return r;
    }

}
