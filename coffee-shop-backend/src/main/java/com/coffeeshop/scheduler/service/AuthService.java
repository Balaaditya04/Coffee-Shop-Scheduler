package com.coffeeshop.scheduler.service;

import com.coffeeshop.scheduler.entity.User;
import com.coffeeshop.scheduler.repository.UserRepository;
import com.coffeeshop.scheduler.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication service handling signup and login with password hashing.
 */
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Register a new user with hashed password.
     */
    public Map<String, Object> signup(String username, String email, String password) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Username is required");
            return response;
        }
        if (email == null || email.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return response;
        }
        if (password == null || password.length() < 6) {
            response.put("success", false);
            response.put("message", "Password must be at least 6 characters");
            return response;
        }
        
        // Check if username exists
        if (userRepository.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "Username already exists");
            return response;
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(email)) {
            response.put("success", false);
            response.put("message", "Email already registered");
            return response;
        }
        
        // Create user with hashed password
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        
        userRepository.save(user);
        
        // Generate token
        String token = jwtUtil.generateToken(user.getUsername());
        
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("userId", user.getId());
        
        return response;
    }
    
    /**
     * Authenticate user and return JWT token.
     */
    public Map<String, Object> login(String usernameOrEmail, String password) {
        Map<String, Object> response = new HashMap<>();
        
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Username or email is required");
            return response;
        }
        if (password == null || password.isEmpty()) {
            response.put("success", false);
            response.put("message", "Password is required");
            return response;
        }
        
        // Find user by username or email
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail.toLowerCase());
        }
        
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }
        
        User user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid password");
            return response;
        }
        
        // Generate token
        String token = jwtUtil.generateToken(user.getUsername());
        
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("userId", user.getId());
        
        return response;
    }
}
