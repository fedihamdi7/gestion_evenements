package tn.esprit.serviceavis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.serviceavis.dto.AvisRequest;
import tn.esprit.serviceavis.dto.AvisResponse;
import tn.esprit.serviceavis.dto.AvisUpdateRequest;
import tn.esprit.serviceavis.model.Avis;
import tn.esprit.serviceavis.repository.AvisRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvisService {

    private final AvisRepository repository;

    public AvisResponse create(AvisRequest req) {
        Avis avis = Avis.builder()
                .utilisateurId(req.getUtilisateurId())
                .evenementId(req.getEvenementId())
                .note(req.getNote())
                .commentaire(req.getCommentaire())
                .dateCreation(LocalDateTime.now())
                .build();
        return toResponse(repository.save(avis));
    }

    public List<AvisResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public AvisResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<AvisResponse> findByEvenement(Long evenementId) {
        return repository.findByEvenementId(evenementId).stream().map(this::toResponse).toList();
    }

    public List<AvisResponse> findByUtilisateur(Long utilisateurId) {
        return repository.findByUtilisateurId(utilisateurId).stream().map(this::toResponse).toList();
    }

    public AvisResponse update(Long id, AvisUpdateRequest req) {
        Avis avis = getOrThrow(id);
        if (req.getNote() != null)        avis.setNote(req.getNote());
        if (req.getCommentaire() != null) avis.setCommentaire(req.getCommentaire());
        return toResponse(repository.save(avis));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis introuvable: " + id);
        }
        repository.deleteById(id);
    }

    private Avis getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis introuvable: " + id));
    }

    private AvisResponse toResponse(Avis a) {
        return AvisResponse.builder()
                .id(a.getId())
                .utilisateurId(a.getUtilisateurId())
                .evenementId(a.getEvenementId())
                .note(a.getNote())
                .commentaire(a.getCommentaire())
                .dateCreation(a.getDateCreation().toString())
                .build();
    }
}
