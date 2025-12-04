package com.mailProject.email.controller;

import com.mailProject.email.service.impl.ClickUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ClickUpService clickUpService;

    @PostMapping("/create-from-db")
    public String createTasksFromDB() {
        clickUpService.createTasksFromDB();
        return "Tasks created from DB emails!";
    }
}
