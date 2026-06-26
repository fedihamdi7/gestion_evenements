package tn.esprit.serviceutilisateurs.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.serviceutilisateurs.dto.RegisterRequest;
import tn.esprit.serviceutilisateurs.model.Role;
import tn.esprit.serviceutilisateurs.repository.UtilisateurRepository;
import tn.esprit.serviceutilisateurs.service.UtilisateurService;

/**
 * On startup, if the local table is empty, create three demo users (one per role) by going
 * through the normal register flow — so they end up BOTH in Keycloak and in the local mirror,
 * and user id=1 exists for the OpenFeign demo (GET /api/users/1/reservations).
 *
 * If Keycloak is not running yet, seeding is skipped with a warning (the service still starts).
 * Password for all three demo users: "password".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UtilisateurRepository repository;
    private final UtilisateurService service;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        try {
            seed("Hamdi", "Fedi", "fedi@esprit.tn", Role.ADMIN);
            seed("Ben Salah", "Nour", "nour@esprit.tn", Role.ORGANISATEUR);
            seed("Trabelsi", "Safaa", "safaa@esprit.tn", Role.PARTICIPANT);
            log.info("Seed: 3 utilisateurs de demonstration crees dans Keycloak + MySQL (mot de passe: 'password').");
        } catch (Exception e) {
            log.warn("Seed ignore: Keycloak ne repond pas encore ({}). "
                    + "Demarrez Keycloak (./keycloak/run-keycloak.ps1) puis relancez ce service.", e.getMessage());
        }
    }

    private void seed(String nom, String prenom, String email, Role role) {
        RegisterRequest req = new RegisterRequest();
        req.setNom(nom);
        req.setPrenom(prenom);
        req.setEmail(email);
        req.setMotDePasse("password");
        req.setRole(role);
        service.register(req);
    }
}
