package tn.esprit.serviceutilisateurs.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.serviceutilisateurs.client.EvenementClient;
import tn.esprit.serviceutilisateurs.client.EvenementDto;
import tn.esprit.serviceutilisateurs.dto.LoginRequest;
import tn.esprit.serviceutilisateurs.dto.RegisterRequest;
import tn.esprit.serviceutilisateurs.dto.UpdateRequest;
import tn.esprit.serviceutilisateurs.dto.UtilisateurAvecEvenements;
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
    private final PasswordEncoder passwordEncoder;
    private final EvenementClient evenementClient;   // the OpenFeign client

    // ---------- Authentication ----------

    /** Register a new user: validate uniqueness, hash the password, save. */
    public UtilisateurResponse register(RegisterRequest req) {
        if (repository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email deja utilise: " + req.getEmail());
        }
        Utilisateur u = Utilisateur.builder()
                .nom(req.getNom())
                .prenom(req.getPrenom())
                .email(req.getEmail())
                .motDePasse(passwordEncoder.encode(req.getMotDePasse()))    // BCrypt hash
                .role(req.getRole() != null ? req.getRole() : Role.PARTICIPANT)
                .build();
        return toResponse(repository.save(u));
    }

    /** Login: check the email exists and the password matches the stored BCrypt hash. */
    public UtilisateurResponse login(LoginRequest req) {
        Utilisateur u = repository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        if (!passwordEncoder.matches(req.getMotDePasse(), u.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        return toResponse(u);
    }

    // ---------- CRUD ----------

    public List<UtilisateurResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public UtilisateurResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public UtilisateurResponse update(Long id, UpdateRequest req) {
        Utilisateur u = getOrThrow(id);
        if (req.getNom() != null)        u.setNom(req.getNom());
        if (req.getPrenom() != null)     u.setPrenom(req.getPrenom());
        if (req.getEmail() != null)      u.setEmail(req.getEmail());
        if (req.getRole() != null)       u.setRole(req.getRole());
        if (req.getMotDePasse() != null) u.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        return toResponse(repository.save(u));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable: " + id);
        }
        repository.deleteById(id);
    }

    // ---------- OpenFeign demo (requirement #3) ----------

    /**
     * Returns the user enriched with the events they organise, fetched from service-evenements
     * via OpenFeign. If that service is not up yet, we degrade gracefully (empty list + note)
     * instead of failing — so this service runs standalone and the link "lights up" automatically
     * once Nour's service is started and registered in Eureka.
     */
    public UtilisateurAvecEvenements getUtilisateurAvecEvenements(Long id) {
        UtilisateurResponse utilisateur = toResponse(getOrThrow(id));
        try {
            List<EvenementDto> evenements = evenementClient.getEvenementsParOrganisateur(id);
            return UtilisateurAvecEvenements.builder()
                    .utilisateur(utilisateur)
                    .evenements(evenements)
                    .noteIntegration("Donnees recuperees via OpenFeign depuis 'service-evenements'.")
                    .build();
        } catch (Exception e) {
            log.warn("Appel Feign vers service-evenements echoue (fallback): {}", e.getMessage());
            return UtilisateurAvecEvenements.builder()
                    .utilisateur(utilisateur)
                    .evenements(List.of())
                    .noteIntegration("'service-evenements' indisponible -> fallback. L'appel OpenFeign "
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
