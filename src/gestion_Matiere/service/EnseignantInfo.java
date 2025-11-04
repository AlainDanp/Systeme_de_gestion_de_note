package gestion_Matiere.service;

public class EnseignantInfo {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private boolean assigneAMatiere;

    public EnseignantInfo() {}

    public EnseignantInfo(Integer id, String nom, String prenom, String email, boolean assigneAMatiere) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.assigneAMatiere = assigneAMatiere;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAssigneAMatiere() {
        return assigneAMatiere;
    }

    public void setAssigneAMatiere(boolean assigneAMatiere) {
        this.assigneAMatiere = assigneAMatiere;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return getNomComplet() + (assigneAMatiere ? " (déjà assigné)" : " (disponible)");
    }
}
