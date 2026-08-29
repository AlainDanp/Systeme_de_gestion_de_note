package gestion_Note.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Note {
    private Integer id;
    private Integer etudiantId;
    private String periode;
    private String matiere;
    private Double valeur;
    private OffsetDateTime createdAt;

    private String etudiantNom;
    private String etudiantPrenom;
    private String enseignantNom;
    private String enseignantPrenom;

    public Note() {}

    public Note(Integer id, Integer etudiantId, String periode, String matiere, Double valeur, OffsetDateTime createdAt) {
        this.id = id;
        this.etudiantId = etudiantId;
        this.periode = periode;
        this.matiere = matiere;
        this.valeur = valeur;
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

    public void setEtudiantId(Integer etudiantId) {
        this.etudiantId = etudiantId;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public Double getValeur() {
        return valeur;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEtudiantNom() {
        return etudiantNom;
    }

    public void setEtudiantNom(String etudiantNom) {
        this.etudiantNom = etudiantNom;
    }

    public String getEtudiantPrenom() {
        return etudiantPrenom;
    }

    public void setEtudiantPrenom(String etudiantPrenom) {
        this.etudiantPrenom = etudiantPrenom;
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

    // Méthodes utilitaires
    public String getEtudiantNomComplet() {
        if (etudiantNom != null && etudiantPrenom != null) {
            return etudiantPrenom + " " + etudiantNom;
        }
        return "Étudiant #" + etudiantId;
    }

    public String getEnseignantNomComplet() {
        if (enseignantNom != null && enseignantPrenom != null) {
            return enseignantPrenom + " " + enseignantNom;
        }
        return "-";
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", etudiantId=" + etudiantId +
                ", periode='" + periode + '\'' +
                ", matiere='" + matiere + '\'' +
                ", valeur=" + valeur +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;
        Note note = (Note) o;
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Integer getIdEtudiant() {
        return etudiantId;
    }

    public int getIdNote() {
        return id;
    }
}