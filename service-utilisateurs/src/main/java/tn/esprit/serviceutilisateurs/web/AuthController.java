package tn.esprit.serviceutilisateurs.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.esprit.serviceutilisateurs.dto.AuthResponse;
import tn.esprit.serviceutilisateurs.dto.LoginRequest;
import tn.esprit.serviceutilisateurs.dto.RegisterRequest;
import tn.esprit.serviceutilisateurs.dto.UtilisateurResponse;
import tn.esprit.serviceutilisateurs.service.UtilisateurService;

/**
 * Authentication endpoints. Mapped under /api/users so the gateway route /api/users/** reaches them.
 * Both are PUBLIC (no token needed). Behind the scenes they talk to Keycloak.
 *   POST /api/users/register  -> creates the user in Keycloak
 *   POST /api/users/login     -> returns a real Keycloak JWT
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurService service;

    @PostMapping("/register")
    public UtilisateurResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
