package com.example.reservationevenement.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload published to RabbitMQ when a reservation is created. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreatedEvent {
    private Long userId;
    private String eventId;
    private String eventTitle;
    private String eventDate;
}
