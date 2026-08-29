package Event;

import gestion_Enseignant.model.Enseignant;

public class EnseignantCreatedEvent extends  BaseEvent{

    private final Enseignant enseignant;

    public EnseignantCreatedEvent(Integer userId, String userName, Enseignant enseignant) {
        super(userId, userName);
        this.enseignant = enseignant;
    }

    @Override
    public String getEventType() {
        return "ENSEIGNANT_CREATED";
    }

    @Override
    public String getDescription() {
        return String.format("Nouvel enseignant créé: %s (Login: %s)",
                enseignant.getNomComplet(), enseignant.getLogin());
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }


}
