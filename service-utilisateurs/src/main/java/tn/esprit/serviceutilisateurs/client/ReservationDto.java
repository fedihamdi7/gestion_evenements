package tn.esprit.serviceutilisateurs.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirror of the Reservation model in service-reservation (the ReservationEvenement project).
 * Fields must match the JSON returned by that service so OpenFeign can map it automatically.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    private Long id;
    private Long userId;
    private String eventTitle;
    private String eventDate;
    private String status;
}
