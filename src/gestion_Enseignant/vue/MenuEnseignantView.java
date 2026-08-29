package gestion_Enseignant.vue;

import Auhentifcation.AuthenticationService;
import Auhentifcation.ProfilView;
import MVC.NotificationView;
import gestion_Bulletin.vue.BulletinView;
import gestion_Enseignant.model.Enseignant;
import gestion_Matiere.vue.MatiereView;
import gestion_Note.vue.NoteView;


import java.util.Scanner;

public class MenuEnseignantView {
    private final AuthenticationService authService;
    private final Scanner scanner;
    private final NoteView noteView;
    private final BulletinView bulletinView;
    private final MatiereView matiereView;
    private final ProfilView profilView;
    private final NotificationView notificationView;

    public MenuEnseignantView( AuthenticationService authService,
                               Scanner scanner,
                               NoteView noteView,
                               BulletinView bulletinView,
                               MatiereView matiereView,
                               ProfilView profilView,
                               NotificationView notificationView) {
        this.bulletinView = bulletinView;
        this.scanner = scanner;
        this.noteView = noteView;
        this.profilView = profilView;
        this.matiereView = matiereView;
        this.authService = authService;
        this.notificationView = notificationView;
    }

    public void afficher(){
        Enseignant enseignant = (Enseignant) authService.getCurrentUser().orElse(null);
        if(enseignant == null){
            System.out.println("Session expirée");
            return;
        }

        while (true){
            afficherMenu(enseignant);
            String choix = scanner.nextLine().trim();

            if(!traiterChoix(choix)){
                break;
            }
        }
    }


    private void afficherMenu(Enseignant enseignant){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  ESPACE ENSEIGNANT                             ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║    Connecté : %-48s ║%n", enseignant.getNomComplet());

        if (enseignant.getMatiereNom() != null) {
            System.out.printf("║    Matière  : %-48s ║%n", enseignant.getMatiereNom());
        }

        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1.  Gestion des Notes                                         ║");
        System.out.println("║  2.  Gestion des Bulletins                                     ║");
        System.out.println("║  3.  Gestion des Matières                                      ║");
        System.out.println("║  4.  Mon Profil                                                ║");
        System.out.println("║  0.  Déconnexion                                               ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix){
        switch(choix){
            case "1":
                noteView.menu();
                break;
            case "2":
                bulletinView.menu();
                break;
            case "3":
                matiereView.menu();
                break;
            case"4":
                profilView.menu();
                break;
            case "0":
                if(confirmerDeconnexion()){
                    authService.logout();
                    return false;
                }
                break;
            default:
                System.out.println("Choix invalide");
                attendreEntree();
        }
        return  true;
    }
    private boolean confirmerDeconnexion() {
        System.out.print("\n  Voulez-vous vraiment vous déconnecter ? (o/N) : ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        return "o".equals(confirmation) || "oui".equals(confirmation);
    }

    private void attendreEntree() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }


}
