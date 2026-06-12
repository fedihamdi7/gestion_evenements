package tn.esprit.serviceutilisateurs.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The shape of an "événement" as returned by Nour's service-evenements.
 * This is the CONTRACT between the two microservices: as long as service-evenements
 * returns JSON with these fields, OpenFeign maps it into this object automatically.
 * (Kept as a simple String date for now to avoid coupling on a date format this week.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvenementDto {
    private Long id;
    private String titre;
    private String description;
    private String date;
    private String lieu;
    private Long organisateurId;
}
