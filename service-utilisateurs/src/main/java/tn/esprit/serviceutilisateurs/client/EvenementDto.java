package tn.esprit.serviceutilisateurs.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirror of ReservationEvenement's Reservation model.
 * Fields must match what service-reservation returns in JSON.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvenementDto {
    private Long id;
    private Long userId;
    private String eventTitle;
    private String eventDate;
    private String status;
}
