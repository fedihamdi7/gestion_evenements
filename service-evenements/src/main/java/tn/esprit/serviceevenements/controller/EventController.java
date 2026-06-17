package tn.esprit.serviceevenements.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tn.esprit.serviceevenements.model.Event;
import tn.esprit.serviceevenements.repository.EventRepository;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository repository;

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

    @GetMapping("/{id}")
    public Event getById(@PathVariable String id) {
        return repository.findById(id).orElseThrow();
    }

    @PostMapping
    public Event create(@RequestBody Event event) {
        return repository.save(event);
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable String id, @RequestBody Event event) {
        event.setId(id);
        return repository.save(event);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        repository.deleteById(id);
    }

    // Endpoint dédié pour la communication inter-microservices via OpenFeign
    @GetMapping("/{id}/capacity")
    public Integer getCapacity(@PathVariable String id) {
        return repository.findById(id)
                .map(Event::getCapacity)
                .orElseThrow();
    }
}