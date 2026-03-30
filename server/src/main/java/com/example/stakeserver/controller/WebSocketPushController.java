package com.example.stakeserver.controller;

import com.example.stakeserver.dto.AuthResponse;
import com.example.stakeserver.dto.PublishMessageRequest;
import com.example.stakeserver.dto.PushMessage;
import com.example.stakeserver.service.WebSocketPushService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ws")
public class WebSocketPushController {

    private final WebSocketPushService webSocketPushService;

    public WebSocketPushController(WebSocketPushService webSocketPushService) {
        this.webSocketPushService = webSocketPushService;
    }

    @PostMapping("/publish")
    public ResponseEntity<AuthResponse> publish(@Valid @RequestBody PublishMessageRequest request) {
        try {
            PushMessage message = webSocketPushService.publish(
                    request.getType(),
                    request.getTopic(),
                    request.getUserId(),
                    request.getPayload()
            );
            return ResponseEntity.ok(new AuthResponse(true, "Publish success", message.getType(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage(), null, null));
        }
    }
}
