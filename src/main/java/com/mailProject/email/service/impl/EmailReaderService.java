package com.mailProject.email.service.impl;

import com.mailProject.email.entity.Email;
import org.springframework.beans.factory.annotation.Autowired;

public class EmailReaderService {

    @Autowired
    private ClickUpService clickUpTaskService;

    public void onNewEmail(Email email) {

        System.out.println("New email received: " + email.getSubject());

        clickUpTaskService.createTask(email);

        System.out.println("Task created in ClickUp automatically!");
    }
}
