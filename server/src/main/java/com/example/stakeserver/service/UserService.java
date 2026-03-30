package com.example.stakeserver.service;

import com.example.stakeserver.model.UserAccount;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public UserAccount register(String username, String rawPassword) {
        String normalizedUsername = username.trim().toLowerCase();

        if (users.containsKey(normalizedUsername)) {
            throw new IllegalArgumentException("Username already exists");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        UserAccount account = new UserAccount(normalizedUsername, passwordHash);
        users.put(normalizedUsername, account);
        return account;
    }

    public UserAccount authenticate(String username, String rawPassword) {
        String normalizedUsername = username.trim().toLowerCase();
        UserAccount account = users.get(normalizedUsername);

        if (account == null) {
            return null;
        }

        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            return null;
        }

        return account;
    }

    public UserAccount findByUsername(String username) {
        if (username == null) {
            return null;
        }
        return users.get(username.trim().toLowerCase());
    }
}
