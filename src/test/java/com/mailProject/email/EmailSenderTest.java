// package com.mailProject.email;

// import com.mailProject.email.repository.EmailRepo;
// import com.mailProject.email.service.EmailService;
// import com.mysql.cj.protocol.Message;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
// import java.io.InputStream;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// @SpringBootTest
// public class EmailSenderTest {

//     @Autowired
//     private EmailService emailService;

//     @Autowired
//     private EmailRepo emailRepo;

//     @Test
//     void emailSendTest(){
//         System.out.println("sending email");
//         emailService.sendEmail("ssukriti39@gmail.com", "Email from spring boot", "this email is send using spring boot while create email service.");

//     }


//     @Test
//     void sendEmailWIthHtml(){
//           String html = ""+
//                   "<h1 style='color:blue;border:1px solid blue'> this email is sent to test api"+
//                   "";
//         emailService.sendEmailWIthHtml("ssukriti39@gmail.com","mail from springboot",html );
//     }


//     @Test
//     void sendEmailWithFile(){
//         emailService.sendEmailWithFile("vivekrai8086@gmail.com","Email with file", "This email contains file",
//                 new File("C:/Users/ThinkBook/Pictures/Screenshots/Screenshot 2025-08-29 102534.png")
//                 );

//     }


// //    @Test
// //    void sendEmailWithFileWIthStream(){
// //        File file = new File("C:/Users/ThinkBook/Pictures/Screenshots/Screenshot 2025-08-29 102534.png");
// //        try {
// //            InputStream is = new FileInputStream(file);
// //            emailService.sendEmailWithFile("ssukriti39@gmail.com", "Email with file", "this email contains file", is);
// //        } catch (FileNotFoundException e) {
// //            throw new RuntimeException(e);
// //        }
// //    }


// // @Test
// // void getInbox(){
// //
// //       List<Message> inboxMessage = emailService.getInboxMessage();
// //       inboxMessage.forEach(item->{
// //           System.out.println(item.getSubject());
// //           System.out.println(item.getContent());
// //           System.out.println(item.getFiles());
// //           System.out.println("----------------------------------------------------------------------");
// //       });
// // }


//     @Test
//     void deleteEmailRealTest() {

//         Long id = 31L;

//         boolean deleted = emailService.deleteEmail(id);

//         Assertions.assertTrue(deleted);
//         System.out.println("Mail deleted from Gmail + Database.");
//     }
// }
