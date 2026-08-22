package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.LoginRequest;
import com.karmayuva.pointsystem.dto.LoginResponse;
import com.karmayuva.pointsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    // BCrypt hash of the real admin password - set via env var, never commit the plain password
    @Value("${app.admin.password-hash}")
    private String adminPasswordHash;

    public AuthService(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        if (!adminUsername.equals(request.username()) ||
                !passwordEncoder.matches(request.password(), adminPasswordHash)) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String token = jwtUtil.generateToken(adminUsername);
        return new LoginResponse(token, adminUsername);
    }
}
