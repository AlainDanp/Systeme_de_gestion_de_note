package gestion_Classe.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Classe {
    private Integer idClasse;
    private String niveau;
    private Integer nombreEleves;
    private OffsetDateTime createdAt;

    public Classe(){}

    public Classe(Integer idClasse, String niveau, Integer nombreEleves) {
        this.idClasse = idClasse;
        this.niveau = niveau;
        this.nombreEleves = nombreEleves;
    }

    public Integer getIdClasse() {
        return idClasse;
    }

    public void setIdClasse(Integer idClasse) {
        this.idClasse = idClasse;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public Integer getNombreEleves() {
        return nombreEleves;
    }

    public void setNombreEleves(Integer nombreEleves) {
        this.nombreEleves = nombreEleves;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Classe{" +
                "idClasse=" + idClasse +
                ", niveau='" + niveau + '\'' +
                ", nombreEleves=" + nombreEleves +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classe)) return false;
        Classe classe = (Classe) o;
        return Objects.equals(idClasse, classe.idClasse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idClasse);
    }
}
