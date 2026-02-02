package com.mailProject.email.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskRequest {
    private String name;
    private String description;

    private Integer priority;
    private List<String> assignees;
    private List<String> tags;
    private String status;
    private Long due_date;
    private Boolean due_date_time;
    private Long time_estimate;
    private Long start_date;
    private Boolean start_date_time;
    private Boolean notify_all;
}
