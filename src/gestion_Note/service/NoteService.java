package gestion_Note.service;

import gestion_Note.model.Note;
import java.util.List;
import java.util.Optional;

public interface NoteService {
    Note creeNote(Note note);
    void modifierNote(Note note);
    void supprimerNote(Integer id);
    Optional<Note> getNote(Integer id);
    List<Note> listerNotesParEtudiant(Integer etudiantId);
    List<Note> listerNotesParEtudiantEtPeriode(Integer etudiantId, String periode);
    List<Note> listerNotesParPeriode(String periode);
    List<Note> listerNotesParMatiere(String matiere);
    List<Note> listerToutesLesNotes();
    Double calculerMoyenneEtudiant(Integer etudiantId, String periode);
    List<String> listerMatieres();
}
