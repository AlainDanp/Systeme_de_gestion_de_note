package gestion_Bulletin.service;

import gestion_Bulletin.dao.BulletinDao;
import gestion_Bulletin.model.Bulletin;
import gestion_Bulletin.model.NoteDetail;

import javax.sql.DataSource;
import java.sql.*;
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
        if (!periode.matches("\\d{4}-(S|T|s|t)\\d+")) {
            throw new IllegalArgumentException("Format de période invalide. Attendu : YYYY-S1 ou YYYY-T1");
        }

        // Requête SQL optimisée avec id_etudiant
        final String sql =
                "WITH moyenne_etudiant AS ( " +
                        "    SELECT AVG(valeur) AS moy " +
                        "    FROM note " +
                        "    WHERE id_etudiant = ? AND periode = ? " +
                        "), " +
                        "classe_info AS ( " +
                        "    SELECT classe_id FROM etudiant WHERE id = ? " +
                        "), " +
                        "moyenne_classe AS ( " +
                        "    SELECT AVG(n.valeur) AS moy_classe " +
                        "    FROM note n " +
                        "    JOIN etudiant e ON n.id_etudiant = e.id " +
                        "    CROSS JOIN classe_info ci " +
                        "    WHERE e.classe_id = ci.classe_id AND n.periode = ? " +
                        ") " +
                        "SELECT " +
                        "    (SELECT moy FROM moyenne_etudiant) AS moyenne_etudiant, " +
                        "    (SELECT moy_classe FROM moyenne_classe) AS moyenne_classe";

        Double moyenneEtudiant = null;
        Double moyenneClasse = null;

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, etudiantId);
            ps.setString(2, periode);
            ps.setInt(3, etudiantId);
            ps.setString(4, periode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double me = rs.getDouble("moyenne_etudiant");
                    if (!rs.wasNull()) moyenneEtudiant = round2(me);

                    double mc = rs.getDouble("moyenne_classe");
                    if (!rs.wasNull()) moyenneClasse = round2(mc);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur SQL lors des calculs:");
            System.err.println("Message: " + ex.getMessage());
            System.err.println("SQL State: " + ex.getSQLState());
            ex.printStackTrace();
            throw new RuntimeException("Erreur lors des calculs de moyennes", ex);
        }

        Bulletin b = new Bulletin();
        b.setEtudiantId(etudiantId);
        b.setPeriode(periode);
        b.setMoyenne(moyenneEtudiant);
        b.setMoyennDelaClasse(moyenneClasse);

        Optional<Bulletin> exist = bulletinDao.findByEtudiantAndPeriode(etudiantId, periode);
        if (exist.isPresent()) {
            b.setId(exist.get().getId());
            bulletinDao.update(b);
            return bulletinDao.findById(b.getId()).orElse(b);
        } else {
            try {
                return bulletinDao.save(b);
            } catch (RuntimeException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof SQLException) {
                    SQLException sqlEx = (SQLException) cause;
                    if ("23505".equals(sqlEx.getSQLState())) {
                        System.out.println("Conflit détecté : le bulletin a été créé par un autre processus.");
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
    public List<NoteDetail> getNotesAvecEnseignants(Integer etudiantId, String periode) {
        String sql =
                "SELECT n.matiere, n.valeur, e.nom as ens_nom, e.prenom as ens_prenom " +
                        "FROM note n " +
                        "LEFT JOIN Matiere m ON n.matiere = m.nom " +
                        "LEFT JOIN Enseignant e ON m.id_enseignant = e.id_enseignant " +
                        "WHERE n.id_etudiant = ? AND n.periode = ? " +
                        "ORDER BY n.matiere";

        List<NoteDetail> details = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setString(2, periode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NoteDetail nd = new NoteDetail();
                    nd.setMatiere(rs.getString("matiere"));
                    nd.setValeur(rs.getDouble("valeur"));
                    nd.setEnseignantNom(rs.getString("ens_nom"));
                    nd.setEnseignantPrenom(rs.getString("ens_prenom"));
                    details.add(nd);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur récupération notes avec enseignants", ex);
        }
        return details;
    }


    @Override
        public List<Bulletin> listerParEtudiant(Integer etudiantId) {
            return bulletinDao.findByEtudiant(etudiantId);
        }


        private Double round2(double v) {
            return Math.round(v * 100.0) / 100.0;
        }
}
