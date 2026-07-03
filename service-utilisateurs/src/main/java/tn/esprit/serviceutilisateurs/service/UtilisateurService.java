package tn.esprit.serviceutilisateurs.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.serviceutilisateurs.client.ReservationClient;
import tn.esprit.serviceutilisateurs.client.ReservationDto;
import tn.esprit.serviceutilisateurs.dto.AuthResponse;
import tn.esprit.serviceutilisateurs.dto.LoginRequest;
import tn.esprit.serviceutilisateurs.dto.RegisterRequest;
import tn.esprit.serviceutilisateurs.dto.UpdateRequest;
import tn.esprit.serviceutilisateurs.dto.UtilisateurAvecReservations;
import tn.esprit.serviceutilisateurs.dto.UtilisateurResponse;
import tn.esprit.serviceutilisateurs.model.Role;
import tn.esprit.serviceutilisateurs.model.Utilisateur;
import tn.esprit.serviceutilisateurs.repository.UtilisateurRepository;

/**
 * All the business logic of the Users microservice lives here.
 * The controllers stay thin and just call these methods.
 */
@Service
@RequiredArgsConstructor   // Lombok generates a constructor for the final fields -> dependency injection
@Slf4j
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final KeycloakService keycloak;              // talks to the Keycloak login server
    private final ReservationClient reservationClient;   // the OpenFeign client

    // ---------- Authentication (delegated to Keycloak) ----------

    /**
     * Register a new user: create it in Keycloak (which owns the password), then keep a small
     * local "profile mirror" row in MySQL so the rest of the app (CRUD, OpenFeign by id) keeps
     * working. The password never touches our database.
     */
    public UtilisateurResponse register(RegisterRequest req) {
        if (repository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email deja utilise: " + req.getEmail());
        }
        // Registration is PUBLIC: a caller must never be able to self-assign a privileged role.
        // Everyone signs up as PARTICIPANT; an ADMIN can promote them afterwards via PUT /api/users/{id}.
        Role role = Role.PARTICIPANT;

        // 1. create in Keycloak (source of truth for identity + password)
        String keycloakId = keycloak.createUser(
                req.getEmail(), req.getPrenom(), req.getNom(), req.getMotDePasse(), role);

        // 2. mirror a profile row locally (no password stored)
        Utilisateur u = Utilisateur.builder()
                .nom(req.getNom())
                .prenom(req.getPrenom())
                .email(req.getEmail())
                .keycloakId(keycloakId)
                .role(role)
                .build();
        return toResponse(repository.save(u));
    }

    /**
     * Login: Keycloak verifies the credentials and issues a JWT. We just hand that token back
     * to the caller, who then sends it as "Authorization: Bearer ..." on secured endpoints.
     */
    public AuthResponse login(LoginRequest req) {
        Map<String, Object> token = keycloak.login(req.getEmail(), req.getMotDePasse());
        return AuthResponse.builder()
                .accessToken((String) token.get("access_token"))
                .refreshToken((String) token.get("refresh_token"))
                .tokenType((String) token.get("token_type"))
                .expiresIn(token.get("expires_in") == null ? null
                        : ((Number) token.get("expires_in")).longValue())
                .build();
    }

    // ---------- CRUD ----------

    public List<UtilisateurResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Directory for any logged-in user: id + name + email (needed to display author/participant
     * names and to pick a chat contact). The ROLE is intentionally omitted — role management stays
     * an ADMIN-only concern, exposed only through findAll().
     */
    public List<UtilisateurResponse> findAllPublic() {
        return repository.findAll().stream()
                .map(u -> UtilisateurResponse.builder()
                        .id(u.getId())
                        .nom(u.getNom())
                        .prenom(u.getPrenom())
                        .email(u.getEmail())
                        .build())
                .toList();
    }

    public UtilisateurResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public UtilisateurResponse findByEmail(String email) {
        return toResponse(repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profil introuvable pour: " + email)));
    }

    public UtilisateurResponse update(Long id, UpdateRequest req) {
        Utilisateur u = getOrThrow(id);
        if (req.getNom() != null)    u.setNom(req.getNom());
        if (req.getPrenom() != null) u.setPrenom(req.getPrenom());
        if (req.getEmail() != null)  u.setEmail(req.getEmail());
        if (req.getRole() != null)   u.setRole(req.getRole());

        // mirror the profile/role change into Keycloak (password changes are handled by Keycloak itself)
        keycloak.updateUser(u.getKeycloakId(), req.getPrenom(), req.getNom(), req.getEmail(), req.getRole());

        return toResponse(repository.save(u));
    }

    public void delete(Long id) {
        Utilisateur u = getOrThrow(id);
        keycloak.deleteUser(u.getKeycloakId());   // remove from Keycloak first
        repository.delete(u);                     // then drop the local mirror row
    }

    // ---------- OpenFeign demo (requirement #3) ----------

    /**
     * Returns the user (from MySQL) enriched with their reservations, fetched from
     * service-reservation (H2) via OpenFeign. If that service is down, we degrade gracefully
     * (empty list + note) instead of failing — so this service runs standalone and the link
     * "lights up" automatically once service-reservation is started and registered in Eureka.
     */
    public UtilisateurAvecReservations getUtilisateurAvecReservations(Long id) {
        UtilisateurResponse utilisateur = toResponse(getOrThrow(id));
        try {
            List<ReservationDto> reservations = reservationClient.getReservationsByUser(id);
            return UtilisateurAvecReservations.builder()
                    .utilisateur(utilisateur)
                    .reservations(reservations)
                    .noteIntegration("Donnees recuperees via OpenFeign depuis 'service-reservation'.")
                    .build();
        } catch (Exception e) {
            log.warn("Appel Feign vers service-reservation echoue (fallback): {}", e.getMessage());
            return UtilisateurAvecReservations.builder()
                    .utilisateur(utilisateur)
                    .reservations(List.of())
                    .noteIntegration("'service-reservation' indisponible -> fallback. L'appel OpenFeign "
                            + "fonctionnera des que ce microservice sera demarre et enregistre dans Eureka.")
                    .build();
        }
    }

    // ---------- helpers ----------

    private Utilisateur getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable: " + id));
    }

    private UtilisateurResponse toResponse(Utilisateur u) {
        return UtilisateurResponse.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .role(u.getRole())
                .build();
    }
}
