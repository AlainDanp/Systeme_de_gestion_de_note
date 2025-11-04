package gestion_Enseignant.service;

import MVC.PasswordUtil;
import gestion_Enseignant.dao.EnseignantDao;
import gestion_Enseignant.model.Enseignant;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EnseignantServiceImpl  implements EnseignantService{
    private final DataSource ds;
    private final EnseignantDao enseignantDao;

    public EnseignantServiceImpl(DataSource ds, EnseignantDao enseignantDao){
        this.ds = ds;
        this.enseignantDao = enseignantDao;
    }

    @Override
    public Enseignant creerEnseignant(Enseignant enseignant, String password){
        validerEnseignant(enseignant);
        if(password == null || password.length() < 6){
            throw  new IllegalArgumentException("Le mot de passe doit contenir 6 carractères");
        }

        if(enseignantDao.loginExists(enseignant.getLogin())){
            throw new IllegalArgumentException("Ce login existe déja");
        }
        if (emailExists(enseignant.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        enseignant.setPasswordHash(PasswordUtil.hashPassword(password));
        enseignant.setActif(true);

        return enseignantDao.save(enseignant);
    }

    @Override
    public void modifierEnseignant(Enseignant enseignant){
            if(enseignant.getIdEnseignant() == null){
                throw new IllegalArgumentException("L'ID de l'enseignat est requis");
            }
        validerEnseignant(enseignant);

        Optional<Enseignant> existing = enseignantDao.findById(enseignant.getIdEnseignant());
           if (existing.isEmpty()) {
            throw new IllegalArgumentException("Enseignant introuvable");
        }
            if (!existing.get().getLogin().equals(enseignant.getLogin())) {
                if (enseignantDao.loginExists(enseignant.getLogin())) {
                    throw new IllegalArgumentException("Ce login existe déjà");
                }
            }

            enseignantDao.update(enseignant);
    }

    @Override
    public void supprimerEnseignant(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID est requis");
        }
        Optional<Enseignant> enseignant = enseignantDao.findById(id);
        if (enseignant.isPresent() && enseignant.get().getMatiereNom() != null) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer : cet enseignant est assigné à la matière '" +
                            enseignant.get().getMatiereNom() + "'"
            );
        }

        enseignantDao.delete(id);
    }

    @Override
    public Optional<Enseignant> getEnseignant(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID est requis");
        }
        return enseignantDao.findById(id);
    }

    @Override
    public List<Enseignant> listerTousLesEnseignants() {
        return enseignantDao.findAll();
    }

    @Override
    public void toggleActif(Integer id) {
        Optional<Enseignant> opt = enseignantDao.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Enseignant introuvable");
        }

        Enseignant e = opt.get();
        e.setActif(!e.isActif());
        enseignantDao.update(e);
    }

    @Override
    public String resetPassword(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID est requis");
        }

        String newPassword = PasswordUtil.generateRandomPassword(8);
        String hash = PasswordUtil.hashPassword(newPassword);

        enseignantDao.updatePassword(id, hash);

        return newPassword;
    }

    private void validerEnseignant(Enseignant enseignant) {
        if (enseignant.getNom() == null || enseignant.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom est requis");
        }
        if (enseignant.getPrenom() == null || enseignant.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le prénom est requis");
        }
        if (enseignant.getEmail() == null || enseignant.getEmail().isBlank()) {
            throw new IllegalArgumentException("L'email est requis");
        }
        if (!enseignant.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Format d'email invalide");
        }
        if (enseignant.getLogin() == null || enseignant.getLogin().isBlank()) {
            throw new IllegalArgumentException("Le login est requis");
        }
        if (enseignant.getLogin().length() < 3) {
            throw new IllegalArgumentException("Le login doit contenir au moins 3 caractères");
        }
    }

    private boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Enseignant WHERE email = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur vérification email", ex);
        }
        return false;
    }


}
