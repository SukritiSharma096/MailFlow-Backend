package com.mailProject.email.controller;


import com.mailProject.email.entity.MessageMail;
import com.mailProject.email.model.CustomResponse;
import com.mailProject.email.model.EmailRequest;
import com.mailProject.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request) {
        // Call your service method
        emailService.sendEmail(request.getTo(), request.getSubject(), request.getMessage());

        // Return response
        return ResponseEntity.ok(
                CustomResponse.builder()
                        .message("Email sent successfully!!")
                        .httpStatus(HttpStatus.OK)
                        .success(true)
                        .build()
        );
    }



    @PostMapping("/send-multiple")
    public ResponseEntity<String> sendMultipleEmail(@RequestBody EmailRequest request) {
        emailService.sendMultipleEmail(
                request.getToMultiple().toArray(new String[0]),
                request.getSubject(),
                request.getMessage()
        );
        return ResponseEntity.ok("Multiple emails sent successfully!");
    }

    @PostMapping(
            value = "/send-with-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> sendEmailWithFile(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        File convFile = new File(System.getProperty("java.io.tmpdir")
                + "/" + file.getOriginalFilename());
        file.transferTo(convFile);

        emailService.sendEmailWithFile(to, subject, message, convFile);

        return ResponseEntity.ok("Email with attachment sent successfully!");
    }




    @PostMapping("/send-html")
    public ResponseEntity<String> sendEmailWithHtml(@RequestParam String to, @RequestParam String subject, @RequestParam String htmlContent) {
        emailService.sendEmailWIthHtml(to, subject, htmlContent);
        return ResponseEntity.ok("HTML email sent successfully!");
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<MessageMail>> getInboxMessages() {
        List<MessageMail> messages = emailService.getInboxMessage();
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CustomResponse> deleteEmail(@PathVariable Long id) {
        boolean deleted = emailService.deleteEmail(id);

        if (deleted) {
            return ResponseEntity.ok(
                    CustomResponse.builder()
                            .message("Email deleted successfully from all folders!")
                            .httpStatus(HttpStatus.OK)
                            .success(true)
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            CustomResponse.builder()
                                    .message("Email not found on server.")
                                    .httpStatus(HttpStatus.NOT_FOUND)
                                    .success(false)
                                    .build()
                    );
        }
    }

    @PostMapping("/move/google")
    public ResponseEntity<String> moveGoogle() {
        emailService.moveGoogleMails();
        return ResponseEntity.ok("Google mails moved successfully");
    }

    @PostMapping("/move/youtube")
    public ResponseEntity<String> moveYouTube() {
        emailService.moveYouTubeMails();
        return ResponseEntity.ok("YouTube mails moved successfully");
    }

    @PostMapping("/move/netflix")
    public ResponseEntity<String> moveNetflix() {
        emailService.moveNetflixMails();
        return ResponseEntity.ok("Netflix mails moved successfully");
    }

    @PostMapping("/move/pokemongo")
    public ResponseEntity<String> movePokemonGo() {
        emailService.movePokemonGoMails();
        return ResponseEntity.ok("PokemonGo mails moved successfully");
    }
}
