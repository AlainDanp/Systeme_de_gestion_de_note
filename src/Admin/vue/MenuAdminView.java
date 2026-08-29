package Admin.vue;

import Auhentifcation.AuthenticationService;
import Auhentifcation.ProfilView;
import MVC.NotificationView;
import gestion_Bulletin.vue.BulletinView;
import gestion_Classe.vue.ClasseView;
import gestion_Enseignant.vue.EnseignantView;
import gestion_Etudiant.vue.EtudiantView;
import gestion_Matiere.vue.MatiereView;
import gestion_Note.vue.NoteView;
import Admin.model.Admin;

import java.util.Scanner;

public class MenuAdminView {
    private final AuthenticationService authService;
    private final Scanner scanner;
    private final EnseignantView enseignantView;
    private final EtudiantView etudiantView;
    private final ClasseView classeView;
    private final MatiereView matiereView;
    private final NoteView noteView;
    private final BulletinView bulletinView;
    private final ProfilView profilView;
    private final NotificationView notificationView;

    public MenuAdminView(
            AuthenticationService authService,
            Scanner scanner,
            EnseignantView enseignantView,
            EtudiantView etudiantView,
            ClasseView classeView,
            MatiereView matiereView,
            NoteView noteView,
            BulletinView bulletinView,
            ProfilView profilView,
            NotificationView notificationView) {
        this.authService = authService;
        this.scanner = scanner;
        this.enseignantView = enseignantView;
        this.etudiantView = etudiantView;
        this.classeView = classeView;
        this.matiereView = matiereView;
        this.noteView = noteView;
        this.bulletinView = bulletinView;
        this.profilView = profilView;
        this.notificationView = notificationView;
    }

    public void afficher() {
        Admin admin = (Admin) authService.getCurrentUser().orElse(null);
        if (admin == null) {
            System.out.println(" Session expirée");
            return;
        }

        while (true) {
            afficherMenu(admin);
            String choix = scanner.nextLine().trim();

            if (!traiterChoix(choix, admin)) {
                break;
            }
        }
    }

    private void afficherMenu(Admin admin) {
        String notificationBadge = notificationView.getBadge(admin.getId());

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  ESPACE ADMINISTRATEUR                         ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  👤 Connecté : %-48s║%n", admin.getNomComplet());
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║                      GESTION                                   ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. 👨‍🏫 Gestion des Enseignants                                 ║");
        System.out.println("║  2. 👨‍🎓 Gestion des Étudiants                                   ║");
        System.out.println("║  3. 🏫 Gestion des Classes                                     ║");
        System.out.println("║  4. 📚 Gestion des Matières                                    ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║                      SCOLAIRE                                  ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  5. 📝 Gestion des Notes                                       ║");
        System.out.println("║  6. 📊 Gestion des Bulletins                                   ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║                      COMPTE                                    ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  7. 📬 Notifications%-41s║%n", notificationBadge);
        System.out.println("║  8. 👤 Mon Profil                                              ║");
        System.out.println("║  0. 🚪 Déconnexion                                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix, Admin admin) {
        switch (choix) {
            case "1":
                enseignantView.menu();
                nettoyerEcran();
                break;
            case "2":
                etudiantView.menu();
                nettoyerEcran();
                break;
            case "3":
                classeView.menu();
                nettoyerEcran();
                break;
            case "4":
                matiereView.menu();
                nettoyerEcran();
                break;
            case "5":
                noteView.menu();
                nettoyerEcran();
                break;
            case "6":
                bulletinView.menu();
                nettoyerEcran();
                break;
            case "7":
                notificationView.afficherNotifications(admin.getId());
                nettoyerEcran();
                break;
            case "8":
                profilView.menu();
                nettoyerEcran();
                break;
            case "0":
                if (confirmerDeconnexion()) {
                    authService.logout();
                    return false;
                }
                break;
            default:
                System.out.println("❌Choix invalide.");
                attendreEntree();
        }
        return true;
    }

    private void nettoyerEcran() {
        for (int i = 0; i < 3; i++) {
            System.out.println();
        }
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("            Retour à l'espace administrateur");
        System.out.println("═══════════════════════════════════════════════════════════════");

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            // Ignorer
        }
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