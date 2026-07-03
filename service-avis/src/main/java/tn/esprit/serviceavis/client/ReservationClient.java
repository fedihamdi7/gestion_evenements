package tn.esprit.serviceavis.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client OpenFeign vers service-reservation.
 * Sert a verifier qu'un utilisateur a bien reserve un evenement avant de pouvoir le noter.
 * Eureka resout lb://service-reservation sans IP codee en dur.
 */
@FeignClient(name = "service-reservation")
public interface ReservationClient {

    @GetMapping("/api/reservations/event/{eventId}")
    List<ReservationDto> getReservationsByEvent(@PathVariable("eventId") String eventId);
}
