package tn.esprit.serviceavis.repository;

import tn.esprit.serviceavis.model.Avis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {

    List<Avis> findByEvenementId(Long evenementId);

    List<Avis> findByUtilisateurId(Long utilisateurId);
}
