package Event;

import gestion_Note.model.Note;

public class NoteCreatedEvent  extends BaseEvent{

    private final Note note;
    private final String etudiantNom;
    private final String matiere;

    public NoteCreatedEvent(Integer userId, String userName, Note note,
                            String etudiantNom, String matiere) {
        super(userId, userName);
        this.note = note;
        this.etudiantNom = etudiantNom;
        this.matiere = matiere;
    }

    @Override
    public String getEventType() {
        return "NOTE_CREATED";
    }

    @Override
    public String getDescription() {
        return String.format("Note de %.2f/20 créée pour %s en %s",
                note.getValeur(), etudiantNom, matiere);
    }

    public Note getNote() {
        return note;
    }

    public String getEtudiantNom() {
        return etudiantNom;
    }

    public String getMatiere() {
        return matiere;
    }

}
