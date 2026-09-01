package Event;

import gestion_Note.model.Note;

public class NoteDeletedEvent extends BaseEvent {

    private final Integer etudiantId;
    private final String etudiantNom;
    private final String matiere;
    private final double valeur;

    public NoteDeletedEvent(Integer userId, String userName, Note note, String etudiantNom) {
        super(userId, userName);
        this.etudiantId = note.getEtudiantId();
        this.etudiantNom = etudiantNom;
        this.matiere = note.getMatiere();
        this.valeur = note.getValeur();
    }

    @Override
    public String getEventType() {
        return "NOTE_DELETED";
    }

    @Override
    public String getDescription() {
        return String.format("Note de %.2f/20 supprimée pour %s en %s", valeur, etudiantNom, matiere);
    }

    public Integer getEtudiantId() {
        return etudiantId;
    }

    public String getEtudiantNom() {
        return etudiantNom;
    }

    public String getMatiere() {
        return matiere;
    }

    public double getValeur() {
        return valeur;
    }
}
