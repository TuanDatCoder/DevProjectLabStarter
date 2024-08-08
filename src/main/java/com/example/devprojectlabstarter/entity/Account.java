package com.example.devprojectlabstarter.entity;

import com.example.devprojectlabstarter.entity.Enum.AccountProviderEnum;
import com.example.devprojectlabstarter.entity.Enum.AccountRoleEnum;
import com.example.devprojectlabstarter.entity.Enum.AccountStatusEnum;
import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String name;

    @Size(min = 6)
    private String password;

    @Column
    private String picture;

    @Column(unique = true)
    private String googleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountRoleEnum role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountProviderEnum provider;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatusEnum status;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;
    // Getters and Setters
}