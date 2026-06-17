package tn.esprit.serviceavis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisResponse {
    private Long id;
    private Long utilisateurId;
    private String evenementId;
    private Integer note;
    private String commentaire;
    private String dateCreation;
}