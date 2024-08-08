package com.example.devprojectlabstarter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1024)  // Tăng độ dài của cột url lên 1024 ký tự
    private String url;

    // Getters và setters
}
