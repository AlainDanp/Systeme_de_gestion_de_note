package service;

import dao.BulletinDao;
import model.Bulletin;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BulletinServiceImpl implements BulletinService {

        private final DataSource ds;
        private final BulletinDao bulletinDao;

        public BulletinServiceImpl(DataSource ds, BulletinDao bulletinDao) {
            this.ds = ds;
            this.bulletinDao = bulletinDao;
        }

    @Override
    public Bulletin genererEtEnregistrerBulletin(Integer etudiantId, String periode) {
        if (etudiantId == null) throw new IllegalArgumentException("etudiantId requis");
        if (periode == null || periode.isBlank()) throw new IllegalArgumentException("periode requise");

        // calculs d'agrégats (moyenne étudiant, récupération classe, moyenne classe)
        final String sqlMoyEtudiant = "SELECT AVG(valeur) AS moyenne FROM note WHERE id_etudiant = ? AND periode = ?";
        final String sqlClasseId = "SELECT classe_id FROM etudiant WHERE id = ?";
        final String sqlMoyClasse = "SELECT AVG(n.valeur) AS moyenne_classe " +
                "FROM note n JOIN etudiant e ON n.id_etudiant = e.id " +
                "WHERE e.classe_id = ? AND n.periode = ?";

        Double moyenneEtudiant = null;
        Integer classeId = null;
        Double moyenneClasse = null;

        try (Connection c = ds.getConnection()) {
            // moyenne etudiant
            try (PreparedStatement ps = c.prepareStatement(sqlMoyEtudiant)) {
                ps.setInt(1, etudiantId);
                ps.setString(2, periode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double v = rs.getDouble("moyenne");
                        if (!rs.wasNull()) moyenneEtudiant = round2(v);
                    }
                }
            }

            // classe id
            try (PreparedStatement ps = c.prepareStatement(sqlClasseId)) {
                ps.setInt(1, etudiantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int cid = rs.getInt("classe_id");
                        if (!rs.wasNull()) classeId = cid;
                    }
                }
            }

            // moyenne classe
            if (classeId != null) {
                try (PreparedStatement ps = c.prepareStatement(sqlMoyClasse)) {
                    ps.setInt(1, classeId);
                    ps.setString(2, periode);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            double v = rs.getDouble("moyenne_classe");
                            if (!rs.wasNull()) moyenneClasse = round2(v);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur SQL lors des calculs:");
            System.err.println("Message: " + ex.getMessage());
            System.err.println("SQL State: " + ex.getSQLState());
            System.err.println("Error Code: " + ex.getErrorCode());
            ex.printStackTrace();
            throw new RuntimeException("Erreur lors des calculs de moyennes", ex);
        }

        Bulletin b = new Bulletin();
        b.setEtudiantId(etudiantId);
        b.setPeriode(periode);
        b.setMoyenne(moyenneEtudiant);
        b.setMoyennDelaClasse(moyenneClasse);

        // Persistance : save() si pas existant, update() sinon
        Optional<Bulletin> exist = bulletinDao.findByEtudiantAndPeriode(etudiantId, periode);
        if (exist.isPresent()) {
            // récupérer id au besoin
            b.setId(exist.get().getId());
            bulletinDao.update(b);
            // retourner l'enregistrement à jour (lire par id pour created_at)
            return exist.get();
        } else {
            try {
                return bulletinDao.save(b);
            } catch (RuntimeException ex) {
                // gestion simple d'une violation d'unicité due à concurrence
                Throwable cause = ex.getCause();
                if (cause instanceof SQLException) {
                    SQLException sqlEx = (SQLException) cause;
                    if ("23505".equals(sqlEx.getSQLState())) {
                        // conflit unique : lire et retourner l'existant
                        return bulletinDao.findByEtudiantAndPeriode(etudiantId, periode)
                                .orElseThrow(() -> new RuntimeException("Erreur après violation unique", ex));
                    }
                }
                throw ex;
            }
        }
    }

        @Override
        public Optional<Bulletin> getBulletin(Integer id) {
            return bulletinDao.findById(id);
        }

        @Override
        public Optional<Bulletin> getParEtudiantEtPeriode(Integer etudiantId, String periode) {
            return bulletinDao.findByEtudiantAndPeriode(etudiantId, periode);
        }

        @Override
        public void update(Bulletin bulletin) {
            bulletinDao.update(bulletin);
        }


        @Override
        public void delete(Integer id) {
            bulletinDao.delete(id);
        }

        @Override
        public List<Bulletin> findByEtudiant(Integer etudiantId) {
            return bulletinDao.findByEtudiant(etudiantId);
        }

        @Override
        public Bulletin creeBulletin(Bulletin bulletin) {
            if (bulletin.getEtudiantId() == null || bulletin.getPeriode() == null) {
                throw new IllegalArgumentException("etudiantId et periode requis pour créer un bulletin");}
            Optional<Bulletin> exist = bulletinDao.findByEtudiantAndPeriode(bulletin.getEtudiantId(), bulletin.getPeriode());
            if (exist.isPresent()) {
                bulletin.setId(exist.get().getId());
                bulletinDao.update(bulletin);
                return bulletinDao.findById(bulletin.getId()).orElse(bulletin);
            } else {
                return bulletinDao.save(bulletin);
            }
        }


        @Override
        public List<Bulletin> listerParEtudiant(Integer etudiantId) {
            final String sql = "SELECT id, id_etudiant, periode, moyenne, moyenne_de_la_classe, created_at " +
                    "FROM bulletin WHERE id_etudiant = ? ORDER BY periode DESC";
            List<Bulletin> result = new ArrayList<>();
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, etudiantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Bulletin b = mapRow(rs);
                        result.add(b);
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur listage bulletins", ex);
            }
            return result;
        }


        private Bulletin mapRow(ResultSet rs) throws SQLException {
            Bulletin b = new Bulletin();
            b.setId(rs.getInt("id"));
            b.setEtudiantId(rs.getInt("id_etudiant"));
            b.setPeriode(rs.getString("periode"));
            double m = rs.getDouble("moyenne");
            if (!rs.wasNull()) b.setMoyenne(round2(m));
            double mc = rs.getDouble("moyenne_de_la_classe");
            if (!rs.wasNull()) b.setMoyennDelaClasse(round2(mc));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) b.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
            return b;
        }

        private Double round2(double v) {
            return Math.round(v * 100.0) / 100.0;
        }
}
