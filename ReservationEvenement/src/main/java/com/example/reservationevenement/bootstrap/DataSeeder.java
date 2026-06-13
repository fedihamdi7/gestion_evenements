package com.example.reservationevenement.bootstrap;

import com.example.reservationevenement.model.Reservation;
import com.example.reservationevenement.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ReservationRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        // userId=1 = Fedi, userId=2 = Nour (matches the seeded users in service-utilisateurs)
        repository.save(Reservation.builder().userId(1L).eventTitle("Concert ESPRIT").eventDate("2026-07-01").status("CONFIRMED").build());
        repository.save(Reservation.builder().userId(1L).eventTitle("Hackathon Tunis").eventDate("2026-08-15").status("PENDING").build());
        repository.save(Reservation.builder().userId(2L).eventTitle("Conference SOA").eventDate("2026-07-20").status("CONFIRMED").build());
    }
}
