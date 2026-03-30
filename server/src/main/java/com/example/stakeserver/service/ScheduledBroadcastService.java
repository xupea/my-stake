package com.example.stakeserver.service;

import com.example.stakeserver.config.RabbitProperties;
import com.example.stakeserver.dto.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledBroadcastService.class);

    private final RabbitPublisherService rabbitPublisherService;
    private final RabbitProperties rabbitProperties;

    public ScheduledBroadcastService(
            RabbitPublisherService rabbitPublisherService,
            RabbitProperties rabbitProperties
    ) {
        this.rabbitPublisherService = rabbitPublisherService;
        this.rabbitProperties = rabbitProperties;
    }

    @Scheduled(fixedDelayString = "${app.rabbitmq.publish-interval-ms}")
    public void publishHeartbeat() {
        long timestamp = System.currentTimeMillis();
        PushMessage message = new PushMessage(
                "topic",
                rabbitProperties.getDefaultTopic(),
                null,
                timestamp,
                "scheduled message at " + timestamp
        );
        try {
            rabbitPublisherService.publish(message);
        } catch (RuntimeException e) {
            log.warn("Failed to publish scheduled RabbitMQ message: {}", e.getMessage());
        }
    }
}
