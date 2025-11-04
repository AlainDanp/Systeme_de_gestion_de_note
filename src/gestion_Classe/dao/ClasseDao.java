package gestion_Classe.dao;

import gestion_Classe.model.Classe;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClasseDao {

    private final DataSource ds;

    public ClasseDao(DataSource ds) {
        this.ds = ds;
    }

    public Classe save(Classe classe){
        String sql = "INSERT INTO Classe (niveau, nombre_eleves) VALUES (?, ?) RETURNING id_classe";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classe.getNiveau());
            ps.setInt(2, classe.getNombreEleves() != null ? classe.getNombreEleves() : 0);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                classe.setIdClasse(rs.getInt("id_classe"));
            }

            return findById(classe.getIdClasse()).orElse(classe);

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la création de la classe", ex);
        }
    }

    public void update(Classe classe) {
        String sql = "UPDATE Classe SET niveau = ?, nombre_eleves = ? WHERE id_classe = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classe.getNiveau());
            ps.setInt(2, classe.getNombreEleves() != null ? classe.getNombreEleves() : 0);
            ps.setInt(3, classe.getIdClasse());
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de la classe", ex);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Classe WHERE id_classe = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la suppression de la classe", ex);
        }
    }

    public Optional<Classe> findById(int id) {
        String sql = "SELECT id_classe, niveau, nombre_eleves FROM Classe WHERE id_classe = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findById classe", ex);
        }
    }

    public List<Classe> findAll() {
        String sql = "SELECT id_classe, niveau, nombre_eleves FROM Classe ORDER BY niveau";

        List<Classe> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findAll classes", ex);
        }
    }

    public int compterEtudiants(int classeId) {
        String sql = "SELECT COUNT(*) FROM etudiant WHERE classe_id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur comptage étudiants", ex);
        }
    }

    public void updateNombreEleves(int classeId) {
        int count = compterEtudiants(classeId);
        String sql = "UPDATE Classe SET nombre_eleves = ? WHERE id_classe = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setInt(2, classeId);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur mise à jour nombre élèves", ex);
        }
    }

    public boolean existe(int id) {
        String sql = "SELECT COUNT(*) FROM Classe WHERE id_classe = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur vérification classe", ex);
        }
    }

    public boolean niveauExiste(String niveau) {
        String sql = "SELECT COUNT(*) FROM Classe WHERE niveau = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, niveau);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur vérification niveau", ex);
        }
    }

    private Classe mapRow(ResultSet rs) throws SQLException {
        Classe c = new Classe();
        c.setIdClasse(rs.getInt("id_classe"));
        c.setNiveau(rs.getString("niveau"));
        c.setNombreEleves(rs.getInt("nombre_eleves"));
        return c;
    }
}
