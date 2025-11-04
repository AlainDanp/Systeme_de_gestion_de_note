package gestion_Classe.service;

import gestion_Classe.model.Classe;

import java.util.List;
import java.util.Optional;

public interface ClasseService {
    Classe creerClasse(Classe classe);
    void modifierClasse(Classe classe);
    void supprimerClasse(Integer id);
    Optional<Classe> getClasse(Integer id);
    List<Classe> listerToutesLesClasses();
    int compterEtudiants(Integer classeId);
    void rafraichirNombreEleves(Integer classeId);
    boolean classeExiste(Integer id);
}
