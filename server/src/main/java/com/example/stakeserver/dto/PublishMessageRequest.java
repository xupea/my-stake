package com.example.stakeserver.dto;

import jakarta.validation.constraints.NotBlank;

public class PublishMessageRequest {

    @NotBlank
    private String type;

    private String topic;

    private String userId;

    @NotBlank
    private String payload;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
