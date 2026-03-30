package com.example.stakeserver.dto;

public class AuthResponse {

    private boolean ok;
    private String message;
    private String username;
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(boolean ok, String message, String username, String token) {
        this.ok = ok;
        this.message = message;
        this.username = username;
        this.token = token;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
