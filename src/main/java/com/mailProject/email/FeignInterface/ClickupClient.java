package com.mailProject.email.FeignInterface;

import com.mailProject.email.config.FeignConfig;
import com.mailProject.email.dto.TaskRequest;
import com.mailProject.email.dto.TaskResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;



@FeignClient(name = "clickupClient", url = "https://api.clickup.com/api/v2", configuration = FeignConfig.class)
public interface ClickupClient {

    @PostMapping("/list/{listId}/task")
    TaskResponse createTask(
            @RequestHeader("Authorization") String token,
            @PathVariable("listId") String listId,
            @RequestBody TaskRequest request
    );

}
