package gestion_Matiere.service;

import Event.EventDispatcher;
import Event.MatiereEnseignantModifieEvent;
import MVC.SecurityContext;
import gestion_Matiere.dao.MatiereDao;
import gestion_Matiere.model.Matiere;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatiereServiceImpl implements MatiereService {

    private final DataSource ds;
    private final MatiereDao matiereDao;
    private final SecurityContext securityContext;
    private final EventDispatcher eventDispatcher;
    private Integer currentUserId;
    private String currentUserName;

    public MatiereServiceImpl(DataSource ds, MatiereDao matiereDao, SecurityContext securityContext) {
        this.ds = ds;
        this.matiereDao = matiereDao;
        this.securityContext = securityContext;
        this.eventDispatcher = EventDispatcher.getInstance();
    }

    public void setCurrentUser(Integer userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
    }

    @Override
    public Matiere creerMatiere(Matiere matiere) {
        // Validation
        validerMatiere(matiere);

        // Vérifier que la matière n'existe pas déjà
        if (matiereDao.existe(matiere.getNom())) {
            throw new IllegalArgumentException("Une matière avec le nom '" + matiere.getNom() + "' existe déjà");
        }

        // Vérifier que l'enseignant existe
        if (!enseignantExiste(matiere.getIdEnseignant())) {
            throw new IllegalArgumentException("L'enseignant avec l'ID " + matiere.getIdEnseignant() + " n'existe pas");
        }

        // Vérifier que l'enseignant n'est pas déjà assigné à une matière
        if (enseignantDejaAssigne(matiere.getIdEnseignant())) {
            throw new IllegalArgumentException("Cet enseignant est déjà assigné à une autre matière");
        }

        return matiereDao.save(matiere);
    }

    @Override
    public void modifierMatiere(Matiere matiere) {
        validerMatiere(matiere);

        // Vérifier que la matière existe
        if (!matiereDao.existe(matiere.getNom())) {
            throw new IllegalArgumentException("La matière '" + matiere.getNom() + "' n'existe pas");
        }

        // Vérifier que l'enseignant existe
        if (!enseignantExiste(matiere.getIdEnseignant())) {
            throw new IllegalArgumentException("L'enseignant avec l'ID " + matiere.getIdEnseignant() + " n'existe pas");
        }

        // Vérifier que le nouvel enseignant n'est pas déjà assigné
        Optional<Matiere> ancienneMatiere = matiereDao.findByNom(matiere.getNom());
        Integer ancienEnseignantId = ancienneMatiere.map(Matiere::getIdEnseignant).orElse(null);
        if (ancienneMatiere.isPresent() &&
                !ancienneMatiere.get().getIdEnseignant().equals(matiere.getIdEnseignant()) &&
                enseignantDejaAssigne(matiere.getIdEnseignant())) {
            throw new IllegalArgumentException("Le nouvel enseignant est déjà assigné à une autre matière");
        }

        matiereDao.update(matiere);

        boolean enseignantChange = ancienEnseignantId != null && !ancienEnseignantId.equals(matiere.getIdEnseignant());
        if (enseignantChange && currentUserId != null && currentUserName != null) {
            eventDispatcher.dispatch(new MatiereEnseignantModifieEvent(
                    currentUserId, currentUserName, matiere.getNom(), ancienEnseignantId, matiere.getIdEnseignant()));
        }
    }

    @Override
    public void supprimerMatiere(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de la matière est requis");
        }

        // Vérifier s'il y a des notes associées
        int nbNotes = matiereDao.compterNotes(nom);
        if (nbNotes > 0) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer cette matière : " + nbNotes + " note(s) y sont associées. " +
                            "Supprimez d'abord les notes."
            );
        }

        matiereDao.delete(nom);
    }

    @Override
    public Optional<Matiere> getMatiere(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de la matière est requis");
        }
        return matiereDao.findByNom(nom);
    }

    @Override
    public List<Matiere> listerToutesLesMatieres() {
        return matiereDao.findAll();
    }

    @Override
    public List<Matiere> listerMatieresParEnseignant(Integer idEnseignant) {
        if (idEnseignant == null) {
            throw new IllegalArgumentException("L'ID de l'enseignant est requis");
        }
        return matiereDao.findByEnseignant(idEnseignant);
    }

    @Override
    public int compterNotesMatiere(String nomMatiere) {
        if (nomMatiere == null || nomMatiere.isBlank()) {
            throw new IllegalArgumentException("Le nom de la matière est requis");
        }
        return matiereDao.compterNotes(nomMatiere);
    }

    @Override
    public boolean matiereExiste(String nom) {
        if (nom == null || nom.isBlank()) {
            return false;
        }
        return matiereDao.existe(nom);
    }

    @Override
    public List<EnseignantInfo> listerEnseignantsDisponibles() {
        String sql =
                "SELECT e.id_enseignant, e.nom, e.prenom, e.email, " +
                        "       CASE WHEN m.nom IS NOT NULL THEN true ELSE false END as assigne " +
                        "FROM Enseignant e " +
                        "LEFT JOIN Matiere m ON e.id_enseignant = m.id_enseignant " +
                        "ORDER BY e.nom, e.prenom";

        List<EnseignantInfo> liste = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                EnseignantInfo info = new EnseignantInfo();
                info.setId(rs.getInt("id_enseignant"));
                info.setNom(rs.getString("nom"));
                info.setPrenom(rs.getString("prenom"));
                info.setEmail(rs.getString("email"));
                info.setAssigneAMatiere(rs.getBoolean("assigne"));
                liste.add(info);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la récupération des enseignants", ex);
        }
        return liste;
    }


    @Override
    public List<String> listerClassesConcernees(String nomMatiere) {
        if (nomMatiere == null || nomMatiere.isBlank()) {
            return new ArrayList<>();
        }
        String sql =
                "SELECT cl.niveau " +
                        "FROM classe cl " +
                        "JOIN enseignant_classe ec ON ec.id_classe = cl.id_classe " +
                        "JOIN matiere m ON m.id_enseignant = ec.id_enseignant " +
                        "WHERE m.nom = ? " +
                        "ORDER BY cl.niveau";

        List<String> classes = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nomMatiere);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    classes.add(rs.getString("niveau"));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la récupération des classes concernées", ex);
        }
        return classes;
    }

    private void validerMatiere(Matiere matiere) {
        if (matiere.getNom() == null || matiere.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de la matière est requis");
        }
        if (matiere.getIdEnseignant() == null) {
            throw new IllegalArgumentException("L'ID de l'enseignant est requis");
        }
        if (matiere.getNom().length() > 100) {
            throw new IllegalArgumentException("Le nom de la matière ne peut pas dépasser 100 caractères");
        }
    }

    private boolean enseignantExiste(Integer idEnseignant) {
        String sql = "SELECT COUNT(*) FROM Enseignant WHERE id_enseignant = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEnseignant);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la vérification de l'enseignant", ex);
        }
        return false;
    }

    private boolean enseignantDejaAssigne(Integer idEnseignant) {
        String sql = "SELECT COUNT(*) FROM Matiere WHERE id_enseignant = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEnseignant);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la vérification de l'assignation", ex);
        }
        return false;
    }
}
