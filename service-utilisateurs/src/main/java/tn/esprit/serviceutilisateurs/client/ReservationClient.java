package tn.esprit.serviceutilisateurs.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ===== OpenFeign CLIENT (inter-service communication, requirement #3) =====
 *
 * Lets service-utilisateurs call service-reservation over HTTP as if it were a local method.
 * - name = "service-reservation": Feign asks Eureka for that service's address by NAME
 *   (no hard-coded IP/port). That is why both services must be registered in Eureka.
 * - The @GetMapping describes the remote endpoint to call:
 *       GET /api/reservations/user/{userId}  ->  List<ReservationDto>
 */
@FeignClient(name = "service-reservation")
public interface ReservationClient {

    @GetMapping("/api/reservations/user/{userId}")
    List<ReservationDto> getReservationsByUser(@PathVariable Long userId);
}
