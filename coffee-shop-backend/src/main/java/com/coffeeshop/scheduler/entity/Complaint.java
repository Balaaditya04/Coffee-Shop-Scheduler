package com.coffeeshop.scheduler.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity for storing customer complaints about baristas.
 */
@Entity
@Table(name = "complaints")
public class Complaint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "barista_name", nullable = false, length = 50)
    private String baristaName;
    
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    @Column(name = "message", nullable = false, length = 500)
    private String message;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    public Complaint() {
        this.createdAt = Instant.now();
    }
    
    public Complaint(String baristaName, String username, String message) {
        this.baristaName = baristaName;
        this.username = username;
        this.message = message;
        this.createdAt = Instant.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public String getBaristaName() { return baristaName; }
    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setBaristaName(String baristaName) { this.baristaName = baristaName; }
    public void setUsername(String username) { this.username = username; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
