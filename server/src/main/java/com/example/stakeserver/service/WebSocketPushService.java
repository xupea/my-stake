package com.example.stakeserver.service;

import com.example.stakeserver.dto.PushMessage;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WebSocketPushService {

    private static final Set<String> SUPPORTED_TOPICS = Set.of(
            "ws.available-balances",
            "ws.vault-balances",
            "ws.highroller-house-bets",
            "ws.announcements",
            "ws.race-status",
            "ws.feature-flag",
            "ws.notifications",
            "ws.house-bets",
            "ws.deposit-bonus-transaction"
    );

    private final RabbitPublisherService rabbitPublisherService;

    public WebSocketPushService(RabbitPublisherService rabbitPublisherService) {
        this.rabbitPublisherService = rabbitPublisherService;
    }

    public PushMessage publish(String type, String topic, String userId, String payload) {
        String normalizedType = type == null ? null : type.trim().toLowerCase();
        if (normalizedType == null || normalizedType.isBlank()) {
            throw new IllegalArgumentException("Message type is required");
        }

        validate(normalizedType, topic, userId);

        PushMessage message = new PushMessage(
                normalizedType,
                normalizeTopic(topic),
                normalizeUserId(userId),
                System.currentTimeMillis(),
                payload
        );
        rabbitPublisherService.publish(message);
        return message;
    }

    private void validate(String type, String topic, String userId) {
        if ("topic".equals(type)) {
            String normalizedTopic = normalizeTopic(topic);
            if (normalizedTopic == null || !SUPPORTED_TOPICS.contains(normalizedTopic)) {
                throw new IllegalArgumentException("Unsupported websocket topic: " + topic);
            }
            return;
        }

        if ("user".equals(type)) {
            String normalizedUserId = normalizeUserId(userId);
            if (normalizedUserId == null || normalizedUserId.isBlank()) {
                throw new IllegalArgumentException("userId is required for user messages");
            }
            return;
        }

        if (!"broadcast".equals(type)) {
            throw new IllegalArgumentException("Unsupported message type: " + type);
        }
    }

    private String normalizeTopic(String topic) {
        if (topic == null) {
            return null;
        }
        return topic.trim();
    }

    private String normalizeUserId(String userId) {
        if (userId == null) {
            return null;
        }
        return userId.trim();
    }
}
