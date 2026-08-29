package gestion_Etudiant.service;

import gestion_Etudiant.model.Etudiant;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface EtudiantService {
    Etudiant creerEtudiant(Etudiant etudiant, String password);
    void modifierEtudiant(Etudiant etudiant);
    void supprimerEtudiant(Connection c,Integer id);
    Optional<Etudiant> getEtudiant(Integer id);
    List<Etudiant> listerTousLesEtudiants();
    List<Etudiant> listerEtudiantsParClasse(Integer classeId);
    void toggleActif(Integer id);
    String resetPassword(Integer id);
}
