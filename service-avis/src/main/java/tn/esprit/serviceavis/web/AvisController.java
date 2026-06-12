package tn.esprit.serviceavis.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.serviceavis.dto.AvisRequest;
import tn.esprit.serviceavis.dto.AvisResponse;
import tn.esprit.serviceavis.dto.AvisUpdateRequest;
import tn.esprit.serviceavis.service.AvisService;

import java.util.List;

/**
 * Endpoints du Service Avis — tous accessibles via le gateway sous /api/avis/**
 *
 *   POST   /api/avis                              -> créer un avis
 *   GET    /api/avis                              -> lister tous les avis
 *   GET    /api/avis/{id}                         -> un avis par id
 *   GET    /api/avis/evenement/{evenementId}      -> avis d'un événement
 *   GET    /api/avis/utilisateur/{utilisateurId}  -> avis d'un utilisateur
 *   PUT    /api/avis/{id}                         -> modifier un avis
 *   DELETE /api/avis/{id}                         -> supprimer un avis
 */
@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
public class AvisController {

    private final AvisService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvisResponse create(@Valid @RequestBody AvisRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<AvisResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AvisResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/evenement/{evenementId}")
    public List<AvisResponse> findByEvenement(@PathVariable Long evenementId) {
        return service.findByEvenement(evenementId);
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<AvisResponse> findByUtilisateur(@PathVariable Long utilisateurId) {
        return service.findByUtilisateur(utilisateurId);
    }

    @PutMapping("/{id}")
    public AvisResponse update(@PathVariable Long id, @Valid @RequestBody AvisUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
