package tn.esprit.serviceutilisateurs.messaging;

import lombok.Data;

/** Mirror of the event published by service-reservation over RabbitMQ. */
@Data
public class ReservationCreatedEvent {
    private Long userId;
    private String eventId;
    private String eventTitle;
    private String eventDate;
}
