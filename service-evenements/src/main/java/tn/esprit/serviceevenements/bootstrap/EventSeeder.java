package tn.esprit.serviceevenements.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.serviceevenements.model.Event;
import tn.esprit.serviceevenements.repository.EventRepository;

import java.time.LocalDate;

/**
 * Seeds a few demo events on startup if the collection is empty, so the frontend
 * has events to list/book/rate. (MongoDB is embedded, so this runs every boot.)
 */
@Component
@RequiredArgsConstructor
public class EventSeeder implements CommandLineRunner {

    private final EventRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new Event(null, "Concert ESPRIT", "Musique", "Auditorium ESPRIT", LocalDate.of(2026, 7, 1), 100));
        repository.save(new Event(null, "Hackathon Tunis", "Tech", "El Ghazala", LocalDate.of(2026, 8, 15), 50));
        repository.save(new Event(null, "Conference SOA", "Tech", "Amphi A", LocalDate.of(2026, 7, 20), 80));
        repository.save(new Event(null, "Gala de fin d'annee", "Social", "Hotel Laico", LocalDate.of(2026, 9, 5), 200));
    }
}
