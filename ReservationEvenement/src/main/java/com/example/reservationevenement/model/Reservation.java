package com.example.reservationevenement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    // Mongo id of the booked event (links to service-evenements + service-avis)
    private String eventId;

    private String eventTitle;

    private String eventDate;

    private String status; // CONFIRMED, PENDING, CANCELLED
}
