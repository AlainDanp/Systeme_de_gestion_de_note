package gestion_Etudiant.service;

import gestion_Etudiant.model.Etudiant;

import java.util.List;
import java.util.Optional;

public interface EtudiantService {
    Etudiant creerEtudiant(Etudiant etudiant, String password);
    void modifierEtudiant(Etudiant etudiant);
    void supprimerEtudiant(Integer id);
    Optional<Etudiant> getEtudiant(Integer id);
    List<Etudiant> listerTousLesEtudiants();
    List<Etudiant> listerEtudiantsParClasse(Integer classeId);
    void toggleActif(Integer id);
    String resetPassword(Integer id);
}
