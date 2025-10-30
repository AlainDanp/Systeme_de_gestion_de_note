package vue;

import model.Bulletin;
import service.BulletinService;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BulletinView {
    private final BulletinService service;
    private final Scanner scanner;
    private final DecimalFormat df = new DecimalFormat("#0.00");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public BulletinView(BulletinService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void menu() {
        while (true) {
            System.out.println("\n=== Gestion des bulletins ===");
            System.out.println("1. Générer & enregistrer un bulletin (par étudiant & période)");
            System.out.println("2. Créer un bulletin manuellement");
            System.out.println("3. Modifier un bulletin (par id)");
            System.out.println("4. Voir bulletin par id");
            System.out.println("5. Rechercher bulletin par étudiant et période");
            System.out.println("6. Lister tous les bulletins d'un étudiant");
            System.out.println("7. Supprimer un bulletin");
            System.out.println("0. Retour");
            System.out.print("Choix > ");
            String ch = scanner.nextLine().trim();
            try {
                switch (ch) {
                    case "1": genererEtEnregistrer(); break;
                    case "2": creerManuel(); break;
                    case "3": modifier(); break;
                    case "4": voirParId(); break;
                    case "5": rechercherParEtudiantPeriode(); break;
                    case "6": listerParEtudiant(); break;
                    case "7": supprimer(); break;
                    case "0": return;
                    default: System.out.println("Choix invalide."); break;
                }
            } catch (Exception ex) {
                System.out.println("Erreur : " + ex.getMessage());
            }
        }
    }

    private void genererEtEnregistrer() {
        try {
            System.out.print("Id étudiant : ");
            int etuId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Période (ex : 2024-T1) : ");
            String periode = scanner.nextLine().trim();
            if (periode.isBlank()) {
                System.out.println("Période obligatoire.");
                return;
            }

            Optional<Bulletin> existed = service.getParEtudiantEtPeriode(etuId, periode);
            boolean existedBefore = existed.isPresent();

            System.out.println("Génération en cours...");
            Bulletin result;
            try {
                result = service.genererEtEnregistrerBulletin(etuId, periode);
            } catch (RuntimeException ex) {
                String sqlState = findSqlState(ex);
                if ("23505".equals(sqlState)) {
                    System.out.println("Conflit d'insertion détecté (autre processus). Récupération de l'enregistrement existant...");
                    Optional<Bulletin> again = service.getParEtudiantEtPeriode(etuId, periode);
                    if (again.isPresent()) {
                        afficherBulletin(again.get());
                        return;
                    } else {
                        throw ex;
                    }
                } else {
                    throw ex;
                }
            }

            if (existedBefore) System.out.println("Bulletin mis à jour :");
            else System.out.println("Bulletin créé :");
            afficherBulletin(result);
        } catch (NumberFormatException ex) {
            System.out.println("Id étudiant invalide.");
        }
    }

    private void creerManuel() {
        try {
            System.out.print("Id étudiant : ");
            int etuId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Période : ");
            String periode = scanner.nextLine().trim();
            if (periode.isBlank()) {
                System.out.println("Période obligatoire.");
                return;
            }
            Double moyenne = lireDoubleNullable("Moyenne étudiant (ou vide) : ");
            Double moyenneClasse = lireDoubleNullable("Moyenne de la classe (ou vide) : ");

            Bulletin b = new Bulletin();
            b.setEtudiantId(etuId);
            b.setPeriode(periode);
            b.setMoyenne(moyenne);
            b.setMoyennDelaClasse(moyenneClasse);


            Bulletin persisted = service.creeBulletin(b);
            System.out.println("Bulletin persisté :");
            afficherBulletin(persisted);
        } catch (NumberFormatException ex) {
            System.out.println("Id étudiant invalide.");
        } catch (Exception ex) {
            System.out.println("Erreur création : " + ex.getMessage());
        }
    }

    private void modifier() {
        try {
            System.out.print("Id bulletin à modifier : ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<Bulletin> ob = service.getBulletin(id);
            if (ob.isEmpty()) {
                System.out.println("Bulletin introuvable pour id = " + id);
                return;
            }
            Bulletin b = ob.get();
            System.out.println("Bulletin courant :");
            afficherBulletin(b);

            Double moyenne = lireDoubleNullable("Nouvelle moyenne étudiant (ou vide pour laisser) : ");
            Double moyenneClasse = lireDoubleNullable("Nouvelle moyenne classe (ou vide pour laisser) : ");

            if (moyenne != null) b.setMoyenne(moyenne);
            if (moyenneClasse != null) b.setMoyennDelaClasse(moyenneClasse);

            service.update(b);
            Optional<Bulletin> updated = service.getParEtudiantEtPeriode(b.getEtudiantId(), b.getPeriode());
            System.out.println("Bulletin mis à jour :");
            afficherBulletin(updated.orElse(b));
        } catch (NumberFormatException ex) {
            System.out.println("Id invalide.");
        } catch (Exception ex) {
            System.out.println("Erreur modification : " + ex.getMessage());
        }
    }

    private void voirParId() {
        try {
            System.out.print("Id bulletin : ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<Bulletin> ob = service.getBulletin(id);
            if (ob.isPresent()) afficherBulletin(ob.get());
            else System.out.println("Aucun bulletin trouvé pour id=" + id);
        } catch (NumberFormatException ex) {
            System.out.println("Id invalide.");
        }
    }

    private void rechercherParEtudiantPeriode() {
        try {
            System.out.print("Id étudiant : ");
            int etuId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Période : ");
            String periode = scanner.nextLine().trim();
            if (periode.isBlank()) {
                System.out.println("Période obligatoire.");
                return;
            }
            Optional<Bulletin> ob = service.getParEtudiantEtPeriode(etuId, periode);
            if (ob.isPresent()) afficherBulletin(ob.get());
            else System.out.println("Aucun bulletin trouvé pour l'étudiant " + etuId + " et la période '" + periode + "'.");
        } catch (NumberFormatException ex) {
            System.out.println("Id étudiant invalide.");
        }
    }

    private void listerParEtudiant() {
        try {
            System.out.print("Id étudiant : ");
            int etuId = Integer.parseInt(scanner.nextLine().trim());
            List<Bulletin> list = service.listerParEtudiant(etuId);
            if (list.isEmpty()) {
                System.out.println("Aucun bulletin pour l'étudiant " + etuId);
            } else {
                System.out.println("Bulletins pour l'étudiant " + etuId + " :");
                for (Bulletin b : list) {
                    afficherResume(b);
                }
            }
        } catch (NumberFormatException ex) {
            System.out.println("Id étudiant invalide.");
        }
    }

    private void supprimer() {
        try {
            System.out.print("Id bulletin à supprimer : ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirmez suppression (o/N) : ");
            String conf = scanner.nextLine().trim().toLowerCase();
            if (!"o".equals(conf) && !"oui".equals(conf)) {
                System.out.println("Suppression annulée.");
                return;
            }
            service.delete(id);
            System.out.println("Suppression effectuée (si bulletin existait).");
        } catch (NumberFormatException ex) {
            System.out.println("Id invalide.");
        } catch (Exception ex) {
            System.out.println("Erreur suppression : " + ex.getMessage());
        }
    }

    /* ----- affichages helpers ----- */

    private void afficherBulletin(Bulletin b) {
        System.out.println("--------------------------------------------------");
        System.out.println("Bulletin id   : " + safe(b.getId()));
        System.out.println("Étudiant id   : " + safe(b.getEtudiantId()));
        System.out.println("Période       : " + safe(b.getPeriode()));
        System.out.println("Moyenne élève : " + formatNullableDouble(b.getMoyenne()));
        System.out.println("Moyenne classe: " + formatNullableDouble(b.getMoyennDelaClasse()));
        if (b.getCreatedAt() != null) {
            System.out.println("Créé le       : " + dtf.format(b.getCreatedAt().toLocalDateTime()));
        }
        System.out.println("--------------------------------------------------");
    }

    private void afficherResume(Bulletin b) {
        String m = formatNullableDouble(b.getMoyenne());
        String mc = formatNullableDouble(b.getMoyennDelaClasse());
        String created = b.getCreatedAt() == null ? "-" : dtf.format(b.getCreatedAt().toLocalDateTime());
        System.out.printf("id=%s | etu=%s | periode=%s | moy=%s | moy_cl=%s | créé=%s%n",
                safe(b.getId()), safe(b.getEtudiantId()), safe(b.getPeriode()), m, mc, created);
    }

    private String formatNullableDouble(Double v) {
        return v == null ? "N/A" : df.format(v);
    }

    private String safe(Object o) {
        return o == null ? "-" : o.toString();
    }

    private Double lireDoubleNullable(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return null;
        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException ex) {
            System.out.println("Nombre invalide, valeur ignorée.");
            return null;
        }
    }

    private String findSqlState(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof java.sql.SQLException) {
                return ((java.sql.SQLException) cur).getSQLState();
            }
            cur = cur.getCause();
        }
        return null;
    }
}
