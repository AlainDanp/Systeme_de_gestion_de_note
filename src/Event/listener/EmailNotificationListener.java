package Event.listener;


import Event.*;

public class EmailNotificationListener implements EventListener<Event> {

    private boolean enabled = true;

    @Override
    public void onEvent(Event event) {
        if (!enabled) return;

        switch (event.getEventType()) {
            case "NOTE_CREATED" -> sendNoteCreatedEmail((NoteCreatedEvent) event);
            case "BULLETIN_GENERATED" -> sendBulletinGeneratedEmail((BulletinGeneratedEvent) event);
            case "ETUDIANT_CREATED" -> sendWelcomeEmail((EtudiantCreatedEvent) event);
            case "ENSEIGNANT_CREATED" -> sendWelcomeEmail((EnseignantCreatedEvent) event);
        }
    }

    @Override
    public String[] getEventTypes() {
        return new String[]{
                "NOTE_CREATED",
                "BULLETIN_GENERATED",
                "ETUDIANT_CREATED",
                "ENSEIGNANT_CREATED"
        };
    }

    @Override
    public String getName() {
        return "EmailNotificationListener";
    }

    private void sendNoteCreatedEmail(NoteCreatedEvent event) {
        System.out.println("\n📧 [SIMULATION EMAIL]");
        System.out.println("   À      : " + event.getEtudiantNom());
        System.out.println("   Sujet  : Nouvelle note en " + event.getMatiere());
        System.out.println("   Message: Une nouvelle note a été saisie : " +
                event.getNote().getValeur() + "/20");
        System.out.println("   ---");
    }

    private void sendBulletinGeneratedEmail(BulletinGeneratedEvent event) {
        System.out.println("\n📧 [SIMULATION EMAIL]");
        System.out.println("   À      : " + event.getEtudiantNom());
        System.out.println("   Sujet  : Nouveau bulletin disponible");
        System.out.println("   Message: Votre bulletin pour la période " +
                event.getBulletin().getPeriode() + " est disponible.");
        System.out.println("   Moyenne: " + event.getBulletin().getMoyenne() + "/20");
        System.out.println("   ---");
    }

    private void sendWelcomeEmail(EtudiantCreatedEvent event) {
        System.out.println("\n📧 [SIMULATION EMAIL]");
        System.out.println("   À      : " + event.getEtudiant().getEmail());
        System.out.println("   Sujet  : Bienvenue sur la plateforme");
        System.out.println("   Message: Bonjour " + event.getEtudiant().getPrenom() + ",");
        System.out.println("            Votre compte a été créé avec succès.");
        System.out.println("            Login: " + event.getEtudiant().getLogin());
        System.out.println("   ---");
    }

    private void sendWelcomeEmail(EnseignantCreatedEvent event) {
        System.out.println("\n📧 [SIMULATION EMAIL]");
        System.out.println("   À      : " + event.getEnseignant().getEmail());
        System.out.println("   Sujet  : Bienvenue sur la plateforme");
        System.out.println("   Message: Bonjour " + event.getEnseignant().getPrenom() + ",");
        System.out.println("            Votre compte enseignant a été créé.");
        System.out.println("            Login: " + event.getEnseignant().getLogin());
        System.out.println("   ---");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}