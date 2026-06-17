package tn.esprit.serviceevenements.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Health check personnalise visible sur /actuator/health.
 * Verifie que la connexion MongoDB (embarquee) est reellement utilisable,
 * pas seulement que le bean Spring existe.
 */
@Component
@RequiredArgsConstructor
public class MongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            long count = mongoTemplate.getCollection("events").countDocuments();
            return Health.up()
                    .withDetail("database", mongoTemplate.getDb().getName())
                    .withDetail("eventsCount", count)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}