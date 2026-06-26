package tn.esprit.serviceavis.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvenementDto {
    private String id;
    private String title;
    private String category;
    private String location;
    private String date;
    private Integer capacity;
}
