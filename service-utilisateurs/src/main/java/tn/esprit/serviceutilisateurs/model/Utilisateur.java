package tn.esprit.serviceutilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity = one row in the MySQL table "utilisateurs".
 * Spring Data JPA + Hibernate create/maintain this table automatically (ddl-auto: update).
 *
 * Lombok generates the getters/setters/constructors at compile time so we don't write boilerplate.
 */
@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // auto-increment primary key
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)              // two users cannot share an email
    private String email;

    /**
     * The id of this user IN KEYCLOAK (a UUID). Passwords are no longer stored here — Keycloak
     * owns them. We keep this so we can update/delete the matching Keycloak user from our CRUD.
     */
    @Column(unique = true)
    private String keycloakId;

    @Enumerated(EnumType.STRING)                          // store "PARTICIPANT" not 0/1/2
    @Column(nullable = false)
    private Role role;
}
