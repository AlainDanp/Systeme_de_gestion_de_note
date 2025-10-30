package dao;

import model.Bulletin;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BulletinDao {
    private final DataSource ds;

    public BulletinDao(DataSource ds) {
        this.ds = ds;
    }

    public Bulletin save(Bulletin b) {
        String sql = "INSERT INTO bulletin (id_etudiant, periode, moyenne, moyenne_de_la_classe) " +
                "VALUES (?, ?, ?, ?) RETURNING id, created_at";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getEtudiantId());
            ps.setString(2, b.getPeriode());
            if (b.getMoyenne() != null) ps.setDouble(3, b.getMoyenne()); else ps.setNull(3, Types.NUMERIC);
            if (b.getMoyennDelaClasse() != null) ps.setDouble(4, b.getMoyennDelaClasse()); else ps.setNull(4, Types.NUMERIC);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                b.setId(rs.getInt("id"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) b.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
            }
            return b;
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de l'insertion du bulletin", ex);
        }
    }
    public void update(Bulletin b) {
        final String sql = "UPDATE bulletin SET moyenne = ?, moyenne_de_la_classe = ? WHERE id_etudiant = ? AND periode = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (b.getMoyenne() != null) ps.setDouble(1, b.getMoyenne()); else ps.setNull(1, Types.NUMERIC);
            if (b.getMoyennDelaClasse() != null) ps.setDouble(2, b.getMoyennDelaClasse()); else ps.setNull(2, Types.NUMERIC);
            ps.setInt(3, b.getEtudiantId());
            ps.setString(4, b.getPeriode());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur update bulletin", ex);
        }
    }

    public Optional<Bulletin> findById(int id) {
        String sql = "SELECT id, id_etudiant, periode, moyenne, moyenne_de_la_classe, created_at FROM bulletin WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            return Optional.empty();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findById bulletin", ex);
        }
    }

    public void delete(int id) {
        final String sql = "DELETE FROM bulletin WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur delete bulletin", ex);
        }
    }

    public Optional<Bulletin> findByEtudiantAndPeriode(int etudiantId, String periode) {
        String sql = "SELECT id, id_etudiant, periode, moyenne, moyenne_de_la_classe, created_at " +
                "FROM bulletin WHERE id_etudiant = ? AND periode = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setString(2, periode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            return Optional.empty();
        } catch (SQLException ex) {
            System.err.println("Erreur SQL: " + ex.getMessage());
            System.err.println("SQL State: " + ex.getSQLState());
            System.err.println("Error Code: " + ex.getErrorCode());
            ex.printStackTrace();
            throw new RuntimeException("Erreur findByEtudiantAndPeriode", ex);
        }
    }

    public List<Bulletin> findByEtudiant(int etudiantId) {
        final String sql = "SELECT id, id_etudiant, periode, moyenne, moyenne_de_la_classe, created_at " +
                "FROM bulletin WHERE id_etudiant = ? ORDER BY periode DESC";
        List<Bulletin> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByEtudiant", ex);
        }
    }

    private Bulletin mapRow(ResultSet rs) throws SQLException {
        Bulletin b = new Bulletin();
        b.setId(rs.getInt("id"));
        b.setEtudiantId(rs.getInt("id_etudiant"));
        b.setPeriode(rs.getString("periode"));
        Double m = rs.getDouble("moyenne");
        if (!rs.wasNull()) b.setMoyenne(m);
        Double mc = rs.getDouble("moyenne_de_la_classe");
        if (!rs.wasNull()) b.setMoyennDelaClasse(mc);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        return b;

    }
}

