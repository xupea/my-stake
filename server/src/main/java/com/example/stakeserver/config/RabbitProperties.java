package com.example.stakeserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitProperties {

    private String exchange = "stake.topic";
    private String queue = "ws.push";
    private String bindingPattern = "push.#";
    private String defaultTopic = "ws.announcements";
    private long publishIntervalMs = 5000;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getBindingPattern() {
        return bindingPattern;
    }

    public void setBindingPattern(String bindingPattern) {
        this.bindingPattern = bindingPattern;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    public long getPublishIntervalMs() {
        return publishIntervalMs;
    }

    public void setPublishIntervalMs(long publishIntervalMs) {
        this.publishIntervalMs = publishIntervalMs;
    }
}
