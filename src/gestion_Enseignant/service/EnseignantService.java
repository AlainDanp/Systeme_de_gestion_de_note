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
    void toggleTitulaire(Integer id);
    String resetPassword(Integer id);
    /** Classes explicitement assignées à cet enseignant. */
    List<String> listerClassesConcernees(Integer idEnseignant);
    /** IDs des classes assignées, pour pré-remplir le formulaire d'édition. */
    List<Integer> listerClasseIds(Integer idEnseignant);
    /** Remplace l'ensemble des classes assignées à l'enseignant. */
    void assignerClasses(Integer idEnseignant, List<Integer> classeIds);
}