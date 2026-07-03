package com.example.reservationevenement.controller;

import com.example.reservationevenement.client.EventClient;
import com.example.reservationevenement.dto.EventDto;
import com.example.reservationevenement.messaging.RabbitConfig;
import com.example.reservationevenement.messaging.ReservationCreatedEvent;
import com.example.reservationevenement.model.Reservation;
import com.example.reservationevenement.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationRepository repository;
    private final EventClient eventClient;
    private final RabbitTemplate rabbitTemplate;

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
        // 1. No double booking: a user cannot reserve the same event twice.
        boolean alreadyBooked = repository.findByUserId(reservation.getUserId()).stream()
                .anyMatch(r -> reservation.getEventId() != null
                        && reservation.getEventId().equals(r.getEventId()));
        if (alreadyBooked) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez deja reserve cet evenement.");
        }

        // 2. Capacity check: fetch the event's capacity from service-evenements (Feign) and refuse
        //    the booking if the event is already full. If service-evenements is unreachable we let
        //    the booking through (don't block the demo on a monitoring/availability check).
        try {
            Integer capacity = eventClient.getCapacity(reservation.getEventId());
            if (capacity != null) {
                long taken = repository.findByEventId(reservation.getEventId()).size();
                if (taken >= capacity) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Evenement complet : plus de places disponibles.");
                }
            }
        } catch (ResponseStatusException e) {
            throw e; // re-throw our own "full" error
        } catch (Exception e) {
            log.warn("Verification de capacite impossible (service-evenements indisponible?): {}", e.getMessage());
        }

        Reservation saved = repository.save(reservation);
        publishReservationCreated(saved);
        return saved;
    }

    // Async notification to other services via RabbitMQ. Wrapped so a broker outage
    // never breaks the booking itself.
    private void publishReservationCreated(Reservation r) {
        try {
            ReservationCreatedEvent event = new ReservationCreatedEvent(
                    r.getUserId(), r.getEventId(), r.getEventTitle(), r.getEventDate());
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
            log.info("Published reservation.created for user {} / event '{}'", r.getUserId(), r.getEventTitle());
        } catch (Exception e) {
            log.warn("RabbitMQ publish skipped (broker unavailable?): {}", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
