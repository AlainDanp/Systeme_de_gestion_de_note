package gestion_Note.vue;

import gestion_Note.model.Note;
import gestion_Note.service.NoteService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class NoteView {

    private final NoteService service;
    private final Scanner scanner;
    private final DataSource ds;
    private final DecimalFormat df = new DecimalFormat("#0.00");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public NoteView(NoteService service, Scanner scanner, DataSource ds) {
        this.service = service;
        this.scanner = scanner;
        this.ds = ds;
    }

    public void menu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║                    GESTION DES NOTES                           ║");
            System.out.println("╠════════════════════════════════════════════════════════════════╣");
            System.out.println("║  1. Ajouter une note                                           ║");
            System.out.println("║  2. Modifier une note                                          ║");
            System.out.println("║  3. Supprimer une note                                         ║");
            System.out.println("║  4. Voir une note par ID                                       ║");
            System.out.println("║  5. Lister les notes d'un étudiant                             ║");
            System.out.println("║  6. Lister les notes d'un étudiant pour une période            ║");
            System.out.println("║  7. Lister toutes les notes d'une période                      ║");
            System.out.println("║  8. Lister toutes les notes d'une matière                      ║");
            System.out.println("║  9. Calculer la moyenne d'un étudiant                          ║");
            System.out.println("║  10. Lister toutes les notes                                   ║");
            System.out.println("║  0. Retour                                                     ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.print("Choix > ");

            String choix = scanner.nextLine().trim();

            try {
                switch (choix) {
                    case "1": ajouterNote(); break;
                    case "2": modifierNote(); break;
                    case "3": supprimerNote(); break;
                    case "4": voirNoteParId(); break;
                    case "5": listerNotesEtudiant(); break;
                    case "6": listerNotesEtudiantPeriode(); break;
                    case "7": listerNotesPeriode(); break;
                    case "8": listerNotesMatiere(); break;
                    case "9": calculerMoyenne(); break;
                    case "10": listerToutesLesNotes(); break;
                    case "0": return;
                    default: System.out.println("Choix invalide."); break;
                }
            } catch (IllegalArgumentException ex) {
                System.out.println(" Validation : " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println(" Erreur : " + ex.getMessage());
            }
        }
    }

    private void ajouterNote(){
        System.out.println("\n=== Ajouter une note ===");

        try {
            System.out.print("ID étudiant : ");
            int etudiantId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Période (ex: 2025-T1) : ");
            String periode = scanner.nextLine().trim();


            System.out.println("\nMatières disponibles :");
            List<String> matieres = service.listerMatieres();
            for (int i = 0; i < matieres.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + matieres.get(i));
            }

            System.out.print("Matière : ");
            String matiere = scanner.nextLine().trim();

            System.out.print("Note (0-20) : ");
            double valeur = Double.parseDouble(scanner.nextLine().trim());
            Note note = new Note();
            note.setEtudiantId(etudiantId);
            note.setPeriode(periode);
            note.setMatiere(matiere);
            note.setValeur(valeur);

            Note created = service.creeNote(note);
            System.out.println("Note créée avec succès !");
            afficherNote(created);
        }catch (NumberFormatException ex){
            System.out.println("Valeur numérique invalide.");
        }
    }

    private void modifierNote() {
        System.out.println("\n=== Modifier une note ===");

        try {
            System.out.print("ID de la note à modifier : ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Note> optNote = service.getNote(id);
            if (optNote.isEmpty()) {
                System.out.println(" Note introuvable avec l'ID " + id);
                return;
            }

            Note note = optNote.get();
            System.out.println("\nNote actuelle :");
            afficherNote(note);

            System.out.println("\n Laissez vide pour conserver la valeur actuelle");

            System.out.print("Nouvelle période (actuelle: " + note.getPeriode() + ") : ");
            String periode = scanner.nextLine().trim();
            if (!periode.isEmpty()) note.setPeriode(periode);

            System.out.print("Nouvelle matière (actuelle: " + note.getMatiere() + ") : ");
            String matiere = scanner.nextLine().trim();
            if (!matiere.isEmpty()) note.setMatiere(matiere);

            System.out.print("Nouvelle note (actuelle: " + note.getValeur() + ") : ");
            String valeurStr = scanner.nextLine().trim();
            if (!valeurStr.isEmpty()) {
                double valeur = Double.parseDouble(valeurStr);
                note.setValeur(valeur);
            }

            service.modifierNote(note);
            System.out.println(" Note modifiée avec succès !");

            Optional<Note> updated = service.getNote(id);
            if (updated.isPresent()) {
                afficherNote(updated.get());
            }

        } catch (NumberFormatException ex) {
            System.out.println(" Valeur numérique invalide.");
        }
    }

    private void supprimerNote() {
        System.out.println("\n=== Supprimer une note ===");

        try {
            System.out.print("ID de la note à supprimer : ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            Connection c = ds.getConnection();

            Optional<Note> optNote = service.getNote(id);
            if (optNote.isEmpty()) {
                System.out.println(" Note introuvable avec l'ID " + id);
                return;
            }

            afficherNote(optNote.get());

            System.out.print("\n  Confirmer la suppression (o/N) : ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("o".equals(confirmation) || "oui".equals(confirmation)) {
                service.supprimerNote(c,id);
                System.out.println(" Note supprimée avec succès !");
            } else {
                System.out.println(" Suppression annulée.");
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void voirNoteParId() {
        System.out.println("\n=== Voir une note ===");

        try {
            System.out.print("ID de la note : ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Note> optNote = service.getNote(id);
            if (optNote.isPresent()) {
                afficherNote(optNote.get());
            } else {
                System.out.println(" Aucune note trouvée avec l'ID " + id);
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }
    }

    private void listerNotesEtudiant() {
        System.out.println("\n=== Lister les notes d'un étudiant ===");

        try {
            System.out.print("ID étudiant : ");
            int etudiantId = Integer.parseInt(scanner.nextLine().trim());

            List<Note> notes = service.listerNotesParEtudiant(etudiantId);

            if (notes.isEmpty()) {
                System.out.println(" Aucune note trouvée pour l'étudiant #" + etudiantId);
            } else {
                System.out.println("\nNotes de " + notes.get(0).getEtudiantNomComplet() + " :");
                afficherTableauNotes(notes);
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }
    }

    private void listerNotesEtudiantPeriode() {
        System.out.println("\n=== Lister les notes d'un étudiant pour une période ===");

        try {
            System.out.print("ID étudiant : ");
            int etudiantId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Période (ex: 2025-T1) : ");
            String periode = scanner.nextLine().trim();

            List<Note> notes = service.listerNotesParEtudiantEtPeriode(etudiantId, periode);

            if (notes.isEmpty()) {
                System.out.println(" Aucune note trouvée pour l'étudiant #" + etudiantId + " en " + periode);
            } else {
                System.out.println("\n Notes de " + notes.get(0).getEtudiantNomComplet() + " - " + periode + " :");
                afficherTableauNotes(notes);


                Double moyenne = service.calculerMoyenneEtudiant(etudiantId, periode);
                if (moyenne != null) {
                    System.out.println("\n Moyenne : " + df.format(moyenne) + "/20");
                }
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }
    }

    private void listerNotesPeriode() {
        System.out.println("\n=== Lister toutes les notes d'une période ===");

        System.out.print("Période (ex: 2025-T1) : ");
        String periode = scanner.nextLine().trim();

        List<Note> notes = service.listerNotesParPeriode(periode);

        if (notes.isEmpty()) {
            System.out.println(" Aucune note trouvée pour la période " + periode);
        } else {
            System.out.println("\n Notes de la période " + periode + " (" + notes.size() + " notes) :");
            afficherTableauNotes(notes);
        }
    }

    private void listerNotesMatiere() {
        System.out.println("\n=== Lister toutes les notes d'une matière ===");

        System.out.println("\nMatières disponibles :");
        List<String> matieres = service.listerMatieres();
        for (int i = 0; i < matieres.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + matieres.get(i));
        }

        System.out.print("\nMatière : ");
        String matiere = scanner.nextLine().trim();

        List<Note> notes = service.listerNotesParMatiere(matiere);

        if (notes.isEmpty()) {
            System.out.println("Aucune note trouvée pour la matière " + matiere);
        } else {
            System.out.println("\nNotes en " + matiere + " (" + notes.size() + " notes) :");
            if (!notes.isEmpty() && notes.get(0).getEnseignantNomComplet() != null) {
                System.out.println("Enseignant : " + notes.get(0).getEnseignantNomComplet());
            }
            afficherTableauNotes(notes);
        }
    }

    private void calculerMoyenne() {
        System.out.println("\n=== Calculer la moyenne d'un étudiant ===");

        try {
            System.out.print("ID étudiant : ");
            int etudiantId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Période (ex: 2025-T1) : ");
            String periode = scanner.nextLine().trim();

            Double moyenne = service.calculerMoyenneEtudiant(etudiantId, periode);

            if (moyenne == null) {
                System.out.println("Aucune note trouvée pour calculer la moyenne.");
            } else {
                List<Note> notes = service.listerNotesParEtudiantEtPeriode(etudiantId, periode);
                if (!notes.isEmpty()) {
                    System.out.println("\n Étudiant : " + notes.get(0).getEtudiantNomComplet());
                }
                System.out.println("Période : " + periode);
                System.out.println("Moyenne : " + df.format(moyenne) + "/20");

                if (moyenne >= 16) {
                    System.out.println(" Excellent !");
                } else if (moyenne >= 14) {
                    System.out.println(" Très bien !");
                } else if (moyenne >= 12) {
                    System.out.println(" Bien");
                } else if (moyenne >= 10) {
                    System.out.println(" Assez bien");
                } else {
                    System.out.println(" Peut mieux faire sinon tu vas échouer ");
                }
            }

        } catch (NumberFormatException ex) {
            System.out.println(" ID invalide.");
        }
    }

    private void listerToutesLesNotes() {
        System.out.println("\n=== Toutes les notes ===");

        List<Note> notes = service.listerToutesLesNotes();

        if (notes.isEmpty()) {
            System.out.println(" Aucune note enregistrée.");
        } else {
            System.out.println("\n Total : " + notes.size() + " notes");
            afficherTableauNotes(notes);
        }
    }

    private void afficherNote(Note n) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         DÉTAIL NOTE                            ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ ID          : %-49s║%n", safe(n.getId()));
        System.out.printf("║ Étudiant    : %-49s║%n", n.getEtudiantNomComplet());
        System.out.printf("║ Période     : %-49s║%n", safe(n.getPeriode()));
        System.out.printf("║ Matière     : %-49s║%n", safe(n.getMatiere()));
        System.out.printf("║ Enseignant  : %-49s║%n", n.getEnseignantNomComplet());
        System.out.printf("║ Note        : %-49s║%n", formatNote(n.getValeur()));
        if (n.getCreatedAt() != null) {
            System.out.printf("║ Créée le    : %-49s║%n", dtf.format(n.getCreatedAt().toLocalDateTime()));
        }
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void afficherTableauNotes(List<Note> notes) {
        System.out.println("\n╔════╤═══════════════════════╤══════════╤══════════════════════╤════════╗");
        System.out.println("║ ID │ Étudiant              │ Période  │ Matière              │ Note   ║");
        System.out.println("╠════╪═══════════════════════╪══════════╪══════════════════════╪════════╣");

        for (Note n : notes) {
            System.out.printf("║%-4s│ %-21s │ %-8s │ %-20s │ %-6s ║%n",
                    truncate(safe(n.getId()), 4),
                    truncate(n.getEtudiantNomComplet(), 21),
                    truncate(safe(n.getPeriode()), 8),
                    truncate(safe(n.getMatiere()), 20),
                    formatNote(n.getValeur()));
        }

        System.out.println("╚════╧═══════════════════════╧══════════╧══════════════════════╧════════╝");
    }

    private String formatNote(Double valeur) {
        if (valeur == null) return "N/A";
        return df.format(valeur) + "/20";
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


}
