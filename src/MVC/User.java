package MVC;

import java.time.OffsetDateTime;
import java.util.Objects;

public abstract class User {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String login;
    private String passwordHash;
    private boolean actif;
    private OffsetDateTime derniereConnexion;

    public User() {}


    public abstract Role getRole();


    public String getNomComplet() {
        return prenom + " " + nom;
    }

    // Getters et Setters

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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public OffsetDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(OffsetDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login);
    }
}