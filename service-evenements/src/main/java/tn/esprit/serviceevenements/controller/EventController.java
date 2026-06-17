package tn.esprit.serviceevenements.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.serviceevenements.model.Event;
import tn.esprit.serviceevenements.repository.EventRepository;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Événements", description = "Gestion CRUD des événements, recherche et filtrage")
public class EventController {

    private final EventRepository repository;

    @Operation(summary = "Lister les événements", description = "Renvoie tous les événements, ou filtre par catégorie, lieu ou date si fourni.")
    @ApiResponse(responseCode = "200", description = "Liste des événements correspondant aux critères")
    @GetMapping
    public List<Event> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (category != null) {
            return repository.findByCategory(category);
        }
        if (location != null) {
            return repository.findByLocation(location);
        }
        if (date != null) {
            return repository.findByDate(date);
        }
        return repository.findAll();
    }

    @Operation(summary = "Récupérer un événement par id")
    @ApiResponse(responseCode = "200", description = "Événement trouvé")
    @ApiResponse(responseCode = "404", description = "Aucun événement avec cet id")
    @GetMapping("/{id}")
    public Event getById(@PathVariable String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable: " + id));
    }

    @Operation(summary = "Créer un événement")
    @ApiResponse(responseCode = "200", description = "Événement créé avec succès")
    @PostMapping
    public Event create(@Valid @RequestBody Event event) {
        return repository.save(event);
    }

    @Operation(summary = "Mettre à jour un événement existant")
    @ApiResponse(responseCode = "200", description = "Événement mis à jour")
    @PutMapping("/{id}")
    public Event update(@PathVariable String id, @Valid @RequestBody Event event) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable: " + id);
        }
        event.setId(id);
        return repository.save(event);
    }

    @Operation(summary = "Supprimer un événement")
    @ApiResponse(responseCode = "204", description = "Événement supprimé")
    @ApiResponse(responseCode = "404", description = "Aucun événement avec cet id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable: " + id);
        }
        repository.deleteById(id);
    }

    @Operation(summary = "Capacité d'un événement", description = "Endpoint dédié à la communication inter-microservices via OpenFeign (ex: appelé par service-avis).")
    @ApiResponse(responseCode = "200", description = "Capacité maximale de l'événement")
    @ApiResponse(responseCode = "404", description = "Aucun événement avec cet id")
    @GetMapping("/{id}/capacity")
    public Integer getCapacity(@PathVariable String id) {
        return repository.findById(id)
                .map(Event::getCapacity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable: " + id));
    }

    @Operation(summary = "Statistiques des événements", description = "Nombre d'événements regroupés par catégorie.")
    @ApiResponse(responseCode = "200", description = "Statistiques calculées avec succès")
    @GetMapping("/stats")
    public java.util.Map<String, Long> getStats() {
        return repository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getCategory() == null ? "non-categorise" : e.getCategory(),
                        java.util.stream.Collectors.counting()));
    }
}