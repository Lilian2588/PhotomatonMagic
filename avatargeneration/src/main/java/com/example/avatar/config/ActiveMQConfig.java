package com.example.avatar.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ActiveMQConfig {

    public static final String AVATAR_REQUEST_QUEUE = "AvatarRequestQueue";

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);

        // Autoriser la classe DTO
        factory.setTrustedPackages(
                List.of("java.util", "com.example.avatar.model")
        );

        return factory;
    }
}
