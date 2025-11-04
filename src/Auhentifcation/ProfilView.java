package Auhentifcation;

import MVC.User;

import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ProfilView {
    private final AuthenticationService authService;
    private final Scanner scanner;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ProfilView(AuthenticationService authService, Scanner scanner) {
        this.authService = authService;
        this.scanner = scanner;
    }

    public void menu() {
        while (true) {
            User user = authService.getCurrentUser().orElse(null);
            if (user == null) {
                System.out.println("Session expirée");
                return;
            }

            afficherMenu();
            String choix = scanner.nextLine().trim();

            try {
                if (!traiterChoix(choix, user)) {
                    break;
                }
            } catch (Exception ex) {
                System.out.println("Erreur : " + ex.getMessage());
                attendreEntree();
            }
        }
    }
    private void afficherMenu() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      MON PROFIL                                ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1.    Voir mes informations                                   ║");
        System.out.println("║  2.    Changer mon mot de passe                                ║");
        System.out.println("║  0.    Retour                                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix, User user) {
        switch (choix) {
            case "1": afficherInformations(user); break;
            case "2": changerMotDePasse(); break;
            case "0": return false;
            default:
                System.out.println(" Choix invalide.");
                attendreEntree();
        }
        return true;
    }

    private void afficherInformations(User user) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  MES INFORMATIONS                              ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  ID          : %-48d║%n", user.getId());
        System.out.printf("║  Nom         : %-48s║%n", user.getNom());
        System.out.printf("║  Prénom      : %-48s║%n", user.getPrenom());
        System.out.printf("║  Email       : %-48s║%n", safe(user.getEmail()));
        System.out.printf("║  Login       : %-48s║%n", user.getLogin());
        System.out.printf("║  Rôle        : %-48s║%n", user.getRole().getLibelle());
        System.out.printf("║  Statut      : %-48s║%n", user.isActif() ? " Actif" : " Désactivé");

        if (user.getDerniereConnexion() != null) {
            System.out.printf("║  Dernière co.: %-48s║%n",
                    dtf.format(user.getDerniereConnexion().toLocalDateTime()));
        }

        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        attendreEntree();
    }

    private void changerMotDePasse(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                CHANGER MON MOT DE PASSE                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nAncien mot de passe : ");
        String oldPassword = scanner.nextLine();

        System.out.print("Nouveau mot de passe (min 6 caractères) : ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirmer le nouveau mot de passe : ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("\n Les mots de passe ne correspondent pas.");
            attendreEntree();
            return;
        }

        try {
            authService.changePassword(oldPassword, newPassword);
            System.out.println("\nMot de passe changé avec succès !");
        } catch (IllegalArgumentException ex) {
            System.out.println("\n " + ex.getMessage());
        }

        attendreEntree();
    }

    private String safe(Object o) {
        return o == null ? "-" : o.toString();
    }

    private void attendreEntree() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }


}
