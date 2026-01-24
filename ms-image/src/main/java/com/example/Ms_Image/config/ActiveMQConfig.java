package com.example.Ms_Image.config;

import com.example.Ms_Image.DTO.AvatarGenerationResultDTO;
import com.example.Ms_Image.DTO.TransformRequest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJms
public class ActiveMQConfig {

    public static final String IMAGE_TRANSFORM_QUEUE = "image.transform.queue";

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        // URL par défaut d'ActiveMQ
        return new ActiveMQConnectionFactory(brokerUrl);
    }

    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("AvatarResult", AvatarGenerationResultDTO.class);
        // AJOUTE CETTE LIGNE :
        typeIdMappings.put("TransformRequest", TransformRequest.class);

        converter.setTypeIdMappings(typeIdMappings);
        return converter;
    }
}