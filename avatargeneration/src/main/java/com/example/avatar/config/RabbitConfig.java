package com.example.avatar.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // Queue pour envoyer les demandes (Spring -> Python)
    @Bean
    public Queue audioQueue() {
        return new Queue("audio_request_queue", true); // true = durable
    }

    //  Queue pour recevoir les réponses (Python -> Spring)
    @Bean
    public Queue responseQueue() {
        return new Queue("audio_response_queue", true);
    }
}
