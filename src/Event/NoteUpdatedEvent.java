package Event;

import gestion_Note.model.Note;

public class NoteUpdatedEvent extends BaseEvent{
    private final Note note;
    private final double ancienneValeur;
    private final double nouvelleValeur;
    private final String etudiantNom;
    private final String matiere;

    public NoteUpdatedEvent(Integer userId, String userName, Note note,
                            double ancienneValeur, String etudiantNom, String matiere) {
        super(userId, userName);
        this.note = note;
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = note.getValeur();
        this.etudiantNom = etudiantNom;
        this.matiere = matiere;
    }

    @Override
    public String getEventType() {
        return "NOTE_UPDATED";
    }

    @Override
    public String getDescription() {
        return String.format("Note de %s modifiée de %.2f à %.2f en %s",
                etudiantNom, ancienneValeur, nouvelleValeur, matiere);
    }

    public Note getNote() {
        return note;
    }

    public double getAncienneValeur() {
        return ancienneValeur;
    }

    public double getNouvelleValeur() {
        return nouvelleValeur;
    }

    public String getEtudiantNom() {
        return etudiantNom;
    }

    public String getMatiere() {
        return matiere;
    }
}
