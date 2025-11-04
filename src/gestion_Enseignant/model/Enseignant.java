package gestion_Enseignant.model;

import MVC.Role;
import MVC.User;

import java.time.OffsetDateTime;

public class Enseignant extends User {
    private Integer idEnseignant;
    private OffsetDateTime createdAt;

    private String matiereNom;

    public Enseignant() {}

    public Enseignant(Integer idEnseignant, String nom, String prenom, String email) {
        this.idEnseignant = idEnseignant;
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
    }

    @Override
    public Role getRole() {
        return Role.ENSEIGNANT;
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