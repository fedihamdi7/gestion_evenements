package com.example.reservationevenement.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the RabbitMQ topology (auto-created on the broker at startup) and a JSON
 * message converter so the event is sent as JSON.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "events.exchange";
    public static final String QUEUE = "reservation.notifications";
    public static final String ROUTING_KEY = "reservation.created";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue reservationQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding reservationBinding(Queue reservationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(reservationQueue).to(eventsExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
