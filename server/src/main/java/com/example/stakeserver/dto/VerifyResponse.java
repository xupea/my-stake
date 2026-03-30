package com.example.stakeserver.dto;

public class VerifyResponse {

    private boolean ok;
    private String username;

    public VerifyResponse() {
    }

    public VerifyResponse(boolean ok, String username) {
        this.ok = ok;
        this.username = username;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}