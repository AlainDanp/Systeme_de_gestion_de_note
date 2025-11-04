package gestion_Classe.service;

import gestion_Classe.dao.ClasseDao;
import gestion_Classe.model.Classe;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class ClasseServiceImpl implements ClasseService {

    private final DataSource ds;
    private final ClasseDao classeDao;

    public ClasseServiceImpl(DataSource ds, ClasseDao classeDao) {
        this.ds = ds;
        this.classeDao = classeDao;
    }

    @Override
    public Classe creerClasse(Classe classe) {
        validerClasse(classe);
        if (classeDao.niveauExiste(classe.getNiveau())) {
            throw new IllegalArgumentException("Une classe avec ce niveau existe déjà : " + classe.getNiveau());
        }
        if (classe.getNombreEleves() == null) {
            classe.setNombreEleves(0);
        }

        return classeDao.save(classe);
    }

    @Override
    public void modifierClasse(Classe classe) {
        if (classe.getIdClasse() == null) {
            throw new IllegalArgumentException("L'ID de la classe est requis");
        }
        if (!classeDao.existe(classe.getIdClasse())) {
            throw new IllegalArgumentException("Classe introuvable");
        }

        classeDao.update(classe);
    }

    @Override
    public void supprimerClasse(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID est requis");
        }

        int nbEtudiants = classeDao.compterEtudiants(id);
        if (nbEtudiants > 0) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer : cette classe contient " + nbEtudiants + " étudiant(s). " +
                            "Réassignez d'abord les étudiants à une autre classe."
            );
        }

        classeDao.delete(id);

    }

    @Override
    public Optional<Classe> getClasse(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID est requis");
        }
        return classeDao.findById(id);
    }

    @Override
    public List<Classe> listerToutesLesClasses() {
        return classeDao.findAll();
    }

    @Override
    public int compterEtudiants(Integer classeId) {
        if (classeId == null) {
            throw new IllegalArgumentException("L'ID de la classe est requis");
        }
        return classeDao.compterEtudiants(classeId);
    }

    @Override
    public void rafraichirNombreEleves(Integer classeId) {
        if (classeId == null) {
            throw new IllegalArgumentException("L'ID de la classe est requis");
        }

        if (!classeDao.existe(classeId)) {
            throw new IllegalArgumentException("Classe introuvable");
        }

        classeDao.updateNombreEleves(classeId);
    }

    @Override
    public boolean classeExiste(Integer id) {
        if (id == null) {
            return false;
        }
        return classeDao.existe(id);
    }

    private void validerClasse(Classe classe) {
        if (classe.getNiveau() == null || classe.getNiveau().isBlank()) {
            throw new IllegalArgumentException("Le niveau est requis");
        }

        if (classe.getNiveau().length() > 50) {
            throw new IllegalArgumentException("Le niveau ne peut pas dépasser 50 caractères");
        }

        if (classe.getNombreEleves() != null && classe.getNombreEleves() < 0) {
            throw new IllegalArgumentException("Le nombre d'élèves ne peut pas être négatif");
        }
    }
}
