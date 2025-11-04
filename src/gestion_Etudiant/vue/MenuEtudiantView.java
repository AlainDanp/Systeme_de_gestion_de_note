package gestion_Etudiant.vue;

import Auhentifcation.AuthenticationService;
import Auhentifcation.ProfilView;
import gestion_Bulletin.model.Bulletin;
import gestion_Bulletin.service.BulletinService;
import gestion_Etudiant.model.Etudiant;
import gestion_Note.model.Note;
import gestion_Note.service.NoteService;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class MenuEtudiantView {
    private final AuthenticationService authService;
    private final NoteService noteService;
    private final BulletinService bulletinService;
    private final ProfilView profilView;
    private final Scanner scanner;
    private final DecimalFormat df = new DecimalFormat("#0.00");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MenuEtudiantView(
            AuthenticationService authService,
            NoteService noteService,
            BulletinService bulletinService,
            ProfilView profilView,
            Scanner scanner) {
        this.authService = authService;
        this.noteService = noteService;
        this.bulletinService = bulletinService;
        this.profilView = profilView;
        this.scanner = scanner;
    }
    public void afficher() {
        Etudiant etudiant = (Etudiant) authService.getCurrentUser().orElse(null);
        if (etudiant == null) {
            System.out.println(" Session expirée");
            return;
        }

        while (true) {
            afficherMenu(etudiant);
            String choix = scanner.nextLine().trim();

            if (!traiterChoix(choix, etudiant)) {
                break;
            }
        }
    }

    private void afficherMenu(Etudiant etudiant){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ESPACE ÉTUDIANT                             ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║    Connecté : %-48s ║%n", etudiant.getNomComplet());

        if (etudiant.getClasseNiveau() != null) {
            System.out.printf("║    Classe   : %-48s ║%n", etudiant.getClasseNiveau());
        }

        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1.    Voir mes notes                                          ║");
        System.out.println("║  2.    Voir mes bulletins                                      ║");
        System.out.println("║  3.    Mes statistiques                                        ║");
        System.out.println("║  4.    Mon Profil                                              ║");
        System.out.println("║  0.    Déconnexion                                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    private boolean traiterChoix(String choix, Etudiant etudiant){
        switch (choix) {
            case "1":
                voirMesNotes(etudiant);
                break;
            case "2":
                voirMesBulletins(etudiant);
                break;
            case "3":
                voirMesStatistiques(etudiant);
                break;
            case "4":
                profilView.menu();
                break;
            case "0":
                if (confirmerDeconnexion()) {
                    authService.logout();
                    return false;
                }
                break;
            default:
                System.out.println("Choix invalide.");
                attendreEntree();
        }
        return true;
    }

    private void voirMesNotes(Etudiant etudiant){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      MES NOTES                                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Note> notes = noteService.listerNotesParEtudiant(etudiant.getIdEtudiant());

        if (notes.isEmpty()) {
            System.out.println("\n Aucune note enregistrée.");
        } else {
            System.out.println("\n Total : " + notes.size() + " note(s)\n");
            afficherTableauNotes(notes);
        }

        attendreEntree();

    }

    private void voirMesBulletins(Etudiant etudiant){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    MES BULLETINS                               ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Bulletin> bulletins = bulletinService.listerParEtudiant(etudiant.getIdEtudiant());

        if (bulletins.isEmpty()) {
            System.out.println("\n Aucun bulletin disponible.");
        } else {
            System.out.println("\n Total : " + bulletins.size() + " bulletin(s)\n");

            for (Bulletin b : bulletins) {
                afficherBulletin(b);
                System.out.println();
            }
        }
        attendreEntree();
    }

    private void voirMesStatistiques(Etudiant etudiant){
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   MES STATISTIQUES                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Note> notes = noteService.listerNotesParEtudiant(etudiant.getIdEtudiant());

        if (notes.isEmpty()) {
            System.out.println("\n Aucune donnée disponible.");
            attendreEntree();
            return;
        }

        double somme = 0;
        double min = 20;
        double max = 0;

        for (Note n : notes) {
            somme += n.getValeur();
            if (n.getValeur() < min) min = n.getValeur();
            if (n.getValeur() > max) max = n.getValeur();
        }

        double moyenne = somme / notes.size();

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  STATISTIQUES GÉNÉRALES                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Nombre de notes     : %-36d    ║%n", notes.size());
        System.out.printf("║  Moyenne générale    : %-36s    ║%n", df.format(moyenne) + "/20");
        System.out.printf("║  Note la plus basse  : %-36s    ║%n", df.format(min) + "/20");
        System.out.printf("║  Note la plus haute  : %-36s    ║%n", df.format(max) + "/20");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        // Appréciation
        System.out.println("\n Appréciation : ");
        if (moyenne >= 16) {
            System.out.println("    Excellent ! Continuez ainsi !");
        } else if (moyenne >= 14) {
            System.out.println("    Très bien ! Bon travail !");
        } else if (moyenne >= 12) {
            System.out.println("    Bien ! Vous progressez !");
        } else if (moyenne >= 10) {
            System.out.println("   Assez bien. Vous pouvez mieux faire !");
        } else {
            System.out.println("   Il faut travailler davantage pour progresser.");
        }

        attendreEntree();
    }

    private void afficherBulletin(Bulletin b){
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.printf(" ║  Bulletin - %-47s║%n", b.getPeriode());
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Moyenne personnelle : %-36s    ║%n", formatNote(b.getMoyenne()));
        System.out.printf("║  Moyenne de classe   : %-36s    ║%n", formatNote(b.getMoyennDelaClasse()));

        if (b.getMoyenne() != null && b.getMoyennDelaClasse() != null) {
            double diff = b.getMoyenne() - b.getMoyennDelaClasse();
            String position = diff > 0 ? "au-dessus" : diff < 0 ? "en-dessous" : "égale à";
            System.out.printf("║  Position            : %-36s    ║%n",
                    String.format("%.2f pts %s la moyenne", Math.abs(diff), position));
        }

        if (b.getCreatedAt() != null) {
            System.out.printf("║  Généré le           : %-36s    ║%n",
                    dtf.format(b.getCreatedAt().toLocalDateTime()));
        }
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void afficherTableauNotes(List<Note> notes){
        System.out.println("╔══════════════╤══════════════════════════╤════════╤════════════════════╗");
        System.out.println("║ Période      │ Matière                  │ Note   │ Enseignant         ║");
        System.out.println("╠══════════════╪══════════════════════════╪════════╪════════════════════╣");


        for (Note n : notes) {
            System.out.printf("║ %-12s │ %-24s │ %-6s │ %-18s ║%n",
                    truncate(n.getPeriode(), 12),
                    truncate(n.getMatiere(), 24),
                    df.format(n.getValeur()),
                    truncate(n.getEnseignantNomComplet(), 18));
        }
        System.out.println("╚══════════════╧══════════════════════════╧════════╧════════════════════╝");

    }

    private String formatNote(Double note) {
        return note == null ? "N/A" : df.format(note) + "/20";
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) {
            return String.format("%-" + maxLength + "s", str);
        }
        return str.substring(0, maxLength - 2) + "..";
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
