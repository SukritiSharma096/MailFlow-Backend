package com.mailProject.email.feignInterface;

import com.mailProject.email.dto.TaskRequest;

import com.mailProject.email.config.FeignConfig;
import com.mailProject.email.dto.TaskResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin("*")
@FeignClient(
        name = "clickupClient",
        url = "${clickup.base-url}",
        configuration = FeignConfig.class
)
public interface ClickupClient {

    @PostMapping("/team/{teamId}/space")
    Map<String, Object> createSpace(
            @PathVariable String teamId,
            @RequestBody Map<String, Object> body
    );

    @GetMapping("/team/{teamId}/space")
    Map<String, Object> getSpaces(@PathVariable String teamId);

    @PostMapping("/space/{spaceId}/list")
    Map<String, Object> createList(
            @PathVariable String spaceId,
            @RequestBody Map<String, Object> body
    );

    @GetMapping("/space/{spaceId}/list")
    Map<String, Object> getLists(@PathVariable String spaceId);


    @PostMapping("/list/{listId}/task")
    TaskResponse createTask(
            @PathVariable String listId,
            @RequestBody TaskRequest request
    );

    @PostMapping(value = "/task/{taskId}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void uploadAttachment(
            @PathVariable String taskId,
            @RequestPart("attachment") MultipartFile file
    );
}