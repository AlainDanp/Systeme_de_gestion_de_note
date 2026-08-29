package Event;

import gestion_Bulletin.model.Bulletin;

public class BulletinGeneratedEvent extends  BaseEvent{
    private final Bulletin bulletin;
    private final String etudiantNom;

    public BulletinGeneratedEvent(Integer userId, String userName, Bulletin bulletin,
                                  String etudiantNom) {
        super(userId, userName);
        this.bulletin = bulletin;
        this.etudiantNom = etudiantNom;
    }

    @Override
    public String getEventType() {
        return "BULLETIN_GENERATED";
    }

    @Override
    public String getDescription() {
        return String.format("Bulletin généré pour %s - Période: %s - Moyenne: %.2f/20",
                etudiantNom, bulletin.getPeriode(),
                bulletin.getMoyenne() != null ? bulletin.getMoyenne() : 0.0);
    }

    public Bulletin getBulletin() {
        return bulletin;
    }

    public String getEtudiantNom() {
        return etudiantNom;
    }

}
