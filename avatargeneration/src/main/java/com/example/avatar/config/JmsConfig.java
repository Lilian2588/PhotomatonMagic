package com.example.avatar.config;

import com.example.avatar.DTO.FaceAnalyzeResultDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JmsConfig {

    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();

        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        // On ne garde que FaceResult car c'est ce que l'Orchestrateur REÇOIT
        typeIdMappings.put("FaceResult", FaceAnalyzeResultDTO.class);

        converter.setTypeIdMappings(typeIdMappings);
        return converter;
    }
}