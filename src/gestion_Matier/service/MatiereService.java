package gestion_Matier.service;

import gestion_Matier.model.Matiere;

import java.util.List;
import java.util.Optional;

public interface MatiereService {
    Matiere creerMatiere(Matiere matiere);
    void modifierMatiere(Matiere matiere);
    void supprimerMatiere(String nom);
    Optional<Matiere> getMatiere(String nom);
    List<Matiere> listerToutesLesMatieres();
    List<Matiere> listerMatieresParEnseignant(Integer idEnseignant);
    int compterNotesMatiere(String nomMatiere);
    boolean matiereExiste(String nom);
    List<EnseignantInfo> listerEnseignantsDisponibles();
}
