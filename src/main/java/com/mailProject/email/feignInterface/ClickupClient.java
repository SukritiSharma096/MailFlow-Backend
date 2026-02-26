package com.mailProject.email.feignInterface;

import com.mailProject.email.dto.TaskRequest;

import com.mailProject.email.config.FeignConfig;
import com.mailProject.email.dto.TaskResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "clickupClient",
        url = "${clickup.base-url}",
        configuration = FeignConfig.class)

public interface ClickupClient {

    @PostMapping("/list/{listId}/task")
    TaskResponse createTask(
            @PathVariable("listId") String listId,
            @RequestBody TaskRequest request
    );

    @PostMapping(value = "/task/{taskId}/attachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void uploadAttachment(
            @PathVariable("taskId") String taskId,
            @RequestPart("attachment") MultipartFile file
    );

}
