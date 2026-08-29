package MVC;

public class SecurityContext {
    private Integer userId;
    private String userName;
    private Role role;
    private String matiere;

    public void setUser(Integer userId, String userName, Role role, String matiere){
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.matiere = matiere;
    }

    public void exigerDroitEcriture() {
        if(role == null) throw  new SecurityException("Utilisateur non authentifié");
        if(role == Role.ETUDIANT)
            throw new SecurityException("Droit d'écriture requis pour cette opération");
    }
    public void exigerDroitSurMatier(String matiereCible){
        exigerDroitEcriture();
        if (role == Role.ENSEIGNANT && (matiere == null || !matiere.equalsIgnoreCase(matiereCible))) {
            throw new SecurityException("Droit d'écriture requis sur la matière : " + matiereCible);
        }
    }

    public void exigerAccesEtudiant(Integer etudiantId) {
        if (role == null) throw new SecurityException("Aucun utilisateur authentifié");
        if (role == Role.ETUDIANT && !java.util.Objects.equals(userId, etudiantId))
            throw new SecurityException("Accès refusé aux données d'un autre étudiant");
    }
}
