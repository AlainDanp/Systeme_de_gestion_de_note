package gestion_Note.service;

import Event.EventDispatcher;
import Event.EventListener;
import Event.NoteCreatedEvent;
import Event.NoteDeletedEvent;
import Event.NoteUpdatedEvent;
import MVC.Role;
import MVC.SecurityContext;
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
    private final SecurityContext securityContext;

    public NoteServiceImpl(DataSource ds, NoteDao noteDao, SecurityContext securityContext) {
        this.ds = ds;
        this.noteDao = noteDao;
        this.securityContext = securityContext;
        this.eventDispatcher = EventDispatcher.getInstance();
        }

    public void setCurrentUser(Integer userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
    }

    @Override
    public Note creeNote(Note note) {
        securityContext.exigerDroitSurMatier(note.getMatiere());
        validerNote(note);

        Note created;
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);

            try {
                created = noteDao.save(c,note);
                c.commit();
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            }finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la création de la note", ex);
        }
        if (currentUserId != null && currentUserName != null){
            eventDispatcher.dispatch(new NoteCreatedEvent(
                    currentUserId,
                    currentUserName,
                    created,
                    created.getEtudiantNomComplet(),
                    created.getMatiere()
            ));
        }
        return created;
    }

    @Override
    public void modifierNote(Note note) {
        Note existante = noteDao.findById(note.getId()).orElseThrow(()
                -> new IllegalArgumentException("Note introuvable :" + note.getId()));
        securityContext.exigerDroitSurMatier(existante.getMatiere());
        securityContext.exigerDroitSurMatier(note.getMatiere());

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
    public void supprimerNote(Connection c,Integer id) {
        Note existante = noteDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note introuvable : " + id));
        securityContext.exigerDroitSurMatier(existante.getMatiere());
        if (id == null) {
            throw new IllegalArgumentException("L'ID est requis");
        }
        noteDao.delete(c,id);

        if (currentUserId != null && currentUserName != null) {
            eventDispatcher.dispatch(new NoteDeletedEvent(
                    currentUserId, currentUserName, existante, existante.getEtudiantNomComplet()));
        }
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
        if (etudiantId == null) throw new IllegalArgumentException("L'ID de l'étudiant est requis");
        securityContext.exigerAccesEtudiant(etudiantId);
        return filtrerSiRestreint(noteDao.findByEtudiant(etudiantId));
    }

    @Override
    public List<Note> listerNotesParEtudiantEtPeriode(Integer etudiantId, String periode) {
        if (etudiantId == null) {
            throw new IllegalArgumentException("L'ID de l'étudiant est requis");
        }
        if (periode == null || periode.isBlank()) {
            throw new IllegalArgumentException("La période est requise");
        }
        return filtrerSiRestreint(noteDao.findByEtudiantAndPeriode(etudiantId, periode));
    }

    @Override
    public List<Note> listerNotesParPeriode(String periode) {
        if (periode == null || periode.isBlank()) {
            throw new IllegalArgumentException("La période est requise");
        }
        return filtrerSiRestreint(noteDao.findByPeriode(periode));
    }

    @Override
    public List<Note> listerNotesParMatiere(String matiere) {
        if (matiere == null || matiere.isBlank()) {
            throw new IllegalArgumentException("La matière est requise");
        }
        if (estRestreintAUneMatiere()) {
            String matiereEns = securityContext.getMatiere();
            if (matiereEns == null || !matiereEns.equalsIgnoreCase(matiere)) {
                throw new SecurityException("Accès refusé : vous ne pouvez consulter que les notes de votre matière"
                        + (matiereEns == null ? "." : " (" + matiereEns + ")."));
            }
        }
        return filtrerSiRestreint(noteDao.findByMatiere(matiere));
    }

    @Override
    public List<Note> listerToutesLesNotes() {
        return filtrerSiRestreint(noteDao.findAll());
    }

    /** Un Enseignant "simple" (non Titulaire, non Admin) ne voit que les notes de sa propre matière. */
    private boolean estRestreintAUneMatiere() {
        return securityContext.getRole() == Role.ENSEIGNANT;
    }

    /**
     * Un Enseignant simple ne voit que les notes de sa matière ET dans une de ses classes assignées :
     * un élève qui suit sa matière mais appartient à une classe non assignée reste invisible.
     */
    private List<Note> filtrerSiRestreint(List<Note> notes) {
        if (!estRestreintAUneMatiere()) {
            return notes;
        }
        String matiere = securityContext.getMatiere();
        if (matiere == null) {
            return new ArrayList<>();
        }
        List<Note> filtrees = new ArrayList<>();
        for (Note n : notes) {
            if (matiere.equalsIgnoreCase(n.getMatiere()) && securityContext.aClasseAssignee(n.getClasseId())) {
                filtrees.add(n);
            }
        }
        return filtrees;
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