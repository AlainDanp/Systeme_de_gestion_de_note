package Admin.model;

import MVC.Role;
import MVC.User;

import java.time.OffsetDateTime;

public class Admin extends User {
    private Integer idAdmin;
    private OffsetDateTime createdAt;

    public Admin() {}

    public Admin(Integer idAdmin, String nom, String prenom, String email) {
        this.idAdmin = idAdmin;
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
    @Override
    public Integer getId() {
        return idAdmin;
    }

    @Override
    public void setId(Integer id) {
        this.idAdmin = id;
    }
    public Integer getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(Integer idAdmin) {
        this.idAdmin = idAdmin;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + idAdmin +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", login='" + getLogin() + '\'' +
                ", actif=" + isActif() +
                '}';
    }

}
