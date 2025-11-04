package gestion_Etudiant.model;

import MVC.Role;
import MVC.User;

import java.time.OffsetDateTime;

public class Etudiant extends User {
    private Integer idEtudiant;
    private Integer classeId;
    private OffsetDateTime createdAt;

    private String classeNiveau;

    public Etudiant() {}

    public Etudiant(Integer idEtudiant, String nom, String prenom, Integer classeId) {
        this.idEtudiant = idEtudiant;
        setNom(nom);
        setPrenom(prenom);
        this.classeId = classeId;
    }

    @Override
    public Role getRole() {
        return Role.ETUDIANT;
    }

    @Override
    public Integer getId() {
        return idEtudiant;
    }

    @Override
    public void setId(Integer id) {
        this.idEtudiant = id;
    }

    public Integer getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(Integer idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public Integer getClasseId() {
        return classeId;
    }

    public void setClasseId(Integer classeId) {
        this.classeId = classeId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getClasseNiveau() {
        return classeNiveau;
    }

    public void setClasseNiveau(String classeNiveau) {
        this.classeNiveau = classeNiveau;
    }

    @Override
    public String toString() {
        return "Etudiant{" +
                "id=" + idEtudiant +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", login='" + getLogin() + '\'' +
                ", classe='" + classeNiveau + '\'' +
                ", actif=" + isActif() +
                '}';
    }


}
