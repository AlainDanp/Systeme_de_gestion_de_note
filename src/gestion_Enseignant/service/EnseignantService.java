package gestion_Enseignant.service;

import gestion_Enseignant.model.Enseignant;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface EnseignantService {

    Enseignant creerEnseignant(Enseignant enseignant, String password);
    void modifierEnseignant(Enseignant enseignant);
    void supprimerEnseignant(Connection c,Integer id);
    Optional<Enseignant> getEnseignant(Integer id);
    List<Enseignant> listerTousLesEnseignants();
    void toggleActif(Integer id);
    String resetPassword(Integer id);
    /** Classes ayant au moins un élève noté par cet enseignant (dérivé des notes, pas de lien direct en base). */
    List<String> listerClassesConcernees(Integer idEnseignant);
}