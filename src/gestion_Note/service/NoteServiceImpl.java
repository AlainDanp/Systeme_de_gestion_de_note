package gestion_Note.service;

import Event.EventDispatcher;
import Event.EventListener;
import Event.NoteCreatedEvent;
import Event.NoteUpdatedEvent;
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
    private final EventDispatcher eventDispatcher;
    private Integer currentUserId;
    private String currentUserName;

    public NoteServiceImpl(DataSource ds, NoteDao noteDao) {
        this.ds = ds;
        this.noteDao = noteDao;
        this.eventDispatcher = EventDispatcher.getInstance();
        }

    public void setCurrentUser(Integer userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
    }

    @Override
    public Note creeNote(Note note) {
        validerNote(note);

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);

            try {
                Note created = noteDao.save(note);
                c.commit();

                //  Déclencher l'événement NOTE_CREATED
                if (currentUserId != null && currentUserName != null) {
                    NoteCreatedEvent event = new NoteCreatedEvent(
                            currentUserId,
                            currentUserName,
                            created,
                            created.getEtudiantNomComplet(),
                            created.getMatiere()
                    );
                    eventDispatcher.dispatch(event);
                }

                return created;

            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la création de la note", ex);
        }
    }


    @Override
    public void modifierNote(Note note) {
        if (note  == null) {
            throw new IllegalArgumentException("L'ID de la note est requis");
        }
        validerNote(note);
        // Récupérer l'ancienne valeur
        Optional<Note> oldNoteOpt = noteDao.findById(note.getIdNote());
        double ancienneValeur = oldNoteOpt.map(Note::getValeur).orElse(0.0);

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);

            try {
                noteDao.update(note);
                c.commit();

                //  Déclencher l'événement NOTE_UPDATED
                if (currentUserId != null && currentUserName != null) {
                    NoteUpdatedEvent event = new NoteUpdatedEvent(
                            currentUserId,
                            currentUserName,
                            note,
                            ancienneValeur,
                            note.getEtudiantNomComplet(),
                            note.getMatiere()
                    );
                    eventDispatcher.dispatch(event);
                }

            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la modification de la note", ex);
        }
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