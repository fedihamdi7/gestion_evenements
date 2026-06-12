package tn.esprit.serviceutilisateurs.model;

/**
 * The three application roles (slide 5/8: ADMIN, ORGANISATEUR, PARTICIPANT).
 * Stored in the DB as text (see @Enumerated(EnumType.STRING) on the entity).
 */
public enum Role {
    ADMIN,
    ORGANISATEUR,
    PARTICIPANT
}
