package com.example.restaurant.security;

import java.time.LocalDateTime;

public record TokenInfo(Long userId, TokenRole role, LocalDateTime expiresAt) {
    public boolean expired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
