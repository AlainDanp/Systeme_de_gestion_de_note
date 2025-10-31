package gestion_Matier.model;

import java.util.Objects;

public class Matiere {
    private String nom;
    private Integer idEnseignant;
    private String enseignantNom;
    private String enseignantPrenom;
    private String enseignantEmail;

    public Matiere() {}

    public Matiere(String nom, Integer idEnseignant) {
        this.nom = nom;
        this.idEnseignant = idEnseignant;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(Integer idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public String getEnseignantNom() {
        return enseignantNom;
    }

    public void setEnseignantNom(String enseignantNom) {
        this.enseignantNom = enseignantNom;
    }

    public String getEnseignantPrenom() {
        return enseignantPrenom;
    }

    public void setEnseignantPrenom(String enseignantPrenom) {
        this.enseignantPrenom = enseignantPrenom;
    }

    public String getEnseignantEmail() {
        return enseignantEmail;
    }

    public void setEnseignantEmail(String enseignantEmail) {
        this.enseignantEmail = enseignantEmail;
    }

    // Méthodes utilitaires
    public String getEnseignantNomComplet() {
        if (enseignantNom != null && enseignantPrenom != null) {
            return enseignantPrenom + " " + enseignantNom;
        }
        return "Enseignant #" + idEnseignant;
    }

    @Override
    public String toString() {
        return "Matiere{" +
                "nom='" + nom + '\'' +
                ", idEnseignant=" + idEnseignant +
                ", enseignant=" + getEnseignantNomComplet() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matiere)) return false;
        Matiere matiere = (Matiere) o;
        return Objects.equals(nom, matiere.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom);
    }
}