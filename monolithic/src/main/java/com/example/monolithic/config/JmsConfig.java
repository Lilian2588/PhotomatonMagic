package com.example.monolithic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJms
public class JmsConfig {

    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        // Existing mapping
        typeIdMappings.put("AvatarResult", com.example.monolithic.DTO.AvatarGenerationResultDTO.class);
        
        // New mappings for Notifications (covering both potential old class name and simple name)
        typeIdMappings.put("com.example.ms_notification.model.Notification", com.example.monolithic.model.Notification.class);
        typeIdMappings.put("Notification", com.example.monolithic.model.Notification.class);
        
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }
}
