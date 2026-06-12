package tn.esprit.serviceutilisateurs.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.serviceutilisateurs.client.EvenementDto;

/**
 * Response of GET /api/users/{id}/evenements — the OpenFeign demo.
 * It combines local user data (this service) with the user's events fetched remotely
 * from service-evenements via Feign. "noteIntegration" explains what happened
 * (real data vs fallback) so the demo is self-describing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurAvecEvenements {
    private UtilisateurResponse utilisateur;
    private List<EvenementDto> evenements;
    private String noteIntegration;
}
