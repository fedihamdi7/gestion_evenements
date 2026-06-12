package tn.esprit.serviceutilisateurs.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ===== THE OpenFeign CLIENT (requirement #3) =====
 *
 * This interface lets service-utilisateurs call service-evenements over HTTP as if it were a
 * local Java method — we never write URLs or use a RestTemplate by hand.
 *
 * - name = "service-evenements": Feign asks Eureka for the address of the service registered under
 *   that name (no hard-coded IP/port). That is why both services must be on Eureka.
 * - The @GetMapping below describes the remote endpoint Nour must expose:
 *       GET /api/events/organisateur/{organisateurId}  ->  List<EvenementDto>
 *
 * The actual call is wrapped in a try/catch in UtilisateurService, so if service-evenements is not
 * running yet, this service still works (graceful fallback) instead of crashing.
 */
@FeignClient(name = "service-evenements")
public interface EvenementClient {

    @GetMapping("/api/events/organisateur/{organisateurId}")
    List<EvenementDto> getEvenementsParOrganisateur(@PathVariable("organisateurId") Long organisateurId);
}
