package gestion_Bulletin.service;

import gestion_Bulletin.model.Bulletin;
import gestion_Bulletin.model.NoteDetail;

import java.util.List;
import java.util.Optional;

public interface BulletinService {

    Bulletin genererEtEnregistrerBulletin(Integer etudiantId, String periode);
    Optional<Bulletin> getBulletin(Integer id);
    Optional<Bulletin> getParEtudiantEtPeriode(Integer etudiantId, String periode);
    List<Bulletin> listerParEtudiant(Integer etudiantId);
    void delete(Integer id);
    void update(Bulletin bulletin);
    Bulletin creeBulletin(Bulletin bulletin);
    List<NoteDetail> getNotesAvecEnseignants(Integer etudiantId, String periode);
}
