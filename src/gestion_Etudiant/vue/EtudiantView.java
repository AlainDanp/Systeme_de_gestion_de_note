package gestion_Etudiant.vue;


import gestion_Etudiant.service.EtudiantService;
import gestion_Etudiant.model.Etudiant;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class EtudiantView {

    private final EtudiantService service;
    private final Scanner scanner;
    private final DataSource ds;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EtudiantView(EtudiantService service, Scanner scanner, DataSource ds) {
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
                System.out.println(" Validation : " + ex.getMessage());
                attendreEntree();
            } catch (Exception ex) {
                System.out.println(" Erreur : " + ex.getMessage());
                attendreEntree();
            }
        }
    }

    private void afficherMenu() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  GESTION DES ÉTUDIANTS                         ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1.    Ajouter un étudiant                                     ║");
        System.out.println("║  2.     Modifier un étudiant                                   ║");
        System.out.println("║  3.     Supprimer un étudiant                                  ║");
        System.out.println("║  4.    Voir un étudiant                                        ║");
        System.out.println("║  5.     Lister tous les étudiants                              ║");
        System.out.println("║  6.     Lister les étudiants d'une classe                      ║");
        System.out.println("║  7.     Activer/Désactiver un compte                           ║");
        System.out.println("║  8.     Réinitialiser le mot de passe                          ║");
        System.out.println("║  0.    Retour                                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix) {
        switch (choix) {
            case "1": ajouterEtudiant(); break;
            case "2": modifierEtudiant(); break;
            case "3": supprimerEtudiant(); break;
            case "4": voirEtudiant(); break;
            case "5": listerEtudiants(); break;
            case "6": listerEtudiantsParClasse(); break;
            case "7": toggleActif(); break;
            case "8": resetPassword(); break;
            case "0": return false;
            default:
                System.out.println(" Choix invalide.");
                attendreEntree();
        }
        return true;
    }

    private void ajouterEtudiant(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   AJOUTER UN ÉTUDIANT                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        Etudiant etudiant = new Etudiant();

        System.out.print("\nNom : ");
        etudiant.setNom(scanner.nextLine().trim());

        System.out.print("Prénom : ");
        etudiant.setPrenom(scanner.nextLine().trim());

        System.out.print("Email (optionnel) : ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) {
            etudiant.setEmail(email);
        }

        System.out.print("Login : ");
        etudiant.setLogin(scanner.nextLine().trim());

        System.out.print("Mot de passe (min 6 caractères) : ");
        String password = scanner.nextLine();

        System.out.print("Confirmer le mot de passe : ");
        String confirmPassword = scanner.nextLine();

        if (!password.equals(confirmPassword)) {
            System.out.println("\n Les mots de passe ne correspondent pas.");
            attendreEntree();
            return;
        }

        System.out.print("ID de la classe (optionnel) : ");
        String classeIdStr = scanner.nextLine().trim();
        if (!classeIdStr.isEmpty()) {
            try {
                etudiant.setClasseId(Integer.parseInt(classeIdStr));
            } catch (NumberFormatException ex) {
                System.out.println("  ID de classe invalide, ignoré.");
            }
        }

        Etudiant created = service.creerEtudiant(etudiant, password);
        System.out.println("\n  Étudiant créé avec succès !");
        afficherEtudiant(created);

        attendreEntree();
    }

    private void modifierEtudiant(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  MODIFIER UN ÉTUDIANT                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'étudiant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Etudiant> opt = service.getEtudiant(id);
            if (opt.isEmpty()) {
                System.out.println(" Étudiant introuvable.");
                attendreEntree();
                return;
            }

            Etudiant etudiant = opt.get();
            System.out.println("\n Étudiant actuel :");
            afficherEtudiant(etudiant);

            System.out.println("\n Laissez vide pour conserver la valeur actuelle");

            System.out.print("Nouveau nom (" + etudiant.getNom() + ") : ");
            String nom = scanner.nextLine().trim();
            if (!nom.isEmpty()) etudiant.setNom(nom);

            System.out.print("Nouveau prénom (" + etudiant.getPrenom() + ") : ");
            String prenom = scanner.nextLine().trim();
            if (!prenom.isEmpty()) etudiant.setPrenom(prenom);

            System.out.print("Nouvel email (" + safe(etudiant.getEmail()) + ") : ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty()) etudiant.setEmail(email);

            System.out.print("Nouveau login (" + etudiant.getLogin() + ") : ");
            String login = scanner.nextLine().trim();
            if (!login.isEmpty()) etudiant.setLogin(login);

            System.out.print("Nouvel ID de classe (" + safe(etudiant.getClasseId()) + ") : ");
            String classeIdStr = scanner.nextLine().trim();
            if (!classeIdStr.isEmpty()) {
                try {
                    etudiant.setClasseId(Integer.parseInt(classeIdStr));
                } catch (NumberFormatException ex) {
                    System.out.println("  ID de classe invalide, ignoré.");
                }
            }

            service.modifierEtudiant(etudiant);
            System.out.println("\n Étudiant modifié avec succès !");

            Optional<Etudiant> updated = service.getEtudiant(id);
            if (updated.isPresent()) {
                afficherEtudiant(updated.get());
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void supprimerEtudiant(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                 SUPPRIMER UN ÉTUDIANT                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'étudiant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Connection c = ds.getConnection();

            Optional<Etudiant> opt = service.getEtudiant(id);
            if (opt.isEmpty()) {
                System.out.println(" Étudiant introuvable.");
                attendreEntree();
                return;
            }
            afficherEtudiant(opt.get());

            System.out.print("\n Confirmer la suppression (o/N) : ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("o".equals(confirmation) || "oui".equals(confirmation)) {
                service.supprimerEtudiant(c,id);
                System.out.println(" Étudiant supprimé avec succès !");
            } else {
                System.out.println(" Suppression annulée.");
            }

        } catch (NumberFormatException | SQLException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void voirEtudiant(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    VOIR UN ÉTUDIANT                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'étudiant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Etudiant> opt = service.getEtudiant(id);
            if (opt.isPresent()) {
                afficherEtudiant(opt.get());
            } else {
                System.out.println(" Étudiant introuvable.");
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void listerEtudiants(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  LISTE DES ÉTUDIANTS                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Etudiant> etudiants = service.listerTousLesEtudiants();

        if (etudiants.isEmpty()) {
            System.out.println("\n Aucun étudiant enregistré.");
        } else {
            System.out.println("\n Total : " + etudiants.size() + " étudiant(s)\n");
            afficherTableauEtudiants(etudiants);
        }

        attendreEntree();
    }

    private void listerEtudiantsParClasse(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              ÉTUDIANTS D'UNE CLASSE                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de la classe : ");
        try {
            int classeId = Integer.parseInt(scanner.nextLine().trim());

            List<Etudiant> etudiants = service.listerEtudiantsParClasse(classeId);

            if (etudiants.isEmpty()) {
                System.out.println("\n Aucun étudiant dans cette classe.");
            } else {
                System.out.println("\n Classe ID " + classeId + " : " + etudiants.size() + " étudiant(s)\n");
                afficherTableauEtudiants(etudiants);
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void toggleActif(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              ACTIVER/DÉSACTIVER UN COMPTE                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de l'étudiant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Etudiant> opt = service.getEtudiant(id);
            if (opt.isEmpty()) {
                System.out.println(" Étudiant introuvable.");
                attendreEntree();
                return;
            }

            Etudiant e = opt.get();
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

        System.out.print("\nID de l'étudiant : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Etudiant> opt = service.getEtudiant(id);
            if (opt.isEmpty()) {
                System.out.println(" Étudiant introuvable.");
                attendreEntree();
                return;
            }

            Etudiant e = opt.get();
            System.out.println("\n Étudiant : " + e.getNomComplet());
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
                System.out.println("║     ️   Communiquez ce mot de passe à l'étudiant                 ║");
                System.out.println("║  Il  devra le changer lors de sa prochaine connexion.          ║");
                System.out.println("╚════════════════════════════════════════════════════════════════╝");
            } else {
                System.out.println(" Réinitialisation annulée.");
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void afficherEtudiant(Etudiant e){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     DÉTAIL ÉTUDIANT                            ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  ID          : %-48d║%n", e.getIdEtudiant());
        System.out.printf("║  Nom         : %-48s║%n", e.getNom());
        System.out.printf("║  Prénom      : %-48s║%n", e.getPrenom());
        System.out.printf("║  Email       : %-48s║%n", safe(e.getEmail()));
        System.out.printf("║  Login       : %-48s║%n", e.getLogin());
        System.out.printf("║  Classe      : %-48s║%n", safe(e.getClasseNiveau()));
        System.out.printf("║  Statut      : %-48s║%n", e.isActif() ? " Actif" : " Désactivé");

        if (e.getDerniereConnexion() != null) {
            System.out.printf("║  Dernière co.: %-48s║%n",
                    dtf.format(e.getDerniereConnexion().toLocalDateTime()));
        }

        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void afficherTableauEtudiants(List<Etudiant> etudiants){
        System.out.println("╔══════╤═══════════════════════╤══════════════════════╤══════════════════════╤════════╗");
        System.out.println("║  ID  │ Nom                   │ Login                │ Classe               │ Statut ║");
        System.out.println("╠══════╪═══════════════════════╪══════════════════════╪══════════════════════╪════════╣");

        for (Etudiant e : etudiants) {
            String statut = e.isActif() ? "✅" : "❌";
            System.out.printf("║ %4d │ %-21s │ %-20s │ %-20s │   %s   ║%n",
                    e.getIdEtudiant(),
                    truncate(e.getNomComplet(), 21),
                    truncate(e.getLogin(), 20),
                    truncate(safe(e.getClasseNiveau()), 20),
                    statut);
        }

        System.out.println("╚══════╧═══════════════════════╧══════════════════════╧══════════════════════╧════════╝");
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
