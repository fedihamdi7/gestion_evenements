package tn.esprit.serviceutilisateurs.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign client — service-utilisateurs calls service-reservation.
 * name = "service-reservation" matches spring.application.name in ReservationEvenement.
 * Eureka resolves the address by name, no hard-coded IP/port needed.
 */
@FeignClient(name = "service-reservation")
public interface EvenementClient {

    @GetMapping("/api/reservations/user/{userId}")
    List<EvenementDto> getReservationsByUser(@PathVariable Long userId);
}
