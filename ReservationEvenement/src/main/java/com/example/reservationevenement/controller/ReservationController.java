package com.example.reservationevenement.controller;

import com.example.reservationevenement.client.EventClient;
import com.example.reservationevenement.dto.EventDto;
import com.example.reservationevenement.model.Reservation;
import com.example.reservationevenement.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository repository;
    private final EventClient eventClient;

    @GetMapping
    public List<Reservation> getAll() {
        return repository.findAll();
    }

    // Endpoint to retrieve events from service-evenements via OpenFeign
    @GetMapping("/events")
    public List<EventDto> getAllEventsFromOtherService() {
        return eventClient.getAllEvents();
    }

    // Endpoint to retrieve a specific event from service-evenements via OpenFeign
    @GetMapping("/events/{id}")
    public EventDto getEventFromOtherService(@PathVariable String id) {
        return eventClient.getEventById(id);
    }

    // This is the endpoint called by service-utilisateurs via OpenFeign
    @GetMapping("/user/{userId}")
    public List<Reservation> getByUser(@PathVariable Long userId) {
        return repository.findByUserId(userId);
    }

    // Who booked a given event — used by the event-details page.
    @GetMapping("/event/{eventId}")
    public List<Reservation> getByEvent(@PathVariable String eventId) {
        return repository.findByEventId(eventId);
    }

    @PostMapping
    public Reservation create(@RequestBody Reservation reservation) {
        return repository.save(reservation);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
