package Admin.dao;

import Admin.model.Admin;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminDao {
    private final DataSource ds;

    public AdminDao(DataSource ds) {
        this.ds = ds;
    }
    public Admin save(Admin admin) {
        String sql = "INSERT INTO Admin (nom, prenom, email, login, password_hash, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_admin";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, admin.getNom());
            ps.setString(2, admin.getPrenom());
            ps.setString(3, admin.getEmail());
            ps.setString(4, admin.getLogin());
            ps.setString(5, admin.getPasswordHash());
            ps.setBoolean(6, admin.isActif());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                admin.setIdAdmin(rs.getInt("id_admin"));
            }

            return findById(admin.getIdAdmin()).orElse(admin);

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la création de l'admin", ex);
        }
    }


    public void update(Admin admin) {
        String sql = "UPDATE Admin SET nom = ?, prenom = ?, email = ?, login = ?, actif = ? " +
                "WHERE id_admin = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, admin.getNom());
            ps.setString(2, admin.getPrenom());
            ps.setString(3, admin.getEmail());
            ps.setString(4, admin.getLogin());
            ps.setBoolean(5, admin.isActif());
            ps.setInt(6, admin.getIdAdmin());
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'admin", ex);
        }
    }

    public void updatePassword(int idAdmin, String newPasswordHash) {
        String sql = "UPDATE Admin SET password_hash = ? WHERE id_admin = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, idAdmin);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors du changement de mot de passe", ex);
        }
    }

    public void updateDerniereConnexion(int idAdmin) {
        String sql = "UPDATE Admin SET derniere_connexion = CURRENT_TIMESTAMP WHERE id_admin = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idAdmin);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de la connexion", ex);
        }
    }

    public Optional<Admin> findById(int id) {
        String sql = "SELECT id_admin, nom, prenom, email, login, password_hash, " +
                "actif, derniere_connexion, created_at FROM Admin WHERE id_admin = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findById admin", ex);
        }
    }

    public Optional<Admin> findByLogin(String login) {
        String sql = "SELECT id_admin, nom, prenom, email, login, password_hash, " +
                "actif, derniere_connexion, created_at FROM Admin WHERE login = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByLogin admin", ex);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Admin WHERE id_admin = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la suppression de l'admin", ex);
        }
    }

    public List<Admin> findAll() {
        String sql = "SELECT id_admin, nom, prenom, email, login, password_hash, " +
                "actif, derniere_connexion, created_at FROM Admin ORDER BY nom, prenom";

        List<Admin> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findAll admins", ex);
        }
    }

    public boolean loginExists(String login) {
        String sql = "SELECT COUNT(*) FROM Admin WHERE login = ?";

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

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setIdAdmin(rs.getInt("id_admin"));
        a.setNom(rs.getString("nom"));
        a.setPrenom(rs.getString("prenom"));
        a.setEmail(rs.getString("email"));
        a.setLogin(rs.getString("login"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setActif(rs.getBoolean("actif"));

        Timestamp derniere = rs.getTimestamp("derniere_connexion");
        if (derniere != null) {
            a.setDerniereConnexion(derniere.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            a.setCreatedAt(created.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        }

        return a;
    }



}
