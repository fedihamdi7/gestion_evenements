package tn.esprit.serviceevenements.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Représente un événement (concert, conférence, atelier, etc.)")
public class Event {

    @Id
    @Schema(description = "Identifiant MongoDB généré automatiquement", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotBlank(message = "Le titre est obligatoire")
    @Schema(description = "Titre de l'événement", example = "Concert Jazz")
    private String title;

    @Schema(description = "Catégorie de l'événement", example = "concert")
    private String category;

    @Schema(description = "Lieu de l'événement", example = "Tunis")
    private String location;

    @NotNull(message = "La date est obligatoire")
    @FutureOrPresent(message = "La date doit être aujourd'hui ou dans le futur")
    @Schema(description = "Date de l'événement (format ISO yyyy-MM-dd)", example = "2026-06-20")
    private LocalDate date;

    @NotNull(message = "La capacité est obligatoire")
    @Positive(message = "La capacité doit être un nombre positif")
    @Schema(description = "Capacité maximale de participants", example = "100")
    private Integer capacity;
}