package com.silvercare.models;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_user")
public class AdminUser {
    @Id
    private String username;
    private String password;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}