package tn.esprit.serviceutilisateurs.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes reservation.created events published by service-reservation over RabbitMQ.
 * This is the ASYNC counterpart to the (synchronous) OpenFeign calls between services.
 */
@Component
@Slf4j
public class ReservationNotificationListener {

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onReservationCreated(ReservationCreatedEvent event) {
        log.info("🔔 [RabbitMQ] Notification : l'utilisateur #{} a réservé l'événement '{}' ({}).",
                event.getUserId(), event.getEventTitle(), event.getEventDate());
    }
}
