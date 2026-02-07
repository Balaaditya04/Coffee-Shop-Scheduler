package com.coffeeshop.scheduler.security;

import com.coffeeshop.scheduler.entity.User;
import com.coffeeshop.scheduler.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles successful OAuth2 login - creates/fetches user, generates JWT, redirects to frontend.
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String oauthId = oAuth2User.getAttribute("sub");
        
        // Find or create user
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update OAuth info if not set
            if (user.getOauthId() == null) {
                user.setOauthId(oauthId);
                user.setOauthProvider("google");
                user.setAuthType(User.AuthType.OAUTH);
                userRepository.save(user);
            }
        } else {
            // Create new OAuth user
            String username = email.split("@")[0];
            // Ensure unique username
            int counter = 1;
            String baseUsername = username;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter++;
            }
            
            user = new User(username, email, "google", oauthId);
            userRepository.save(user);
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());
        
        // Redirect to frontend with token
        String redirectUrl = "http://localhost:3000/oauth-callback?token=" + token 
                           + "&username=" + user.getUsername();
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
