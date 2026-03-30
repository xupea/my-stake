package com.example.stakeserver.controller;

import com.example.stakeserver.dto.AuthResponse;
import com.example.stakeserver.dto.LoginRequest;
import com.example.stakeserver.dto.RegisterRequest;
import com.example.stakeserver.dto.VerifyResponse;
import com.example.stakeserver.model.UserAccount;
import com.example.stakeserver.service.TokenService;
import com.example.stakeserver.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserAccount account = userService.register(request.getUsername(), request.getPassword());
            String token = tokenService.issueToken(account.getUsername());

            return ResponseEntity.ok(
                    new AuthResponse(true, "Register success", account.getUsername(), token)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new AuthResponse(false, e.getMessage(), null, null)
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        UserAccount account = userService.authenticate(request.getUsername(), request.getPassword());

        if (account == null) {
            return ResponseEntity.status(401).body(
                    new AuthResponse(false, "Invalid username or password", null, null)
            );
        }

        String token = tokenService.issueToken(account.getUsername());

        return ResponseEntity.ok(
                new AuthResponse(true, "Login success", account.getUsername(), token)
        );
    }

    @GetMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        String token = extractBearerToken(authorization);
        String username = tokenService.verifyToken(token);

        if (username == null) {
            return ResponseEntity.status(401).body(new VerifyResponse(false, null));
        }

        return ResponseEntity.ok(new VerifyResponse(true, username));
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        return authorization;
    }
}
