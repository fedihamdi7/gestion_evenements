package tn.esprit.serviceutilisateurs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body for POST /api/users/login. */
@Data
public class LoginRequest {

    @Email(message = "email invalide")
    @NotBlank(message = "l'email est obligatoire")
    private String email;

    @NotBlank(message = "le mot de passe est obligatoire")
    private String motDePasse;
}
