package com.mailProject.email.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clickup_config")
public class ClickupConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token; // encrypted
    private String listId;

    @OneToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;
}
