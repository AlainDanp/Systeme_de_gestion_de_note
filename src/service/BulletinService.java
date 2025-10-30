package service;

import model.Bulletin;

import java.util.List;
import java.util.Optional;

public interface BulletinService {

    Bulletin genererEtEnregistrerBulletin(Integer etudiantId, String periode);
    Optional<Bulletin> getBulletin(Integer id);
    Optional<Bulletin> getParEtudiantEtPeriode(Integer etudiantId, String periode);
    List<Bulletin> listerParEtudiant(Integer etudiantId);
    void delete(Integer id);
    void update(Bulletin bulletin);
    List<Bulletin> findByEtudiant(Integer etudiantId);
    Bulletin creeBulletin(Bulletin bulletin);
}
