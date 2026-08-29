package Event;

import gestion_Etudiant.model.Etudiant;

public class EtudiantCreatedEvent extends  BaseEvent {
    private final Etudiant etudiant;


    public EtudiantCreatedEvent(Integer userId, String userName, Etudiant etudiant) {
        super(userId, userName);
        this.etudiant = etudiant;
    }

    @Override
    public String getEventType() {
        return "ETUDIANT_CREATED";
    }

    @Override
    public String getDescription() {
        return String.format("Nouvel étudiant créé: %s (Login: %s)",
                etudiant.getNomComplet(), etudiant.getLogin());
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

}
