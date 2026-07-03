package com.example.reservationevenement.client;

import com.example.reservationevenement.dto.EventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "service-evenements")
public interface EventClient {

    @GetMapping("/api/events")
    List<EventDto> getAllEvents();

    @GetMapping("/api/events/{id}")
    EventDto getEventById(@PathVariable("id") String id);

    @GetMapping("/api/events/{id}/capacity")
    Integer getCapacity(@PathVariable("id") String id);
}
