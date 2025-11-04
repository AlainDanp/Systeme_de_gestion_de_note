package gestion_Etudiant.dao;

import gestion_Etudiant.model.Etudiant;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EtudiantDao {
    private final DataSource ds;

    public EtudiantDao(DataSource ds) {
        this.ds = ds;
    }

    public Etudiant save(Etudiant etudiant) {
        String sql = "INSERT INTO etudiant (nom, prenom, email, login, password_hash, classe_id, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, etudiant.getNom());
            ps.setString(2, etudiant.getPrenom());
            ps.setString(3, etudiant.getEmail());
            ps.setString(4, etudiant.getLogin());
            ps.setString(5, etudiant.getPasswordHash());

            if (etudiant.getClasseId() != null) {
                ps.setInt(6, etudiant.getClasseId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setBoolean(7, etudiant.isActif());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                etudiant.setIdEtudiant(rs.getInt("id"));
            }

            return findById(etudiant.getIdEtudiant()).orElse(etudiant);

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la création de l'étudiant", ex);
        }
    }

    public void update(Etudiant etudiant) {
        String sql = "UPDATE etudiant SET nom = ?, prenom = ?, email = ?, login = ?, " +
                "classe_id = ?, actif = ? WHERE id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, etudiant.getNom());
            ps.setString(2, etudiant.getPrenom());
            ps.setString(3, etudiant.getEmail());
            ps.setString(4, etudiant.getLogin());

            if (etudiant.getClasseId() != null) {
                ps.setInt(5, etudiant.getClasseId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setBoolean(6, etudiant.isActif());
            ps.setInt(7, etudiant.getIdEtudiant());
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'étudiant", ex);
        }
    }

    public void updatePassword(int idEtudiant, String newPasswordHash) {
        String sql = "UPDATE etudiant SET password_hash = ? WHERE id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, idEtudiant);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors du changement de mot de passe", ex);
        }
    }

    public void updateDerniereConnexion(int idEtudiant) {
        String sql = "UPDATE etudiant SET derniere_connexion = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEtudiant);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de la connexion", ex);
        }
    }

    public Optional<Etudiant> findById(int id) {
        String sql =
                "SELECT e.id, e.nom, e.prenom, e.email, e.login, e.password_hash, e.classe_id, " +
                        "       e.actif, e.derniere_connexion, e.created_at, c.niveau as classe_niveau " +
                        "FROM etudiant e " +
                        "LEFT JOIN Classe c ON e.classe_id = c.id_classe " +
                        "WHERE e.id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findById étudiant", ex);
        }
    }

    public Optional<Etudiant> findByLogin(String login) {
        String sql =
                "SELECT e.id, e.nom, e.prenom, e.email, e.login, e.password_hash, e.classe_id, " +
                        "       e.actif, e.derniere_connexion, e.created_at, c.niveau as classe_niveau " +
                        "FROM etudiant e " +
                        "LEFT JOIN Classe c ON e.classe_id = c.id_classe " +
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
            throw new RuntimeException("Erreur findByLogin étudiant", ex);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM etudiant WHERE id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la suppression de l'étudiant", ex);
        }
    }

    public List<Etudiant> findAll() {
        String sql =
                "SELECT e.id, e.nom, e.prenom, e.email, e.login, e.password_hash, e.classe_id, " +
                        "       e.actif, e.derniere_connexion, e.created_at, c.niveau as classe_niveau " +
                        "FROM etudiant e " +
                        "LEFT JOIN Classe c ON e.classe_id = c.id_classe " +
                        "ORDER BY e.nom, e.prenom";

        List<Etudiant> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findAll étudiants", ex);
        }
    }

    public List<Etudiant> findByClasse(int classeId) {
        String sql =
                "SELECT e.id, e.nom, e.prenom, e.email, e.login, e.password_hash, e.classe_id, " +
                        "       e.actif, e.derniere_connexion, e.created_at, c.niveau as classe_niveau " +
                        "FROM etudiant e " +
                        "LEFT JOIN Classe c ON e.classe_id = c.id_classe " +
                        "WHERE e.classe_id = ? " +
                        "ORDER BY e.nom, e.prenom";

        List<Etudiant> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByClasse étudiants", ex);
        }
    }

    public boolean loginExists(String login) {
        String sql = "SELECT COUNT(*) FROM etudiant WHERE login = ?";

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

    private Etudiant mapRow(ResultSet rs) throws SQLException {
        Etudiant e = new Etudiant();
        e.setIdEtudiant(rs.getInt("id"));
        e.setNom(rs.getString("nom"));
        e.setPrenom(rs.getString("prenom"));

        String email = rs.getString("email");
        if (email != null) e.setEmail(email);

        e.setLogin(rs.getString("login"));
        e.setPasswordHash(rs.getString("password_hash"));

        int classeId = rs.getInt("classe_id");
        if (!rs.wasNull()) e.setClasseId(classeId);

        e.setActif(rs.getBoolean("actif"));

        Timestamp derniere = rs.getTimestamp("derniere_connexion");
        if (derniere != null) {
            e.setDerniereConnexion(derniere.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            e.setCreatedAt(created.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        }

        try {
            String classe = rs.getString("classe_niveau");
            if (classe != null) e.setClasseNiveau(classe);
        } catch (SQLException ex) {
            // Colonne non disponible
        }

        return e;
    }


}