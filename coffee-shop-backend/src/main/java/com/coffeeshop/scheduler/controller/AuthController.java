package com.coffeeshop.scheduler.controller;

import com.coffeeshop.scheduler.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication REST controller for signup and login.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * POST /api/auth/signup
     * Body: { "username": "john", "email": "john@email.com", "password": "pass123" }
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        Map<String, Object> response = authService.signup(
            request.username, 
            request.email, 
            request.password
        );
        
        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * POST /api/auth/login
     * Body: { "usernameOrEmail": "john", "password": "pass123" }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = authService.login(
            request.usernameOrEmail, 
            request.password
        );
        
        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }
    
    // Request DTOs
    public static class SignupRequest {
        public String username;
        public String email;
        public String password;
    }
    
    public static class LoginRequest {
        public String usernameOrEmail;
        public String password;
    }
}
