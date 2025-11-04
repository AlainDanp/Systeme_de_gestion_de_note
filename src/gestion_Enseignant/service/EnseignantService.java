package gestion_Enseignant.service;

import gestion_Enseignant.model.Enseignant;

import java.util.List;
import java.util.Optional;

public interface EnseignantService {

    Enseignant creerEnseignant(Enseignant enseignant, String password);
    void modifierEnseignant(Enseignant enseignant);
    void supprimerEnseignant(Integer id);
    Optional<Enseignant> getEnseignant(Integer id);
    List<Enseignant> listerTousLesEnseignants();
    void toggleActif(Integer id);
    String resetPassword(Integer id);
}