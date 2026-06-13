package com.example.reservationevenement.controller;

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

    @GetMapping
    public List<Reservation> getAll() {
        return repository.findAll();
    }

    // This is the endpoint called by service-utilisateurs via OpenFeign
    @GetMapping("/user/{userId}")
    public List<Reservation> getByUser(@PathVariable Long userId) {
        return repository.findByUserId(userId);
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
