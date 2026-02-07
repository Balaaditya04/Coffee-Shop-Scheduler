package com.coffeeshop.scheduler.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User entity for authentication - maps to 'users' table in MySQL.
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = true, length = 255)
    private String password;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "role", length = 20)
    private String role = "USER";
    
    @Column(name = "auth_type", length = 20)
    @Enumerated(EnumType.STRING)
    private AuthType authType = AuthType.EMAIL;
    
    @Column(name = "oauth_provider", length = 20)
    private String oauthProvider;
    
    @Column(name = "oauth_id", length = 100)
    private String oauthId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.authType = AuthType.EMAIL;
    }
    
    // OAuth constructor
    public User(String username, String email, String oauthProvider, String oauthId) {
        this.username = username;
        this.email = email;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.authType = AuthType.OAUTH;
        this.password = null;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public AuthType getAuthType() { return authType; }
    public void setAuthType(AuthType authType) { this.authType = authType; }
    
    public String getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }
    
    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }
    
    public enum AuthType {
        EMAIL, OAUTH
    }
}
