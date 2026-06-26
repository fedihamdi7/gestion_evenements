package tn.esprit.serviceavis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client OpenFeign vers service-evenements.
 * Eureka résout lb://service-evenements sans IP codée en dur.
 */
@FeignClient(name = "service-evenements")
public interface EvenementClient {

    @GetMapping("/api/events/{id}")
    EvenementDto getEvenement(@PathVariable("id") String id);

    @GetMapping("/api/events/{id}/capacity")
    Integer getCapacity(@PathVariable("id") String id);
}
