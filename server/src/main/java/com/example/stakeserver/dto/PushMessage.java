package com.example.stakeserver.dto;

public class PushMessage {

    private String type;
    private String topic;
    private String userId;
    private long timestamp;
    private String payload;

    public PushMessage() {
    }

    public PushMessage(String type, String topic, String userId, long timestamp, String payload) {
        this.type = type;
        this.topic = topic;
        this.userId = userId;
        this.timestamp = timestamp;
        this.payload = payload;
    }

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
