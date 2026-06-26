package tn.esprit.serviceutilisateurs.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.serviceutilisateurs.client.ReservationDto;

/**
 * Response of GET /api/users/{id}/reservations — the OpenFeign demo.
 * Combines local user data (this service / MySQL) with the user's reservations fetched remotely
 * from service-reservation (H2) via Feign. "noteIntegration" describes what happened.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurAvecReservations {
    private UtilisateurResponse utilisateur;
    private List<ReservationDto> reservations;
    private String noteIntegration;
}
