package tn.esprit.serviceutilisateurs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tn.esprit.serviceutilisateurs.model.Role;

/**
 * Body for POST /api/users/register.
 * The @NotBlank/@Email/@Size annotations are checked automatically thanks to @Valid in the controller;
 * invalid bodies get a 400 with a clear message (see GlobalExceptionHandler).
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "le nom est obligatoire")
    private String nom;

    @NotBlank(message = "le prenom est obligatoire")
    private String prenom;

    @Email(message = "email invalide")
    @NotBlank(message = "l'email est obligatoire")
    private String email;

    @NotBlank(message = "le mot de passe est obligatoire")
    @Size(min = 6, message = "le mot de passe doit faire au moins 6 caracteres")
    private String motDePasse;

    /** Optional. If omitted, the service defaults to PARTICIPANT. */
    private Role role;
}
