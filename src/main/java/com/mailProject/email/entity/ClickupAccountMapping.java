package com.mailProject.email.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clickup_account_mapping")
public class ClickupAccountMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String spaceId;
    private String listId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private MultipleEmailAccounts account;
}
