package tn.esprit.serviceevenements.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.serviceevenements.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByCategory(String category);
    List<Event> findByLocation(String location);
    List<Event> findByDate(LocalDate date);
}