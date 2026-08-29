package gestion_Matiere.dao;


import gestion_Matiere.model.Matiere;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatiereDao {
    private final DataSource ds;

    public MatiereDao(DataSource ds) {
        this.ds = ds;
    }

    /**
     * Créer une nouvelle matière
     */
    public Matiere save(Matiere matiere) {
        String sql = "INSERT INTO Matiere (nom, id_enseignant) VALUES (?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, matiere.getNom());
            ps.setInt(2, matiere.getIdEnseignant());
            ps.executeUpdate();
            return findByNom(matiere.getNom()).orElse(matiere);
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de l'insertion de la matière", ex);
        }
    }

    /**
     * Mettre à jour une matière (changer l'enseignant)
     */
    public void update(Matiere matiere) {
        String sql = "UPDATE Matiere SET id_enseignant = ? WHERE nom = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, matiere.getIdEnseignant());
            ps.setString(2, matiere.getNom());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de la matière", ex);
        }
    }

    /**
     * Supprimer une matière par son nom
     */
    public void delete(String nom) {
        String sql = "DELETE FROM Matiere WHERE nom = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la suppression de la matière", ex);
        }
    }

    /**
     * Trouver une matière par son nom
     */
    public Optional<Matiere> findByNom(String nom) {
        String sql =
                "SELECT m.nom, m.id_enseignant, " +
                        "       e.nom as enseignant_nom, e.prenom as enseignant_prenom, e.email as enseignant_email " +
                        "FROM Matiere m " +
                        "JOIN Enseignant e ON m.id_enseignant = e.id_enseignant " +
                        "WHERE m.nom = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByNom matière", ex);
        }
    }

    /**
     * Trouver toutes les matières d'un enseignant
     */
    public List<Matiere> findByEnseignant(int idEnseignant) {
        String sql =
                "SELECT m.nom, m.id_enseignant, " +
                        "       e.nom as enseignant_nom, e.prenom as enseignant_prenom, e.email as enseignant_email " +
                        "FROM Matiere m " +
                        "JOIN Enseignant e ON m.id_enseignant = e.id_enseignant " +
                        "WHERE m.id_enseignant = ? " +
                        "ORDER BY m.nom";

        List<Matiere> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEnseignant);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByEnseignant", ex);
        }
        return list;
    }

    /**
     * Lister toutes les matières
     */
    public List<Matiere> findAll() {
        String sql =
                "SELECT m.nom, m.id_enseignant, " +
                        "       e.nom as enseignant_nom, e.prenom as enseignant_prenom, e.email as enseignant_email " +
                        "FROM Matiere m " +
                        "JOIN Enseignant e ON m.id_enseignant = e.id_enseignant " +
                        "ORDER BY m.nom";

        List<Matiere> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findAll matières", ex);
        }
        return list;
    }

    /**
     * Compter le nombre de notes dans une matière
     */
    public int compterNotes(String nomMatiere) {
        String sql = "SELECT COUNT(*) FROM note WHERE nom_matiere = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nomMatiere);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors du comptage des notes", ex);
        }
    }

    /**
     * Vérifier si une matière existe
     */
    public boolean existe(String nom) {
        String sql = "SELECT COUNT(*) FROM Matiere WHERE nom = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la vérification de la matière", ex);
        }
    }

    private Matiere mapRow(ResultSet rs) throws SQLException {
        Matiere m = new Matiere();
        m.setNom(rs.getString("nom"));
        m.setIdEnseignant(rs.getInt("id_enseignant"));

        // Informations enrichies de l'enseignant
        try {
            String nom = rs.getString("enseignant_nom");
            if (nom != null) m.setEnseignantNom(nom);

            String prenom = rs.getString("enseignant_prenom");
            if (prenom != null) m.setEnseignantPrenom(prenom);

            String email = rs.getString("enseignant_email");
            if (email != null) m.setEnseignantEmail(email);
        } catch (SQLException e) {
            // Colonnes non disponibles
        }

        return m;
    }
}
