package tn.esprit.serviceavis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.serviceavis.client.EvenementDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisAvecDetailsResponse {
    private EvenementDto evenement;
    private List<AvisResponse> avis;
    private Double noteMoyenne;
    private String note;
}
