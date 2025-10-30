package gestion_Bulletin.model;

public class NoteDetail {
    private String matiere;
    private Double valeur;
    private String enseignantNom;
    private String enseignantPrenom;

    public NoteDetail() {}

    public NoteDetail(String matiere, Double valeur, String enseignantNom, String enseignantPrenom) {
        this.matiere = matiere;
        this.valeur = valeur;
        this.enseignantNom = enseignantNom;
        this.enseignantPrenom = enseignantPrenom;
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

    public String getEnseignantNomComplet() {
        if (enseignantNom != null && enseignantPrenom != null) {
            return enseignantPrenom + " " + enseignantNom;
        }
        return "-";
    }
}
