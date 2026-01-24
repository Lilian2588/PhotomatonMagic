package com.example.face_analyze.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ActiveMQConfig {

    public static final String FACE_REQUEST_QUEUE = "FaceRequestQueue";

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {

        ActiveMQConnectionFactory factory =
                new ActiveMQConnectionFactory(brokerUrl);

        factory.setTrustedPackages(
                List.of(
                        "java.util",
                        "com.example.face_analyze.DTO",
                        "com.example.avatar.DTO"
                )
        );

        return factory;
    }
}
