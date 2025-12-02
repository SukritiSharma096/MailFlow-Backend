package com.mailProject.email.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;
    private String subject;
    @Column(columnDefinition = "LONGTEXT")
    private String body;
    private LocalDateTime sentAt;
    private boolean isReceived;
    @ElementCollection
    private List<String> files;

}
