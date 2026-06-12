package tn.esprit.serviceutilisateurs.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import tn.esprit.serviceutilisateurs.model.Role;

/**
 * Body for PUT /api/users/{id}. Every field is optional: only the non-null ones are applied,
 * so the client can update just the name, or just the role, etc.
 */
@Data
public class UpdateRequest {

    private String nom;

    private String prenom;

    @Email(message = "email invalide")
    private String email;

    private String motDePasse;   // if provided, it is re-hashed with BCrypt

    private Role role;
}
