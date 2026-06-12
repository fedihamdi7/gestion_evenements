package tn.esprit.serviceutilisateurs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.serviceutilisateurs.model.Utilisateur;

/**
 * Spring Data JPA repository. Just by extending JpaRepository we get
 * findAll / findById / save / deleteById ... for free (no SQL to write).
 * The two method names below are parsed by Spring into queries automatically.
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);
}
