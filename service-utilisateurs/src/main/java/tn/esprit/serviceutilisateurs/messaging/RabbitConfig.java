package tn.esprit.serviceutilisateurs.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper.TypePrecedence;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Consumer-side RabbitMQ config. Declares the queue it listens on and a JSON converter
 * that deserializes using the LISTENER's parameter type (INFERRED) — so it ignores the
 * producer's __TypeId__ header (which points at a class in the other service's package).
 */
@Configuration
public class RabbitConfig {

    public static final String QUEUE = "reservation.notifications";

    @Bean
    public Queue reservationQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTypePrecedence(TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(mapper);
        return converter;
    }
}
