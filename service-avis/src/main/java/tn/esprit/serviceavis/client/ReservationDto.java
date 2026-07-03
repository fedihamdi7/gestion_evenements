package tn.esprit.serviceavis.client;

import lombok.Data;

/** Minimal view of a reservation returned by service-reservation (only what we need to check). */
@Data
public class ReservationDto {
    private Long userId;
    private String eventId;
}
