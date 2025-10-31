

import gestion_Bulletin.dao.BulletinDao;
import gestion_Bulletin.dao.DataSourceProvider;
import gestion_Bulletin.service.BulletinService;
import gestion_Bulletin.service.BulletinServiceImpl;
import gestion_Bulletin.vue.BulletinView;
import gestion_Matier.dao.MatiereDao;
import gestion_Matier.service.MatiereService;
import gestion_Matier.service.MatiereServiceImpl;
import gestion_Matier.vue.MatiereView;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;


public class Application {

    private DataSource dataSource;
    private Scanner scanner;


    private BulletinDao bulletinDao;
//    private NoteDao noteDao;
    private MatiereDao matiereDao;


    private BulletinService bulletinService;
//    private NoteService noteService;
    private MatiereService matiereService;


    private BulletinView bulletinView;
//    private NoteView noteView;
    private MatiereView matiereView;


    private void initialiser() {
        System.out.println("Démarrage de l'application...");

        // Initialisation du DataSource
        try {
            dataSource = DataSourceProvider.getDataSource();
          //  System.out.println("DataSource initialisé");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du DataSource");
            e.printStackTrace();
            System.exit(1);
        }

        // Test de connexion
        if (!testerConnexion()) {
            System.err.println(" Impossible de se connecter à la base de données");
            System.err.println("Vérifiez votre configuration (JDBC_URL, JDBC_USER, JDBC_PASSWORD)");
            System.exit(1);
        }

        scanner = new Scanner(System.in);

        bulletinDao = new BulletinDao(dataSource);
//        noteDao = new NoteDao(dataSource);
        matiereDao = new MatiereDao(dataSource);
       // System.out.println(" DAO initialisés");




//        noteService = new NoteServiceImpl(dataSource, noteDao);
        bulletinService = new BulletinServiceImpl(dataSource, bulletinDao);
        matiereService = new MatiereServiceImpl(dataSource, matiereDao);
       // System.out.println("Services initialisés");

        // Initialisation des Vues
        bulletinView = new BulletinView(bulletinService, scanner);
//        noteView = new NoteView(noteService, scanner);
        matiereView = new MatiereView(matiereService, scanner);
        //System.out.println(" Vues initialisées");




        System.out.println("Application prête !\n");


    }

    /**
     * Teste la connexion à la base de données
     */
    private boolean testerConnexion() {
        try (Connection conn = dataSource.getConnection()) {
            //System.out.println(" Connexion à la base de données réussie");
            //System.out.println("   Database : " + conn.getCatalog());
            return true;
        } catch (SQLException e) {
            System.err.println(" Erreur de connexion : " + e.getMessage());
            return false;
        }
    }

    /**
     * Démarre l'application
     */
    public void demarrer() {
        initialiser();
        afficherBanniere();
        menuPrincipal();
        arreter();
    }

    /**
     * Affiche la bannière de l'application
     */
    private void afficherBanniere() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                ║");
        System.out.println("║          SYSTÈME DE GESTION DES NOTES ETUDIANTS                ║");
        System.out.println("║          Version 1.0                                           ║");
        System.out.println("║                                                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Gère le menu principal de l'application
     */
    private void menuPrincipal() {
        while (true) {
            afficherMenuPrincipal();
            String choix = lireChoix();

            if (!traiterChoixMenuPrincipal(choix)) {
                break;
            }
        }
    }

    /**
     * Affiche le menu principal
     */
    private void afficherMenuPrincipal() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                       MENU PRINCIPAL                           ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Gestion des Notes                        [À venir]         ║");
        System.out.println("║  2. Gestion des Bulletins                                      ║");
        System.out.println("║  3. Gestion des Étudiants                    [À venir]         ║");
        System.out.println("║  4. Gestion des Matières                                       ║");
        System.out.println("║  5. Gestion des Enseignants                  [À venir]         ║");
        System.out.println("║  6. Gestion des Classes                      [À venir]         ║");
        System.out.println("║                                                                ║");
        System.out.println("║  9. À propos                                                   ║");
        System.out.println("║  0. Quitter                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix > ");
    }

    /**
     * Lit le choix de l'utilisateur
     */
    private String lireChoix() {
        return scanner.nextLine().trim();
    }

    /**
     * Traite le choix du menu principal
     * @return false si l'utilisateur veut quitter, true sinon
     */
    private boolean traiterChoixMenuPrincipal(String choix) {
        switch (choix) {
            case "1":
                System.out.println("\n⚠️  La gestion des notes sera disponible prochainement.");
                attendreEntree();
                break;

            case "2":
                bulletinView.menu();
                break;

            case "3":
                System.out.println("\n⚠️  La gestion des étudiants sera disponible prochainement.");
                attendreEntree();
                break;

            case "4":
                matiereView.menu();
                break;

            case "5":
                System.out.println("\n⚠️  La gestion des enseignants sera disponible prochainement.");
                attendreEntree();
                break;

            case "6":
                System.out.println("\n⚠️  La gestion des classes sera disponible prochainement.");
                attendreEntree();
                break;

            case "9":
                afficherAPropos();
                attendreEntree();
                break;

            case "0":
                return confirmerQuitter();

            default:
                System.out.println("\n Choix invalide. Veuillez entrer un nombre entre 0 et 9.");
                attendreEntree();
                break;
        }
        return true;
    }

    /**
     * Affiche les informations "À propos"
     */
    private void afficherAPropos() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        À PROPOS                                ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Application   : Système de Gestion Scolaire                   ║");
        System.out.println("║  Version       : 1.0.0                                         ║");
        System.out.println("║  Date          : Octobre 2025                                  ║");
        System.out.println("║  Base de données : PostgreSQL                                  ║");
        System.out.println("║                                                                ║");
        System.out.println("║  Fonctionnalités :                                             ║");
        System.out.println("║    Gestion des notes                                           ║");
        System.out.println("║    Génération automatique de bulletins                         ║");
        System.out.println("║    Calcul des moyennes                                         ║");
        System.out.println("║    Gestion des matières et enseignants                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Demande confirmation pour quitter
     */
    private boolean confirmerQuitter() {
        System.out.print("\n️  Êtes-vous sûr de vouloir quitter ? (o/N) : ");
        String reponse = scanner.nextLine().trim().toLowerCase();

        if ("o".equals(reponse) || "oui".equals(reponse)) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║                    Au revoir !                                 ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
            return false; // Quitter
        } else {
            System.out.println(" Retour au menu principal");
            return true; // Continuer
        }
    }

    /**
     * Attend que l'utilisateur appuie sur Entrée
     */
    private void attendreEntree() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    /**
     * Arrête proprement l'application
     */
    private void arreter() {
        System.out.println(" Fermeture de l'application...");

        // Fermer le scanner
        if (scanner != null) {
            scanner.close();
            //System.out.println(" Scanner fermé");
        }

        // Fermer le DataSource
        try {
            DataSourceProvider.close();
            //System.out.println(" DataSource fermé");
        } catch (Exception e) {
            System.err.println("  Erreur lors de la fermeture du DataSource : " + e.getMessage());
        }

        System.out.println(" Application arrêtée proprement");
    }
}