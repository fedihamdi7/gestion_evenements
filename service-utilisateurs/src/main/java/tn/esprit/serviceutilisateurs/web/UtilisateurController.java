package tn.esprit.serviceutilisateurs.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.esprit.serviceutilisateurs.dto.UpdateRequest;
import tn.esprit.serviceutilisateurs.dto.UtilisateurAvecReservations;
import tn.esprit.serviceutilisateurs.dto.UtilisateurResponse;
import tn.esprit.serviceutilisateurs.service.UtilisateurService;

/**
 * User CRUD + the OpenFeign demo endpoint. All under /api/users (-> gateway route /api/users/**).
 *
 * Authorization:
 *   GET    /api/users            -> ADMIN only (full directory with emails + roles)
 *   GET    /api/users/public     -> any logged-in user (id + name only, for display)
 *   GET    /api/users/me         -> any logged-in user (their own profile, from the JWT)
 *   GET    /api/users/{id}       -> any logged-in user
 *   PUT    /api/users/{id}       -> ADMIN only (cannot change one's OWN role)
 *   DELETE /api/users/{id}       -> ADMIN only (cannot delete one's OWN account)
 *   GET    /api/users/{id}/reservations   (OpenFeign -> service-reservation)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService service;

    /** Full user directory — sensitive (emails, roles), so ADMIN only. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UtilisateurResponse> findAll() {
        return service.findAll();
    }

    /** Non-sensitive list (id + first/last name) so any page can resolve a userId to a name. */
    @GetMapping("/public")
    public List<UtilisateurResponse> findAllPublic() {
        return service.findAllPublic();
    }

    /** The current user's own profile, resolved from the JWT — no need to expose the whole list. */
    @GetMapping("/me")
    public UtilisateurResponse me(@AuthenticationPrincipal Jwt jwt) {
        return service.findByEmail(currentEmail(jwt));
    }

    @GetMapping("/{id}")
    public UtilisateurResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UtilisateurResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        UtilisateurResponse target = service.findById(id);
        boolean self = target.getEmail() != null && target.getEmail().equalsIgnoreCase(currentEmail(jwt));
        if (self && request.getRole() != null && request.getRole() != target.getRole()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vous ne pouvez pas modifier votre propre rôle.");
        }
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        UtilisateurResponse target = service.findById(id);
        if (target.getEmail() != null && target.getEmail().equalsIgnoreCase(currentEmail(jwt))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vous ne pouvez pas supprimer votre propre compte.");
        }
        service.delete(id);
    }

    /** OpenFeign demo: returns the user enriched with their reservations from service-reservation. */
    @GetMapping("/{id}/reservations")
    public UtilisateurAvecReservations getUtilisateurAvecReservations(@PathVariable Long id) {
        return service.getUtilisateurAvecReservations(id);
    }

    /** The logged-in user's email, taken from the Keycloak token (never trusted from the body). */
    private static String currentEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return email != null ? email : jwt.getClaimAsString("preferred_username");
    }
}
