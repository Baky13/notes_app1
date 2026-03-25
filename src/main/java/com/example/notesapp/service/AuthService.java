package com.example.notesapp.service;

import com.example.notesapp.dto.auth.LoginRequest;
import com.example.notesapp.dto.auth.AuthResponse;
import com.example.notesapp.dto.auth.RegisterRequest;
import com.example.notesapp.entity.User;
import com.example.notesapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        var user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), java.util.Collections.emptyList()));
        return new AuthResponse(jwtToken, "Bearer", user.getId(), user.getUsername());
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), java.util.Collections.emptyList()));
        return new AuthResponse(jwtToken, "Bearer", user.getId(), user.getUsername());
    }
}
