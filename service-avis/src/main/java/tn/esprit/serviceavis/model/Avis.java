package tn.esprit.serviceavis.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "avis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long utilisateurId;

    @Column(nullable = false)
    private String evenementId;

    @Column(nullable = false)
    private Integer note;

    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateCreation;
}
