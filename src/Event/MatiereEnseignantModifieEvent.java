package Event;

/** Déclenché quand l'enseignant assigné à une matière change (réassignation). */
public class MatiereEnseignantModifieEvent extends BaseEvent {

    private final String matiere;
    private final Integer ancienEnseignantId;
    private final Integer nouvelEnseignantId;

    public MatiereEnseignantModifieEvent(Integer userId, String userName, String matiere,
                                          Integer ancienEnseignantId, Integer nouvelEnseignantId) {
        super(userId, userName);
        this.matiere = matiere;
        this.ancienEnseignantId = ancienEnseignantId;
        this.nouvelEnseignantId = nouvelEnseignantId;
    }

    @Override
    public String getEventType() {
        return "MATIERE_ENSEIGNANT_MODIFIE";
    }

    @Override
    public String getDescription() {
        return String.format("Matière %s réassignée (enseignant #%s → #%s)",
                matiere, ancienEnseignantId, nouvelEnseignantId);
    }

    public String getMatiere() {
        return matiere;
    }

    public Integer getAncienEnseignantId() {
        return ancienEnseignantId;
    }

    public Integer getNouvelEnseignantId() {
        return nouvelEnseignantId;
    }
}
