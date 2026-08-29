package gestion_Etudiant.service;

import Event.EtudiantCreatedEvent;
import Event.EventDispatcher;
import MVC.PasswordUtil;
import MVC.SecurityContext;
import gestion_Etudiant.dao.EtudiantDao;
import gestion_Etudiant.model.Etudiant;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EtudiantServiceImpl implements EtudiantService {
    private final DataSource ds;
    private final EtudiantDao etudiantDao;
    private final EventDispatcher eventDispatcher;
    private Integer currentUserId;
    private String currentUserName;
    private final SecurityContext securityContext;

    public EtudiantServiceImpl(DataSource ds, EtudiantDao etudiantDao, SecurityContext securityContext){
        this.ds = ds;
        this.etudiantDao = etudiantDao;
        this.securityContext = securityContext;
        this.eventDispatcher = EventDispatcher.getInstance();
    }

    public void setCurrentUser(Integer userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
    }

    @Override
    public Etudiant creerEtudiant(Etudiant etudiant, String password) {
        validerEtudiant(etudiant);

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
        }

        if (etudiantDao.loginExists(etudiant.getLogin())) {
            throw new IllegalArgumentException("Ce login existe déjà");
        }

        etudiant.setPasswordHash(PasswordUtil.hashPassword(password));
        etudiant.setActif(true);

        Etudiant created = etudiantDao.save(etudiant);

        //  Déclencher l'événement ETUDIANT_CREATED
        if (currentUserId != null && currentUserName != null) {
            EtudiantCreatedEvent event = new EtudiantCreatedEvent(
                    currentUserId,
                    currentUserName,
                    created
            );
            eventDispatcher.dispatch(event);
        }

        return created;
    }

    @Override
    public void modifierEtudiant(Etudiant etudiant){
        if (etudiant.getIdEtudiant() == null) {
            throw new IllegalArgumentException("L'ID de l'étudiant est requis");
        }
        validerEtudiant(etudiant);

        Optional<Etudiant> existing = etudiantDao.findById(etudiant.getIdEtudiant());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Étudiant introuvable");
        }

        if (!existing.get().getLogin().equals(etudiant.getLogin())) {
            if (etudiantDao.loginExists(etudiant.getLogin())) {
                throw new IllegalArgumentException("Ce login existe déjà");
            }
        }

        if (etudiant.getClasseId() != null) {
            if (!classeExists(etudiant.getClasseId())) {
                throw new IllegalArgumentException("Classe introuvable");
            }
        }
        etudiantDao.update(etudiant);
    }

    @Override
    public void supprimerEtudiant(Connection c,Integer id){
        if(id == null){
            throw new IllegalArgumentException("L'ID est requis");
        }

        if (hasNotes(id)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer : cet étudiant a des notes enregistrées"
            );
        }

        if (hasBulletins(id)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer : cet étudiant a des bulletins enregistrés"
            );
        }
        etudiantDao.delete(c,id);
    }

    @Override
    public Optional<Etudiant> getEtudiant(Integer id){
        if(id == null){
            throw new IllegalArgumentException("L'ID est requis");
        }
        return etudiantDao.findById(id);
    }

    @Override
    public List<Etudiant> listerTousLesEtudiants() {
        return etudiantDao.findAll();
    }

    @Override
    public List<Etudiant> listerEtudiantsParClasse(Integer classeId) {
        if (classeId == null) {
            throw new IllegalArgumentException(" L'ID de la classe est requis");
        }
        return etudiantDao.findByClasse(classeId);
    }

    @Override
    public void toggleActif(Integer id) {
        Optional<Etudiant> opt = etudiantDao.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Étudiant introuvable");
        }

        Etudiant e = opt.get();
        e.setActif(!e.isActif());
        etudiantDao.update(e);
    }

    @Override
    public String resetPassword(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID est requis");
        }
        String newPassword = PasswordUtil.generateRandomPassword(8);
        String hash = PasswordUtil.hashPassword(newPassword);

        etudiantDao.updatePassword(id, hash);

        return newPassword;
    }

    private void validerEtudiant(Etudiant etudiant) {
        if (etudiant.getNom() == null || etudiant.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom est requis");
        }
        if (etudiant.getPrenom() == null || etudiant.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le prénom est requis");
        }
        if (etudiant.getLogin() == null || etudiant.getLogin().isBlank()) {
            throw new IllegalArgumentException("Le login est requis");
        }
        if (etudiant.getLogin().length() < 3) {
            throw new IllegalArgumentException("Le login doit contenir au moins 3 caractères");
        }
        if (etudiant.getEmail() != null && !etudiant.getEmail().isBlank()) {
            if (!etudiant.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("Format d'email invalide");
            }
        }
    }

    private boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM etudiant WHERE email = ?";
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

    private boolean classeExists(Integer classeId) {
        String sql = "SELECT COUNT(*) FROM Classe WHERE id_classe = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur vérification classe", ex);
        }
        return false;
    }

    private boolean hasNotes(Integer etudiantId) {
        String sql = "SELECT COUNT(*) FROM note WHERE id_etudiant = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur vérification notes", ex);
        }
        return false;
    }

    private boolean hasBulletins(Integer etudiantId) {
        String sql = "SELECT COUNT(*) FROM bulletin WHERE id_etudiant = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur vérification bulletins", ex);
        }
        return false;
    }


}
