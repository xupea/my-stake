package com.example.stakeserver.service;

import com.example.stakeserver.config.RabbitProperties;
import com.example.stakeserver.dto.PushMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties rabbitProperties;

    public RabbitPublisherService(RabbitTemplate rabbitTemplate, RabbitProperties rabbitProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitProperties = rabbitProperties;
    }

    public void publish(PushMessage message) {
        rabbitTemplate.convertAndSend(
                rabbitProperties.getExchange(),
                toRoutingKey(message),
                toJson(message)
        );
    }

    private String toRoutingKey(PushMessage message) {
        return "push." + escapeJson(message.getType());
    }

    private String toJson(PushMessage message) {
        return "{\"type\":\"" + escapeJson(message.getType()) + "\","
                + "\"topic\":\"" + escapeJson(message.getTopic()) + "\","
                + "\"userId\":\"" + escapeJson(message.getUserId()) + "\","
                + "\"timestamp\":" + message.getTimestamp() + ","
                + "\"payload\":\"" + escapeJson(message.getPayload()) + "\"}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
