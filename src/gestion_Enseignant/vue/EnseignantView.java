package gestion_Enseignant.vue;

import gestion_Enseignant.model.Enseignant;
import gestion_Enseignant.service.EnseignantService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class EnseignantView {
    private final EnseignantService service;
    private final Scanner scanner;
    private final DataSource ds;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EnseignantView(EnseignantService service, Scanner scanner, DataSource ds) {
        this.service = service;
        this.scanner = scanner;
        this.ds = ds;
    }

    public void menu() {
        while (true) {
            afficherMenu();
            String choix = scanner.nextLine().trim();

            try {
                if (!traiterChoix(choix)) {
                    break;
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Validation : " + ex.getMessage());
                attendreEntree();
            } catch (Exception ex) {
                System.out.println(" Erreur : " + ex.getMessage());
                attendreEntree();
            }
        }
    }
    private void afficherMenu() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                 GESTION DES ENSEIGNANTS                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1.    Ajouter un enseignant                                   ║");
        System.out.println("║  2.     Modifier un enseignant                                 ║");
        System.out.println("║  3.     Supprimer un enseignant                                ║");
        System.out.println("║  4.    Voir un enseignant                                      ║");
        System.out.println("║  5.     Lister tous les enseignants                            ║");
        System.out.println("║  6.     Activer/Désactiver un compte                           ║");
        System.out.println("║  7.     Réinitialiser le mot de passe                          ║");
        System.out.println("║  0.    Retour                                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix) {
        switch (choix) {
            case "1": ajouterEnseignant(); break;
            case "2": modifierEnseignant(); break;
            case "3": supprimerEnseignant(); break;
            case "4": voirEnseignant(); break;
            case "5": listerEnseignants(); break;
            case "6": toggleActif(); break;
            case "7": resetPassword(); break;
            case "0": return false;
            default:
                System.out.println("❌ Choix invalide.");
                attendreEntree();
        }
        return true;
    }

    private void ajouterEnseignant() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  AJOUTER UN ENSEIGNANT                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        Enseignant enseignant = new Enseignant();

        System.out.print("\nNom : ");
        enseignant.setNom(scanner.nextLine().trim());

        System.out.print("Prénom : ");
        enseignant.setPrenom(scanner.nextLine().trim());

        System.out.print("Email : ");
        enseignant.setEmail(scanner.nextLine().trim());

        System.out.print("Login : ");
        enseignant.setLogin(scanner.nextLine().trim());

        System.out.print("Mot de passe (min 6 caractères) : ");
        String password = scanner.nextLine();

        System.out.print("Confirmer le mot de passe : ");
        String confirmPassword = scanner.nextLine();

        if (!password.equals(confirmPassword)) {
            System.out.println("\n Les mots de passe ne correspondent pas.");
            attendreEntree();
            return;
        }

        Enseignant created = service.creerEnseignant(enseignant, password);
        System.out.println("\n Enseignant créé avec succès !");
        afficherEnseignant(created);

        attendreEntree();
    }

    private void modifierEnseignant() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                 MODIFIER UN ENSEIGNANT                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'enseignant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Enseignant> opt = service.getEnseignant(id);
            if (opt.isEmpty()) {
                System.out.println("❌ Enseignant introuvable.");
                attendreEntree();
                return;
            }

            Enseignant enseignant = opt.get();
            System.out.println("\n Enseignant actuel :");
            afficherEnseignant(enseignant);

            System.out.println("\n Laissez vide pour conserver la valeur actuelle");

            System.out.print("Nouveau nom (" + enseignant.getNom() + ") : ");
            String nom = scanner.nextLine().trim();
            if (!nom.isEmpty()) enseignant.setNom(nom);

            System.out.print("Nouveau prénom (" + enseignant.getPrenom() + ") : ");
            String prenom = scanner.nextLine().trim();
            if (!prenom.isEmpty()) enseignant.setPrenom(prenom);

            System.out.print("Nouvel email (" + enseignant.getEmail() + ") : ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty()) enseignant.setEmail(email);

            System.out.print("Nouveau login (" + enseignant.getLogin() + ") : ");
            String login = scanner.nextLine().trim();
            if (!login.isEmpty()) enseignant.setLogin(login);

            service.modifierEnseignant(enseignant);
            System.out.println("\n Enseignant modifié avec succès !");

            Optional<Enseignant> updated = service.getEnseignant(id);
            if (updated.isPresent()) {
                afficherEnseignant(updated.get());
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void supprimerEnseignant() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                SUPPRIMER UN ENSEIGNANT                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'enseignant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Connection c = ds.getConnection();

            Optional<Enseignant> opt = service.getEnseignant(id);
            if (opt.isEmpty()) {
                System.out.println(" Enseignant introuvable.");
                attendreEntree();
                return;
            }

            afficherEnseignant(opt.get());

            System.out.print("\n  Confirmer la suppression (o/N) : ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("o".equals(confirmation) || "oui".equals(confirmation)) {
                service.supprimerEnseignant(c,id);
                System.out.println(" Enseignant supprimé avec succès !");
            } else {
                System.out.println(" Suppression annulée.");
            }

        } catch (NumberFormatException | SQLException ex) {
            System.out.println(" ID invalide.");
        }
        attendreEntree();
    }

    private void voirEnseignant(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   VOIR UN ENSEIGNANT                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'enseignant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Enseignant> opt = service.getEnseignant(id);
            if (opt.isPresent()) {
                afficherEnseignant(opt.get());
            } else {
                System.out.println(" Enseignant introuvable.");
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }
    private void listerEnseignants(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                 LISTE DES ENSEIGNANTS                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Enseignant> enseignants = service.listerTousLesEnseignants();

        if (enseignants.isEmpty()) {
            System.out.println("\n Aucun enseignant enregistré.");
        } else {
            System.out.println("\n Total : " + enseignants.size() + " enseignant(s)\n");
            afficherTableauEnseignants(enseignants);
        }

        attendreEntree();
    }
    private void toggleActif() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              ACTIVER/DÉSACTIVER UN COMPTE                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'enseignant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Enseignant> opt = service.getEnseignant(id);
            if (opt.isEmpty()) {
                System.out.println(" Enseignant introuvable.");
                attendreEntree();
                return;
            }

            Enseignant e = opt.get();
            String action = e.isActif() ? "désactiver" : "activer";

            System.out.println("\nÉtat actuel : " + (e.isActif() ? " Actif" : " Désactivé"));
            System.out.print("Voulez-vous " + action + " ce compte ? (o/N) : ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("o".equals(confirmation) || "oui".equals(confirmation)) {
                service.toggleActif(id);
                System.out.println(" Compte " + (e.isActif() ? "désactivé" : "activé") + " avec succès !");
            } else {
                System.out.println(" Opération annulée.");
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void resetPassword(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              RÉINITIALISER LE MOT DE PASSE                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'enseignant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Enseignant> opt = service.getEnseignant(id);
            if (opt.isEmpty()) {
                System.out.println(" Enseignant introuvable.");
                attendreEntree();
                return;
            }

            Enseignant e = opt.get();
            System.out.println("\n Enseignant : " + e.getNomComplet());
            System.out.print("Confirmer la réinitialisation ? (o/N) : ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("o".equals(confirmation) || "oui".equals(confirmation)) {
                String newPassword = service.resetPassword(id);

                System.out.println("\n Mot de passe réinitialisé avec succès !");
                System.out.println("╔════════════════════════════════════════════════════════════════╗");
                System.out.println("║                 NOUVEAU MOT DE PASSE                           ║");
                System.out.println("╠════════════════════════════════════════════════════════════════╣");
                System.out.printf("║  %-60s  ║%n", newPassword);
                System.out.println("╠════════════════════════════════════════════════════════════════╣");
                System.out.println("║       Communiquez ce mot de passe à l'enseignant               ║");
                System.out.println("║   Il devra le changer lors de sa prochaine connexion.          ║");
                System.out.println("╚════════════════════════════════════════════════════════════════╝");
            } else {
                System.out.println(" Réinitialisation annulée.");
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void afficherEnseignant(Enseignant e) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    DÉTAIL ENSEIGNANT                           ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  ID          : %-48d║%n", e.getIdEnseignant());
        System.out.printf("║  Nom         : %-48s║%n", e.getNom());
        System.out.printf("║  Prénom      : %-48s║%n", e.getPrenom());
        System.out.printf("║  Email       : %-48s║%n", e.getEmail());
        System.out.printf("║  Login       : %-48s║%n", e.getLogin());
        System.out.printf("║  Matière     : %-48s║%n", safe(e.getMatiereNom()));
        System.out.printf("║  Statut      : %-48s║%n", e.isActif() ? "Actif" : "  Désactivé");

        if (e.getDerniereConnexion() != null) {
            System.out.printf("║  Dernière co.: %-48s║%n",
                    dtf.format(e.getDerniereConnexion().toLocalDateTime()));
        }

        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void afficherTableauEnseignants(List<Enseignant> enseignants) {
        System.out.println("╔══════╤═══════════════════════╤══════════════════════════╤══════════════════════╤════════╗");
        System.out.println("║  ID  │ Nom                   │ Email                    │ Matière              │ Statut ║");
        System.out.println("╠══════╪═══════════════════════╪══════════════════════════╪══════════════════════╪════════╣");

        for (Enseignant e : enseignants) {
            String statut = e.isActif() ? "✅" : "❌";
            System.out.printf("║ %4d │ %-21s │ %-24s │ %-20s │   %s   ║%n",
                    e.getIdEnseignant(),
                    truncate(e.getNomComplet(), 21),
                    truncate(e.getEmail(), 24),
                    truncate(safe(e.getMatiereNom()), 20),
                    statut);
        }

        System.out.println("╚══════╧═══════════════════════╧══════════════════════════╧══════════════════════╧════════╝");
    }
    private String safe(Object o) {
        return o == null ? "-" : o.toString();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) {
            return String.format("%-" + maxLength + "s", str);
        }
        return str.substring(0, maxLength - 2) + "..";
    }

    private void attendreEntree() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }


}
