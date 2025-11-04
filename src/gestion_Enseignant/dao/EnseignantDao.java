package gestion_Enseignant.dao;

import gestion_Enseignant.model.Enseignant;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnseignantDao {
    private final DataSource ds;

    public EnseignantDao(DataSource ds){
        this.ds = ds;
    }

    public Enseignant save(Enseignant enseignant) {
        String sql = "INSERT INTO Enseignant (nom, prenom, email, login, password_hash, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_enseignant";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, enseignant.getNom());
            ps.setString(2, enseignant.getPrenom());
            ps.setString(3, enseignant.getEmail());
            ps.setString(4, enseignant.getLogin());
            ps.setString(5, enseignant.getPasswordHash());
            ps.setBoolean(6, enseignant.isActif());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                enseignant.setIdEnseignant(rs.getInt("id_enseignant"));
            }

            return findById(enseignant.getIdEnseignant()).orElse(enseignant);

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la création de l'enseignant", ex);
        }
    }

    public void update(Enseignant enseignant) {
        String sql = "UPDATE Enseignant SET nom = ?, prenom = ?, email = ?, login = ?, actif = ? " +
                "WHERE id_enseignant = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, enseignant.getNom());
            ps.setString(2, enseignant.getPrenom());
            ps.setString(3, enseignant.getEmail());
            ps.setString(4, enseignant.getLogin());
            ps.setBoolean(5, enseignant.isActif());
            ps.setInt(6, enseignant.getIdEnseignant());
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'enseignant", ex);
        }
    }

    public void updatePassword(int idEnseignant, String newPasswordHash) {
        String sql = "UPDATE Enseignant SET password_hash = ? WHERE id_enseignant = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, idEnseignant);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors du changement de mot de passe", ex);
        }
    }

    public void updateDerniereConnexion(int idEnseignant) {
        String sql = "UPDATE Enseignant SET derniere_connexion = CURRENT_TIMESTAMP WHERE id_enseignant = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEnseignant);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de la connexion", ex);
        }
    }

    public Optional<Enseignant> findById(int id) {
        String sql =
                "SELECT e.id_enseignant, e.nom, e.prenom, e.email, e.login, e.password_hash, " +
                        "       e.actif, e.derniere_connexion, m.nom as matiere_nom " +
                        "FROM Enseignant e " +
                        "LEFT JOIN Matiere m ON e.id_enseignant = m.id_enseignant " +
                        "WHERE e.id_enseignant = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findById enseignant", ex);
        }
    }

    public Optional<Enseignant> findByLogin(String login) {
        String sql =
                "SELECT e.id_enseignant, e.nom, e.prenom, e.email, e.login, e.password_hash, " +
                        "       e.actif, e.derniere_connexion, m.nom as matiere_nom " +
                        "FROM Enseignant e " +
                        "LEFT JOIN Matiere m ON e.id_enseignant = m.id_enseignant " +
                        "WHERE e.login = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByLogin enseignant", ex);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Enseignant WHERE id_enseignant = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la suppression de l'enseignant", ex);
        }
    }

    public List<Enseignant> findAll() {
        String sql =
                "SELECT e.id_enseignant, e.nom, e.prenom, e.email, e.login, e.password_hash, " +
                        "       e.actif, e.derniere_connexion, m.nom as matiere_nom " +
                        "FROM Enseignant e " +
                        "LEFT JOIN Matiere m ON e.id_enseignant = m.id_enseignant " +
                        "ORDER BY e.nom, e.prenom";

        List<Enseignant> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findAll enseignants", ex);
        }
    }

    public boolean loginExists(String login) {
        String sql = "SELECT COUNT(*) FROM Enseignant WHERE login = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la vérification du login", ex);
        }
    }

    private Enseignant mapRow(ResultSet rs) throws SQLException {
        Enseignant e = new Enseignant();
        e.setIdEnseignant(rs.getInt("id_enseignant"));
        e.setNom(rs.getString("nom"));
        e.setPrenom(rs.getString("prenom"));
        e.setEmail(rs.getString("email"));
        e.setLogin(rs.getString("login"));
        e.setPasswordHash(rs.getString("password_hash"));
        e.setActif(rs.getBoolean("actif"));

        Timestamp ts = rs.getTimestamp("derniere_connexion");
        if (ts != null) {
            e.setDerniereConnexion(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        }

        try {
            String matiere = rs.getString("matiere_nom");
            if (matiere != null) e.setMatiereNom(matiere);
        } catch (SQLException ex) {
            // Colonne non disponible
        }

        return e;
    }


}
