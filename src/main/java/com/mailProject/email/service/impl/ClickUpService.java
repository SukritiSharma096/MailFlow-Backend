package com.mailProject.email.service.impl;

import com.mailProject.email.FeignInterface.ClickupClient;
import com.mailProject.email.dto.TaskRequest;
import com.mailProject.email.dto.TaskResponse;
import com.mailProject.email.entity.Email;
import com.mailProject.email.repository.EmailRepo;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ClickUpService {

    @Autowired
    private ClickupClient clickupClient;

    @Autowired
    private EmailRepo emailRepo;
    
    @Autowired
    private EmailServiceImpl emailService;

    private final String TOKEN = "pk_100906060_3O93JIRRV7RJO4HU0UTX2UYB3TS8DZOJ";
    private final String listId = "901605368435";


    public TaskResponse createTask(Email email) {
        TaskRequest request = new TaskRequest();
        request.setName(email.getSubject());
        request.setDescription(email.getBody());

        return clickupClient.createTask(TOKEN, listId, request);
    }

    public void createTasksFromDB() {
        List<Email> emails = emailRepo.findByTaskCreatedFalse();

        for (Email email : emails) {
            TaskRequest request = new TaskRequest();
            request.setName(email.getSubject());
            request.setDescription(email.getBody());

            TaskResponse task = clickupClient.createTask(TOKEN, listId, request);

            if (task != null && task.getId() != null) {
                email.setTaskCreated(true);
                email.setTaskId(task.getId());
                emailRepo.save(email);

                if (email.getFiles() != null) {
                    for (String path : email.getFiles()) {
                        uploadFile(task.getId(), path);
                    }
                }
                emailService.moveEmailToRead(email.getMessageId());
            }
        }
    }

    private void uploadFile(String taskId, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return;

            OkHttpClient client = new OkHttpClient();
            RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("attachment", file.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .header("Authorization", TOKEN)
                    .url("https://api.clickup.com/api/v2/task/" + taskId + "/attachment")
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) System.out.println("✔ Attachment Uploaded: " + file.getName());
            else System.out.println("Upload Failed: " + response.message());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
