package tn.esprit.serviceutilisateurs.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.serviceutilisateurs.model.Role;
import tn.esprit.serviceutilisateurs.model.Utilisateur;
import tn.esprit.serviceutilisateurs.repository.UtilisateurRepository;

/**
 * On startup, if the table is empty, insert three demo users (one per role) with BCrypt-hashed
 * passwords. This gives Postman something to query immediately, and guarantees user id=1 exists
 * for the OpenFeign demo (GET /api/users/1/reservations). Password for all three: "password".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(Utilisateur.builder()
                .nom("Hamdi").prenom("Fedi").email("fedi@esprit.tn")
                .motDePasse(passwordEncoder.encode("password")).role(Role.ADMIN).build());
        repository.save(Utilisateur.builder()
                .nom("Ben Salah").prenom("Nour").email("nour@esprit.tn")
                .motDePasse(passwordEncoder.encode("password")).role(Role.ORGANISATEUR).build());
        repository.save(Utilisateur.builder()
                .nom("Trabelsi").prenom("Safaa").email("safaa@esprit.tn")
                .motDePasse(passwordEncoder.encode("password")).role(Role.PARTICIPANT).build());
        log.info("Seed: 3 utilisateurs de demonstration crees (mot de passe: 'password').");
    }
}
