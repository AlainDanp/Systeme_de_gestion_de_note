package gestion_Bulletin.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Bulletin {
    private Integer id;
    private Integer etudiantId;
    private String periode;
    private Double moyenne;
    private Double moyennDelaClasse;
    private OffsetDateTime createdAt;

    private String etudiantNom;
    private String etudiantPrenom;
    private String classeNiveau;

    public Bulletin(){}

    public Bulletin(Integer id, Integer etudiantId, String periode, Double moyenne, Double moyennDelaClasse, OffsetDateTime createdAt) {
        this.id = id;
        this.etudiantId = etudiantId;
        this.periode = periode;
        this.moyenne = moyenne;
        this.moyennDelaClasse = moyennDelaClasse;
        this.createdAt = createdAt;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getEtudiantId() {
        return etudiantId;
    }
    public String getPeriode() {
        return periode;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public Double getMoyennDelaClasse() {
        return moyennDelaClasse;
    }
    public Double getMoyenne() {
        return moyenne;
    }
    public String getEtudiantNom(){return etudiantNom;}
    public String getEtudiantPrenom(){return etudiantPrenom;}
    public String getClasseNiveau(){return classeNiveau;}

    public void setEtudiantId(Integer etudiantId) {
        this.etudiantId = etudiantId;
    }
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setMoyennDelaClasse(Double moyennDelaClasse) {
        this.moyennDelaClasse = moyennDelaClasse;
    }
    public void setMoyenne(Double moyenne) {
        this.moyenne = moyenne;
    }
    public void setPeriode(String periode) {
        this.periode = periode;
    }
    public void setEtudiantNom(String etudiantNom){this.etudiantNom = etudiantNom;}
    public void setEtudiantPrenom(String etudiantPrenom){this.etudiantPrenom = etudiantPrenom;}
    public void setClasseNiveau(String classeNiveau){this.classeNiveau = classeNiveau;}

    public String getEtudiantNomComplet() {
        if (etudiantNom != null && etudiantPrenom != null) {
            return etudiantPrenom + " " + etudiantNom;
        }
        return "Étudiant #" + etudiantId;
    }

    @Override
    public String toString() {
        return "Bulletin{" +
                "id=" + id +
                ", etudiantId=" + etudiantId +
                ", periode='" + periode + '\'' +
                ", moyenne=" + moyenne +
                ", moyenneDeLaClasse=" + moyennDelaClasse +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bulletin)) return false;
        Bulletin bulletin = (Bulletin) o;
        return Objects.equals(id, bulletin.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }



}
