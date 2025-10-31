package gestion_Matier.vue;

import gestion_Matier.model.Matiere;
import gestion_Matier.service.EnseignantInfo;
import gestion_Matier.service.MatiereService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class MatiereView {
    private final MatiereService service;
    private final Scanner scanner;

    public MatiereView(MatiereService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
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
                System.out.println("Erreur : " + ex.getMessage());
                attendreEntree();
            }
        }
    }

    private void afficherMenu() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    GESTION DES MATIÈRES                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Ajouter une matière                                        ║");
        System.out.println("║  2. Modifier une matière (changer l'enseignant)                ║");
        System.out.println("║  3. Supprimer une matière                                      ║");
        System.out.println("║  4. Voir une matière                                           ║");
        System.out.println("║  5. Lister toutes les matières                                 ║");
        System.out.println("║  6. Lister les matières d'un enseignant                        ║");
        System.out.println("║  7. Statistiques d'une matière                                 ║");
        System.out.println("║  0. Retour                                                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix) {
        switch (choix) {
            case "1": ajouterMatiere(); break;
            case "2": modifierMatiere(); break;
            case "3": supprimerMatiere(); break;
            case "4": voirMatiere(); break;
            case "5": listerMatieres(); break;
            case "6": listerMatieresEnseignant(); break;
            case "7": afficherStatistiques(); break;
            case "0": return false;
            default:
                System.out.println("Choix invalide.");
                attendreEntree();
        }
        return true;
    }

    private void ajouterMatiere() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    AJOUTER UNE MATIÈRE                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nNom de la matière : ");
        String nom = scanner.nextLine().trim();

        if (nom.isEmpty()) {
            System.out.println("❌ Le nom est requis.");
            attendreEntree();
            return;
        }
        System.out.println("\n Enseignants disponibles :");
        List<EnseignantInfo> enseignants = service.listerEnseignantsDisponibles();

        if (enseignants.isEmpty()) {
            System.out.println("Aucun enseignant disponible. Créez d'abord des enseignants.");
            attendreEntree();
            return;
        }

        afficherListeEnseignants(enseignants);

        System.out.print("\nID de l'enseignant : ");
        try {
            int idEnseignant = Integer.parseInt(scanner.nextLine().trim());

            Matiere matiere = new Matiere();
            matiere.setNom(nom);
            matiere.setIdEnseignant(idEnseignant);

            Matiere created = service.creerMatiere(matiere);
            System.out.println("\nMatière créée avec succès !");
            afficherMatiere(created);

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void modifierMatiere() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   MODIFIER UNE MATIÈRE                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nNom de la matière à modifier : ");
        String nom = scanner.nextLine().trim();

        if (nom.isEmpty()) {
            System.out.println(" Le nom est requis.");
            attendreEntree();
            return;
        }

        Optional<Matiere> optMatiere = service.getMatiere(nom);
        if (optMatiere.isEmpty()) {
            System.out.println(" Matière introuvable.");
            attendreEntree();
            return;
        }

        Matiere matiere = optMatiere.get();
        System.out.println("\n Matière actuelle :");
        afficherMatiere(matiere);

        System.out.println("\n Enseignants disponibles :");
        List<EnseignantInfo> enseignants = service.listerEnseignantsDisponibles();
        afficherListeEnseignants(enseignants);

        System.out.print("\nNouvel ID de l'enseignant (actuel: " + matiere.getIdEnseignant() + ") : ");
        try {
            int idEnseignant = Integer.parseInt(scanner.nextLine().trim());

            matiere.setIdEnseignant(idEnseignant);
            service.modifierMatiere(matiere);

            System.out.println("\n Matière modifiée avec succès !");
            Optional<Matiere> updated = service.getMatiere(nom);
            if (updated.isPresent()) {
                afficherMatiere(updated.get());
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }

        attendreEntree();
    }

    private void supprimerMatiere(){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   SUPPRIMER UNE MATIÈRE                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nNom de la matière à supprimer : ");
        String nom = scanner.nextLine().trim();

        if(nom.isEmpty()){
            System.out.println("le nom est requis");
            attendreEntree();
            return;
        }

        Optional<Matiere> optMatiere = service.getMatiere(nom);
        if (optMatiere.isEmpty()) {
            System.out.println(" Matière introuvable.");
            attendreEntree();
            return;
        }

        afficherMatiere(optMatiere.get());

        // Vérifier le nombre de notes
        int nbNotes = service.compterNotesMatiere(nom);
        if (nbNotes > 0) {
            System.out.println("\n ATTENTION : Cette matière contient " + nbNotes + " note(s).");
            System.out.println("La suppression est impossible tant que des notes y sont associées.");
            attendreEntree();
            return;
        }

        System.out.print("\n  Confirmer la suppression (o/N) : ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("o".equals(confirmation) || "oui".equals(confirmation)) {
            service.supprimerMatiere(nom);
            System.out.println(" Matière supprimée avec succès !");
        } else {
            System.out.println(" Suppression annulée.");
        }

        attendreEntree();
    }

    private void voirMatiere() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     VOIR UNE MATIÈRE                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.println("\n Nom de la matière: ");
        String nom = scanner.nextLine().trim();

        if (nom.isEmpty()) {
            System.out.println(" Le nom est requis.");
            attendreEntree();
            return;
        }

        Optional<Matiere> optMatiere = service.getMatiere(nom);
        if (optMatiere.isPresent()) {
            afficherMatiere(optMatiere.get());

            // Afficher les statistiques
            int nbNotes = service.compterNotesMatiere(nom);
            System.out.println("\n Statistiques :");
            System.out.println("   Nombre de notes : " + nbNotes);
        } else {
            System.out.println("Matière introuvable.");
        }

        attendreEntree();
    }

    private void listerMatieres() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   LISTE DES MATIÈRES                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Matiere> matieres = service.listerToutesLesMatieres();

        if (matieres.isEmpty()) {
            System.out.println("\n Aucune matière enregistrée.");
        } else {
            System.out.println("\n Total : " + matieres.size() + " matière(s)\n");
            afficherTableauMatieres(matieres);
        }

        attendreEntree();
    }
    private void listerMatieresEnseignant() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              MATIÈRES D'UN ENSEIGNANT                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<EnseignantInfo> enseignants = service.listerEnseignantsDisponibles();
        afficherListeEnseignants(enseignants);

        System.out.print("\nID de l'enseignant : ");
        try {
            int idEnseignant = Integer.parseInt(scanner.nextLine().trim());

            List<Matiere> matieres = service.listerMatieresParEnseignant(idEnseignant);

            if (matieres.isEmpty()) {
                System.out.println("\n Cet enseignant n'a pas de matière assignée.");
            } else {
                System.out.println("\n Matière(s) de " + matieres.get(0).getEnseignantNomComplet() + " :\n");
                afficherTableauMatieres(matieres);
            }

        } catch (NumberFormatException ex) {
            System.out.println("ID invalide.");
        }

        attendreEntree();
    }
    private void afficherStatistiques() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              STATISTIQUES D'UNE MATIÈRE                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\nNom de la matière : ");
        String nom = scanner.nextLine().trim();

        if (nom.isEmpty()) {
            System.out.println("Le nom est requis.");
            attendreEntree();
            return;
        }

        Optional<Matiere> optMatiere = service.getMatiere(nom);
        if (optMatiere.isEmpty()) {
            System.out.println("Matière introuvable.");
            attendreEntree();
            return;
        }

        Matiere matiere = optMatiere.get();
        int nbNotes = service.compterNotesMatiere(nom);

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      STATISTIQUES                              ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Matière     : %-49s║%n", matiere.getNom());
        System.out.printf("║ Enseignant  : %-49s║%n", matiere.getEnseignantNomComplet());
        System.out.printf("║ Email       : %-49s║%n", safe(matiere.getEnseignantEmail()));
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Nombre de notes enregistrées : %-32d║%n", nbNotes);
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        attendreEntree();
    }
    private void afficherMatiere(Matiere m) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      DÉTAIL MATIÈRE                            ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Nom         : %-49s║%n", m.getNom());
        System.out.printf("║ Enseignant  : %-49s║%n", m.getEnseignantNomComplet());
        System.out.printf("║ Email       : %-49s║%n", safe(m.getEnseignantEmail()));
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void afficherTableauMatieres(List<Matiere> matieres) {
        System.out.println("╔══════════════════════════╤════════════════════════════╤══════════════════════════╗");
        System.out.println("║ Matière                  │ Enseignant                 │ Email                    ║");
        System.out.println("╠══════════════════════════╪════════════════════════════╪══════════════════════════╣");

        for (Matiere m : matieres) {
            int nbNotes = service.compterNotesMatiere(m.getNom());
            System.out.printf("║ %-24s │ %-26s │ %-24s ║%n",
                    truncate(m.getNom(), 24),
                    truncate(m.getEnseignantNomComplet(), 26),
                    truncate(safe(m.getEnseignantEmail()), 24));
            System.out.printf("║   └─ %d note(s)%-10s│%-28s│%-26s║%n", nbNotes, "", "", "");
        }

        System.out.println("╚══════════════════════════╧════════════════════════════╧══════════════════════════╝");
    }

    private void afficherListeEnseignants(List<EnseignantInfo> enseignants) {
        System.out.println("╔══════╤══════════════════════════╤══════════════════════════╗");
        System.out.println("║  ID  │ Nom                      │ Statut                   ║");
        System.out.println("╠══════╪══════════════════════════╪══════════════════════════╣");

        for (EnseignantInfo e : enseignants) {
            String statut = e.isAssigneAMatiere() ? "✓ Assigné" : "○ Disponible";
            System.out.printf("║ %4d │ %-24s │ %-24s ║%n",
                    e.getId(),
                    truncate(e.getNomComplet(), 24),
                    statut);
        }

        System.out.println("╚══════╧══════════════════════════╧══════════════════════════╝");
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
