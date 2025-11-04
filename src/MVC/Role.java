package MVC;

public enum Role {
    ENSEIGNANT("Enseignant", "Accès complet aux notes, bulletins et matières"),
    ETUDIANT("Étudiant", "Consultation uniquement de ses propres données"),
    ADMIN("Administrateur", "Accès complet au système");

    private final String libelle;
    private final String description;

    Role(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

}
