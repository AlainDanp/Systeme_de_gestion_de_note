package gestion_Enseignant.model;

import MVC.Role;
import MVC.User;

import java.time.OffsetDateTime;

public class Enseignant extends User {
    private Integer idEnseignant;
    private OffsetDateTime createdAt;

    private String matiereNom;
    private boolean titulaire;

    public Enseignant() {}

    public Enseignant(Integer idEnseignant, String nom, String prenom, String email) {
        this.idEnseignant = idEnseignant;
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
    }

    /** Un enseignant Titulaire a les mêmes droits qu'un Enseignant, plus la gestion des bulletins. */
    @Override
    public Role getRole() {
        return titulaire ? Role.TITULAIRE : Role.ENSEIGNANT;
    }

    public boolean isTitulaire() {
        return titulaire;
    }

    public void setTitulaire(boolean titulaire) {
        this.titulaire = titulaire;
    }

    @Override
    public Integer getId() {
        return idEnseignant;
    }

    @Override
    public void setId(Integer id) {
        this.idEnseignant = id;
    }

    public Integer getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(Integer idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMatiereNom() {
        return matiereNom;
    }

    public void setMatiereNom(String matiereNom) {
        this.matiereNom = matiereNom;
    }

    @Override
    public String toString() {
        return "Enseignant{" +
                "id=" + idEnseignant +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", login='" + getLogin() + '\'' +
                ", matiere='" + matiereNom + '\'' +
                ", actif=" + isActif() +
                '}';
    }
}