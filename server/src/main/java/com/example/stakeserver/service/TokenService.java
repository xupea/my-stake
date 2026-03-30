package com.example.stakeserver.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Map<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public String issueToken(String username) {
        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        tokenToUsername.put(token, username);
        return token;
    }

    public String verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return tokenToUsername.get(token);
    }

    public void revokeToken(String token) {
        if (token != null) {
            tokenToUsername.remove(token);
        }
    }
}
