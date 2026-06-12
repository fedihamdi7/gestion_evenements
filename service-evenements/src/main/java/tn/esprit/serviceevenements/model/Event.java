package tn.esprit.serviceevenements.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    private String id;

    @NotBlank
    private String title;

    private String category;

    private String location;

    private LocalDate date;

    private Integer capacity;
}