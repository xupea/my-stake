package com.example.stakeserver.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitConfig {

    @Bean
    public TopicExchange topicExchange(RabbitProperties rabbitProperties) {
        return new TopicExchange(rabbitProperties.getExchange(), true, false);
    }

    @Bean
    public Queue wsPushQueue(RabbitProperties rabbitProperties) {
        return new Queue(rabbitProperties.getQueue(), true);
    }

    @Bean
    public Binding wsPushBinding(
            Queue wsPushQueue,
            TopicExchange topicExchange,
            RabbitProperties rabbitProperties
    ) {
        return BindingBuilder.bind(wsPushQueue)
                .to(topicExchange)
                .with(rabbitProperties.getBindingPattern());
    }
}
