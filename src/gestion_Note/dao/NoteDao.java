package gestion_Note.dao;

import gestion_Note.model.Note;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteDao {
    private final DataSource ds;
    public NoteDao(DataSource ds){
        this.ds = ds;
    }

    public Note save(Note note) {
        try (Connection c = ds.getConnection()) {
            return save(c, note);
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de l'insertion de la note", ex);
        }
    }


    public Note save(Connection c, Note note) {
        String sql = "INSERT INTO note (id_etudiant, periode, nom_matiere, valeur) " +
                "VALUES (?, ?, ?, ?) RETURNING id, created_at";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, note.getEtudiantId());
            ps.setString(2, note.getPeriode());
            ps.setString(3, note.getMatiere());
            ps.setDouble(4, note.getValeur());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    note.setId(rs.getInt("id"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) note.setCreatedAt(ts.toLocalDateTime().atOffset(ZoneOffset.UTC));
                }
            }
            return note;
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de l'insertion de la note", ex);
        }
    }

    public void update(Note note) {
        String sql = "UPDATE note SET id_etudiant = ?, periode = ?, nom_matiere = ?, valeur = ? WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, note.getEtudiantId());
            ps.setString(2, note.getPeriode());
            ps.setString(3, note.getMatiere());
            ps.setDouble(4, note.getValeur());
            ps.setInt(5, note.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour de la note", ex);
        }
    }

    public Optional<Note> findById(int id) {
        String sql =
                "SELECT n.id, n.id_etudiant, n.periode, n.nom_matiere, n.valeur, n.created_at, " +
                        "       e.nom as etudiant_nom, e.prenom as etudiant_prenom, e.classe_id, " +
                        "       ens.nom as enseignant_nom, ens.prenom as enseignant_prenom " +
                        "FROM note n " +
                        "JOIN etudiant e ON n.id_etudiant = e.id " +
                        "LEFT JOIN Matiere m ON n.nom_matiere = m.nom " +
                        "LEFT JOIN Enseignant ens ON m.id_enseignant = ens.id_enseignant " +
                        "WHERE n.id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findById note", ex);
        }
    }

    public void delete(Connection c,int id) {
        String sql = "DELETE FROM note WHERE id = ?";
        try (
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la suppression de la note", ex);
        }
    }

    public List<Note> findByEtudiant(int etudiantId) {
        String sql =
                "SELECT n.id, n.id_etudiant, n.periode, n.nom_matiere, n.valeur, n.created_at, " +
                        "       e.nom as etudiant_nom, e.prenom as etudiant_prenom, e.classe_id, " +
                        "       ens.nom as enseignant_nom, ens.prenom as enseignant_prenom " +
                        "FROM note n " +
                        "JOIN etudiant e ON n.id_etudiant = e.id " +
                        "LEFT JOIN Matiere m ON n.nom_matiere = m.nom " +
                        "LEFT JOIN Enseignant ens ON m.id_enseignant = ens.id_enseignant " +
                        "WHERE n.id_etudiant = ? " +
                        "ORDER BY n.periode DESC, n.nom_matiere";

        return executeQuery(sql, etudiantId);
    }

    public List<Note> findByEtudiantAndPeriode(int etudiantId, String periode) {
        String sql =
                "SELECT n.id, n.id_etudiant, n.periode, n.nom_matiere, n.valeur, n.created_at, " +
                        "       e.nom as etudiant_nom, e.prenom as etudiant_prenom, e.classe_id, " +
                        "       ens.nom as enseignant_nom, ens.prenom as enseignant_prenom " +
                        "FROM note n " +
                        "JOIN etudiant e ON n.id_etudiant = e.id " +
                        "LEFT JOIN Matiere m ON n.nom_matiere = m.nom " +
                        "LEFT JOIN Enseignant ens ON m.id_enseignant = ens.id_enseignant " +
                        "WHERE n.id_etudiant = ? AND n.periode = ? " +
                        "ORDER BY n.nom_matiere";

        List<Note> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setString(2, periode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByEtudiantAndPeriode", ex);
        }
        return list;
    }

    public List<Note> findByPeriode(String periode) {
        String sql =
                "SELECT n.id, n.id_etudiant, n.periode, n.nom_matiere, n.valeur, n.created_at, " +
                        "       e.nom as etudiant_nom, e.prenom as etudiant_prenom, e.classe_id, " +
                        "       ens.nom as enseignant_nom, ens.prenom as enseignant_prenom " +
                        "FROM note n " +
                        "JOIN etudiant e ON n.id_etudiant = e.id " +
                        "LEFT JOIN Matiere m ON n.nom_matiere = m.nom " +
                        "LEFT JOIN Enseignant ens ON m.id_enseignant = ens.id_enseignant " +
                        "WHERE n.periode = ? " +
                        "ORDER BY e.nom, n.nom_matiere";

        List<Note> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, periode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByPeriode", ex);
        }
        return list;
    }

    public List<Note> findByMatiere(String matiere) {
        String sql =
                "SELECT n.id, n.id_etudiant, n.periode, n.nom_matiere, n.valeur, n.created_at, " +
                        "       e.nom as etudiant_nom, e.prenom as etudiant_prenom, e.classe_id, " +
                        "       ens.nom as enseignant_nom, ens.prenom as enseignant_prenom " +
                        "FROM note n " +
                        "JOIN etudiant e ON n.id_etudiant = e.id " +
                        "LEFT JOIN Matiere m ON n.nom_matiere = m.nom " +
                        "LEFT JOIN Enseignant ens ON m.id_enseignant = ens.id_enseignant " +
                        "WHERE n.nom_matiere = ? " +
                        "ORDER BY n.periode DESC, e.nom";

        List<Note> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, matiere);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur findByMatiere", ex);
        }
        return list;
    }

    public List<Note> findAll() {
        String sql =
                "SELECT n.id, n.id_etudiant, n.periode, n.nom_matiere, n.valeur, n.created_at, " +
                        "       e.nom as etudiant_nom, e.prenom as etudiant_prenom, e.classe_id, " +
                        "       ens.nom as enseignant_nom, ens.prenom as enseignant_prenom " +
                        "FROM note n " +
                        "JOIN etudiant e ON n.id_etudiant = e.id " +
                        "LEFT JOIN Matiere m ON n.nom_matiere = m.nom " +
                        "LEFT JOIN Enseignant ens ON m.id_enseignant = ens.id_enseignant " +
                        "ORDER BY n.created_at DESC";

        return executeQueryAll(sql);
    }

    private List<Note> executeQuery(String sql, int param) {
        List<Note> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur requête note", ex);
        }
        return list;
    }

    private List<Note> executeQueryAll(String sql) {
        List<Note> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur requête note", ex);
        }
        return list;
    }


    private Note mapRow(ResultSet rs) throws SQLException {
        Note n = new Note();
        n.setId(rs.getInt("id"));
        n.setEtudiantId(rs.getInt("id_etudiant"));
        n.setPeriode(rs.getString("periode"));
        n.setMatiere(rs.getString("nom_matiere"));
        n.setValeur(rs.getDouble("valeur"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            n.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        }

        try {
            int classeId = rs.getInt("classe_id");
            if (!rs.wasNull()) n.setClasseId(classeId);

            String nom = rs.getString("etudiant_nom");
            if (nom != null) n.setEtudiantNom(nom);

            String prenom = rs.getString("etudiant_prenom");
            if (prenom != null) n.setEtudiantPrenom(prenom);

            String ensNom = rs.getString("enseignant_nom");
            if (ensNom != null) n.setEnseignantNom(ensNom);

            String ensPrenom = rs.getString("enseignant_prenom");
            if (ensPrenom != null) n.setEnseignantPrenom(ensPrenom);
        } catch (SQLException e) {

        }

        return n;
    }
}
