package MVC;

import java.util.List;

public class SecurityContext {
    private Integer userId;
    private String userName;
    private Role role;
    private String matiere;
    private List<Integer> classeIds = List.of();

    public void setUser(Integer userId, String userName, Role role, String matiere){
        setUser(userId, userName, role, matiere, List.of());
    }

    /** @param classeIds classes assignées à l'utilisateur (pertinent pour Enseignant/Titulaire). */
    public void setUser(Integer userId, String userName, Role role, String matiere, List<Integer> classeIds){
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.matiere = matiere;
        this.classeIds = classeIds == null ? List.of() : classeIds;
    }

    public Role getRole() {
        return role;
    }

    public String getMatiere() {
        return matiere;
    }

    public List<Integer> getClasseIds() {
        return classeIds;
    }

    public boolean aClasseAssignee(Integer classeId) {
        return classeId != null && classeIds.contains(classeId);
    }

    public void exigerDroitEcriture() {
        if(role == null) throw  new SecurityException("Utilisateur non authentifié");
        if(role == Role.ETUDIANT)
            throw new SecurityException("Droit d'écriture requis pour cette opération");
    }
    public void exigerDroitSurMatier(String matiereCible){
        exigerDroitEcriture();
        if ((role == Role.ENSEIGNANT || role == Role.TITULAIRE)
                && (matiere == null || !matiere.equalsIgnoreCase(matiereCible))) {
            throw new SecurityException("Droit d'écriture requis sur la matière : " + matiereCible);
        }
    }

    /** Réservé à l'Administrateur et à l'Enseignant Titulaire : génération/modification/suppression des bulletins. */
    public void exigerDroitBulletin() {
        if (role == null) throw new SecurityException("Utilisateur non authentifié");
        if (role != Role.ADMIN && role != Role.TITULAIRE) {
            throw new SecurityException("Seul un enseignant titulaire ou un administrateur peut gérer les bulletins.");
        }
    }

    public void exigerAccesEtudiant(Integer etudiantId) {
        if (role == null) throw new SecurityException("Aucun utilisateur authentifié");
        if (role == Role.ETUDIANT && !java.util.Objects.equals(userId, etudiantId))
            throw new SecurityException("Accès refusé aux données d'un autre étudiant");
    }
}
