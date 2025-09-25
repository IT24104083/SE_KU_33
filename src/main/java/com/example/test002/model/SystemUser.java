package com.example.test002.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SystemUsers")
public class SystemUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userID;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String passwordSalt;

    @Column(nullable = false)
    private String userRole;

    private String fullName;
    private String phone;
    private Boolean isActive = true;
    private Integer failedLoginAttempts = 0;
    private Boolean isLocked = false;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;
    private LocalDateTime lastPasswordChange;

    // Constructors, getters, and setters
    public SystemUser() {
        this.createdDate = LocalDateTime.now();
        this.lastPasswordChange = LocalDateTime.now();
    }

    // Getters and setters...
}