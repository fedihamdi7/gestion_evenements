package tn.esprit.serviceutilisateurs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.serviceutilisateurs.model.Role;

/**
 * What we send BACK to clients. Notice there is NO motDePasse field here:
 * the password hash must never leave the service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Role role;
}
