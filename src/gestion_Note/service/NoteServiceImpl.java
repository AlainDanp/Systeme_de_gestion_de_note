package gestion_Note.service;

import gestion_Note.model.Note;
import gestion_Note.dao.NoteDao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteServiceImpl implements NoteService{
    private final DataSource ds;
    private final NoteDao noteDao;

    public NoteServiceImpl(DataSource ds, NoteDao noteDao) {
        this.ds = ds;
        this.noteDao = noteDao;
    }


    @Override
    public Note creeNote(Note note) {
        validerNote(note);

        if (!etudiantExiste(note.getEtudiantId())) {
            throw new IllegalArgumentException("L'étudiant avec l'ID " + note.getEtudiantId() + " n'existe pas");
        }

        if (!matiereExiste(note.getMatiere())) {
            throw new IllegalArgumentException("La matière '" + note.getMatiere() + "' n'existe pas");
        }

        return noteDao.save(note);
    }


    @Override
    public void modifierNote(Note note) {
        if(note.getId() == null){
            throw new IllegalArgumentException("L'ID de la note est requis pour la modification");
        }
        validerNote(note);
        noteDao.update(note);
    }

    @Override
    public void supprimerNote(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID est requis");
        }
        noteDao.delete(id);
    }

    @Override
    public Optional<Note> getNote(Integer id) {
        if(id == null){
            throw new IllegalArgumentException("L'ID est requis");
        }
        return noteDao.findById(id);
    }

    @Override
    public List<Note> listerNotesParEtudiant(Integer etudiantId) {
        if (etudiantId == null) {
            throw new IllegalArgumentException("L'ID de l'étudiant est requis");
        }
        return noteDao.findByEtudiant(etudiantId);
    }

    @Override
    public List<Note> listerNotesParEtudiantEtPeriode(Integer etudiantId, String periode) {
        if (etudiantId == null) {
            throw new IllegalArgumentException("L'ID de l'étudiant est requis");
        }
        if (periode == null || periode.isBlank()) {
            throw new IllegalArgumentException("La période est requise");
        }
        return noteDao.findByEtudiantAndPeriode(etudiantId, periode);
    }

    @Override
    public List<Note> listerNotesParPeriode(String periode) {
        if (periode == null || periode.isBlank()) {
            throw new IllegalArgumentException("La période est requise");
        }
        return noteDao.findByPeriode(periode);
    }

    @Override
    public List<Note> listerNotesParMatiere(String matiere) {
        if (matiere == null || matiere.isBlank()) {
            throw new IllegalArgumentException("La matière est requise");
        }
        return noteDao.findByMatiere(matiere);
    }

    @Override
    public List<Note> listerToutesLesNotes() {
        return noteDao.findAll();
    }

    @Override
    public Double calculerMoyenneEtudiant(Integer etudiantId, String periode) {
        List<Note> notes = noteDao.findByEtudiantAndPeriode(etudiantId, periode);
        if (notes.isEmpty()) {
            return null;
        }

        double somme = 0.0;
        for (Note n : notes) {
            somme += n.getValeur();
        }

        return Math.round((somme / notes.size()) * 100.0) / 100.0;
    }

    @Override
    public List<String> listerMatieres() {
        String sql = "SELECT nom FROM Matiere ORDER BY nom";
        List<String> matieres = new ArrayList<>();

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                matieres.add(rs.getString("nom"));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la récupération des matières", ex);
        }

        return matieres;
    }

    private void validerNote(Note note) {
        if (note.getEtudiantId() == null) {
            throw new IllegalArgumentException("L'ID de l'étudiant est requis");
        }
        if (note.getPeriode() == null || note.getPeriode().isBlank()) {
            throw new IllegalArgumentException("La période est requise");
        }
        if (note.getMatiere() == null || note.getMatiere().isBlank()) {
            throw new IllegalArgumentException("La matière est requise");
        }
        if (note.getValeur() == null) {
            throw new IllegalArgumentException("La valeur de la note est requise");
        }
        if (note.getValeur() < 0 || note.getValeur() > 20) {
            throw new IllegalArgumentException("La note doit être entre 0 et 20");
        }

        if (!note.getPeriode().matches("\\d{4}-(S|T|s|t)\\d+")) {
            throw new IllegalArgumentException("Format de période invalide. Utilisez : YYYY-S1 ou YYYY-T1");
        }
    }

    private boolean etudiantExiste(Integer etudiantId){
        String sql = "SELECT COUNT(*) FROM etudiant WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la vérification de l'étudiant", ex);
        }
        return false;
    }

    private boolean matiereExiste(String matiere) {
        String sql = "SELECT COUNT(*) FROM Matiere WHERE nom = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, matiere);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la vérification de la matière", ex);
        }
        return false;
    }
}