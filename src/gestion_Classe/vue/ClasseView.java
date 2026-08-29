package gestion_Classe.vue;

import gestion_Classe.model.Classe;
import gestion_Classe.service.ClasseService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class ClasseView {
    private final ClasseService service;
    private final Scanner scanner;
    private final DataSource ds;

    public ClasseView(ClasseService service, Scanner scanner, DataSource ds) {
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
        System.out.println("║                   GESTION DES CLASSES                          ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1.  Ajouter une classe                                        ║");
        System.out.println("║  2.  Modifier une classe                                       ║");
        System.out.println("║  3. ️ Supprimer une classe                                       ║");
        System.out.println("║  4.  Voir une classe                                           ║");
        System.out.println("║  5.  Lister toutes les classes                                 ║");
        System.out.println("║  6.  Rafraîchir le nombre d'élèves                             ║");
        System.out.println("║  0. ️ Retour                                                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix) {
        switch (choix) {
            case "1": ajouterClasse(); break;
            case "2": modifierClasse(); break;
            case "3": supprimerClasse(); break;
            case "4": voirClasse(); break;
            case "5": listerClasses(); break;
            case "6": rafraichirNombreEleves(); break;
            case "0":
                System.out.println("Retour au menu précédent.");
                try {
                    Thread.sleep(500);
                }catch (InterruptedException e){

                }
                return false;
            default:
                System.out.println("Choix invalide. Veuillez réessayer.");
                attendreEntree();
                }
        return true;
    }

    private void ajouterClasse() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    AJOUTER UNE CLASSE                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        Classe classe = new Classe();

        System.out.print("\nNiveau de la classe (ex: Terminale S, 6ème A) : ");
        classe.setNiveau(scanner.nextLine().trim());

        if (classe.getNiveau().isEmpty()) {
            System.out.println(" Le niveau est requis.");
            attendreEntree();
            return;
        }

        Classe created = service.creerClasse(classe);
        System.out.println("\n Classe créée avec succès !");
        afficherClasse(created);

        attendreEntree();
    }

    private void modifierClasse(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   MODIFIER UNE CLASSE                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de la classe : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Classe> opt = service.getClasse(id);
            if (opt.isEmpty()) {
                System.out.println(" Classe introuvable.");
                attendreEntree();
                return;
            }

            Classe classe = opt.get();
            System.out.println("\n Classe actuelle :");
            afficherClasse(classe);

            System.out.println("\n Laissez vide pour conserver la valeur actuelle");

            System.out.print("Nouveau niveau (" + classe.getNiveau() + ") : ");
            String niveau = scanner.nextLine().trim();
            if (!niveau.isEmpty()) {
                classe.setNiveau(niveau);
            }

            service.modifierClasse(classe);
            System.out.println("\n Classe modifiée avec succès !");

            Optional<Classe> updated = service.getClasse(id);
            if (updated.isPresent()) {
                afficherClasse(updated.get());
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }
    private void supprimerClasse() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   SUPPRIMER UNE CLASSE                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de la classe à supprimer : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Connection c = ds.getConnection();

            Optional<Classe> opt = service.getClasse(id);
            if (opt.isEmpty()) {
                System.out.println(" Classe introuvable.");
                attendreEntree();
                return;
            }

            afficherClasse(opt.get());

            int nbEtudiants = service.compterEtudiants(id);
            if (nbEtudiants > 0) {
                System.out.println("\n⚠️  ATTENTION : Cette classe contient " + nbEtudiants + " étudiant(s).");
                System.out.println("La suppression est impossible. Réassignez d'abord les étudiants.");
                attendreEntree();
                return;
            }

            System.out.print("\n⚠️  Confirmer la suppression (o/N) : ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("o".equals(confirmation) || "oui".equals(confirmation)) {
                service.supprimerClasse(c,id);
                System.out.println("✅ Classe supprimée avec succès !");
            } else {
                System.out.println("❌ Suppression annulée.");
            }

        } catch (NumberFormatException | SQLException ex) {
            System.out.println("❌ ID invalide.");
        }

        attendreEntree();
    }

    private void voirClasse(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     VOIR UNE CLASSE                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de la classe : ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Classe> opt = service.getClasse(id);
            if (opt.isPresent()) {
                afficherClasse(opt.get());

                int nbEtudiants = service.compterEtudiants(id);
                System.out.println("\n Statistiques :");
                System.out.println("   Nombre réel d'étudiants : " + nbEtudiants);
            } else {
                System.out.println(" Classe introuvable.");
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void listerClasses(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   LISTE DES CLASSES                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Classe> classes = service.listerToutesLesClasses();

        if (classes.isEmpty()) {
            System.out.println("\n Aucune classe enregistrée.");
        } else {
            System.out.println("\n Total : " + classes.size() + " classe(s)\n");
            afficherTableauClasses(classes);
        }

        attendreEntree();
    }

    private void rafraichirNombreEleves(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              RAFRAÎCHIR LE NOMBRE D'ÉLÈVES                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nID de la classe (ou vide pour toutes) : ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            List<Classe> classes = service.listerToutesLesClasses();
            for (Classe c : classes) {
                service.rafraichirNombreEleves(c.getIdClasse());
            }
            System.out.println(" Toutes les classes ont été rafraîchies !");
        } else {
            try {
                int id = Integer.parseInt(input);
                service.rafraichirNombreEleves(id);
                System.out.println(" Classe rafraîchie avec succès !");

                Optional<Classe> opt = service.getClasse(id);
                if (opt.isPresent()) {
                    afficherClasse(opt.get());
                }
            } catch (NumberFormatException ex) {
                System.out.println(" ID invalide.");
            }
        }

        attendreEntree();

    }

    private void afficherClasse(Classe c) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      DÉTAIL CLASSE                             ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf(" ║  ID              : %-44d║%n", c.getIdClasse());
        System.out.printf(" ║  Niveau          : %-44s║%n", c.getNiveau());
        System.out.printf(" ║  Nombre d'élèves : %-44d║%n", c.getNombreEleves());
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void afficherTableauClasses(List<Classe> classes) {
        System.out.println("╔══════╤════════════════════════════════╤════════════════╗");
        System.out.println("║  ID  │ Niveau                         │ Nb élèves      ║");
        System.out.println("╠══════╪════════════════════════════════╪════════════════╣");

        for (Classe c : classes) {
            int nbReel = service.compterEtudiants(c.getIdClasse());
            String affichage = c.getNombreEleves() + " (" + nbReel + " réel)";

            System.out.printf("║ %4d │ %-30s │ %-14s ║%n",
                    c.getIdClasse(),
                    truncate(c.getNiveau(), 30),
                    affichage);
        }

        System.out.println("╚══════╧════════════════════════════════╧════════════════╝");
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
