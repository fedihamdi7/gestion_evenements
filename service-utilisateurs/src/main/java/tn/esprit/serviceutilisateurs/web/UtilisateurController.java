package tn.esprit.serviceutilisateurs.web;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.esprit.serviceutilisateurs.dto.UpdateRequest;
import tn.esprit.serviceutilisateurs.dto.UtilisateurAvecEvenements;
import tn.esprit.serviceutilisateurs.dto.UtilisateurResponse;
import tn.esprit.serviceutilisateurs.service.UtilisateurService;

/**
 * User CRUD + the OpenFeign demo endpoint. All under /api/users (-> gateway route /api/users/**).
 *   GET    /api/users
 *   GET    /api/users/{id}
 *   PUT    /api/users/{id}
 *   DELETE /api/users/{id}
 *   GET    /api/users/{id}/evenements   (OpenFeign -> service-evenements)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService service;

    @GetMapping
    public List<UtilisateurResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UtilisateurResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public UtilisateurResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /** OpenFeign demo: returns the user enriched with their events from service-evenements. */
    @GetMapping("/{id}/evenements")
    public UtilisateurAvecEvenements getUtilisateurAvecEvenements(@PathVariable Long id) {
        return service.getUtilisateurAvecEvenements(id);
    }
}
