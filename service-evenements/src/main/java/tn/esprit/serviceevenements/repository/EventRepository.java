package tn.esprit.serviceevenements.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.serviceevenements.model.Event;

import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByCategory(String category);
    List<Event> findByLocation(String location);
}