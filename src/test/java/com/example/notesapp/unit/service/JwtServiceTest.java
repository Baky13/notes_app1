package com.example.notesapp.unit.service;

import com.example.notesapp.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private static final String SECRET_KEY = "dGVzdC1zZWNyZXQta2V5LWZvci1pbnRlZ3JhdGlvbi10ZXN0cy1pbi1ub3Rlcy1hcHAtcHJvamVjdA==";
    private static final long JWT_EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("generateToken_ReturnsValidJwt - генерация валидного токена")
    void generateToken_ReturnsValidJwt() {
        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("extractUsername_FromValidToken - извлечение username")
    void extractUsername_FromValidToken() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    @DisplayName("isTokenValid_WithValidToken_ReturnsTrue - валидный токен проходит проверку")
    void isTokenValid_WithValidToken_ReturnsTrue() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid_WithExpiredToken_ReturnsFalse - просроченный токен отклоняется")
    void isTokenValid_WithExpiredToken_ReturnsFalse() {
        // Given - создаём токен с истёкшим сроком действия
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "testuser");
        
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        String expiredToken = Jwts.builder()
                .claims(claims)
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 50000)) // Already expired
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();

        // When & Then - ожидаем что isTokenValid выбросит исключение
        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, userDetails))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    @DisplayName("isTokenValid_WithTamperedToken_ReturnsFalse - изменённый токен отклоняется")
    void isTokenValid_WithTamperedToken_ReturnsFalse() {
        // Given - создаём валидный токен и меняем его
        String validToken = jwtService.generateToken(userDetails);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "xxxxx";

        // When & Then - ожидаем что isTokenValid выбросит исключение
        assertThatThrownBy(() -> jwtService.isTokenValid(tamperedToken, userDetails))
                .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }

    @Test
    @DisplayName("generateToken_WithExtraClaims - генерация токена с дополнительными данными")
    void generateToken_WithExtraClaims() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", 123);

        // When
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("testuser");
    }
}
