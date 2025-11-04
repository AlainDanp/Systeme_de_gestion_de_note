import Admin.dao.AdminDao;
import Admin.vue.MenuAdminView;
import Auhentifcation.AuthenticationService;
import Auhentifcation.AuthenticationServiceImpl;
import Auhentifcation.LoginView;
import Auhentifcation.ProfilView;
import MVC.Role;
import MVC.User;
import gestion_Bulletin.dao.BulletinDao;
import gestion_Bulletin.dao.DataSourceProvider;
import gestion_Bulletin.service.BulletinService;
import gestion_Bulletin.service.BulletinServiceImpl;
import gestion_Bulletin.vue.BulletinView;
import gestion_Classe.dao.ClasseDao;
import gestion_Classe.service.ClasseService;
import gestion_Classe.service.ClasseServiceImpl;
import gestion_Classe.vue.ClasseView;
import gestion_Enseignant.dao.EnseignantDao;
import gestion_Enseignant.service.EnseignantService;
import gestion_Enseignant.service.EnseignantServiceImpl;
import gestion_Enseignant.vue.EnseignantView;
import gestion_Enseignant.vue.MenuEnseignantView;
import gestion_Etudiant.dao.EtudiantDao;
import gestion_Etudiant.service.EtudiantService;
import gestion_Etudiant.service.EtudiantServiceImpl;
import gestion_Etudiant.vue.EtudiantView;
import gestion_Etudiant.vue.MenuEtudiantView;
import gestion_Matiere.dao.MatiereDao;
import gestion_Matiere.service.MatiereService;
import gestion_Matiere.service.MatiereServiceImpl;
import gestion_Matiere.vue.MatiereView;
import gestion_Note.dao.NoteDao;
import gestion_Note.service.NoteService;
import gestion_Note.service.NoteServiceImpl;
import gestion_Note.vue.NoteView;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;


public class Application {

    private DataSource dataSource;
    private Scanner scanner;

    private AdminDao adminDao;
    private BulletinDao bulletinDao;
    private NoteDao noteDao;
    private MatiereDao matiereDao;
    private EnseignantDao enseignantDao;
    private EtudiantDao etudiantDao;
    private ClasseDao classeDao;



    private BulletinService bulletinService;
    private NoteService noteService;
    private MatiereService matiereService;
    private EnseignantService enseignantService;
    private ClasseService classeService;
    private EtudiantService etudiantService;
    private AuthenticationService authenticationService;


    private BulletinView bulletinView;
    private NoteView noteView;
    private MatiereView matiereView;
    private ClasseView classeView;
    private EnseignantView enseignantView;
    private EtudiantView etudiantView;


    private LoginView loginView;
    private ProfilView profilView;

    private MenuEnseignantView menuEnseignantView;
    private MenuEtudiantView menuEtudiantView;
    private MenuAdminView menuAdminView;


    private void initialiser() {
        System.out.println("Démarrage de l'application...");

        try {
            dataSource = DataSourceProvider.getDataSource();
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

        adminDao = new AdminDao(dataSource);
        bulletinDao = new BulletinDao(dataSource);
        noteDao = new NoteDao(dataSource);
        matiereDao = new MatiereDao(dataSource);
        enseignantDao = new EnseignantDao(dataSource);
        etudiantDao = new EtudiantDao(dataSource);
        classeDao = new ClasseDao(dataSource);

       // System.out.println(" DAO initialisés");




        noteService = new NoteServiceImpl(dataSource, noteDao);
        bulletinService = new BulletinServiceImpl(dataSource, bulletinDao);
        matiereService = new MatiereServiceImpl(dataSource, matiereDao);
        enseignantService = new EnseignantServiceImpl(dataSource, enseignantDao);
        etudiantService = new EtudiantServiceImpl(dataSource, etudiantDao);
        classeService = new ClasseServiceImpl(dataSource, classeDao);
        authenticationService = new AuthenticationServiceImpl(adminDao,enseignantDao, etudiantDao);
       // System.out.println("Services initialisés");

        loginView = new LoginView(authenticationService, scanner);
        profilView = new ProfilView(authenticationService, scanner);


        // Initialisation des Vues
        bulletinView = new BulletinView(bulletinService, scanner);
        noteView = new NoteView(noteService, scanner);
        classeView = new ClasseView(classeService, scanner);
        matiereView = new MatiereView(matiereService, scanner);
        enseignantView = new EnseignantView(enseignantService, scanner);
        etudiantView = new EtudiantView(etudiantService, scanner);

        menuEnseignantView = new MenuEnseignantView(
                authenticationService, scanner,
                noteView, bulletinView, matiereView, profilView
        );
        menuEtudiantView = new MenuEtudiantView(
                authenticationService, noteService, bulletinService,
                profilView, scanner
        );
        menuAdminView = new MenuAdminView(
                authenticationService, scanner,
                enseignantView, etudiantView, classeView,
                matiereView, noteView, bulletinView, profilView
        );

        //System.out.println(" Vues initialisées");

        System.out.println("Application prête !\n");


    }


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
       while (true){
           Optional<User> user =loginView.afficherLogin();

           if (user.isEmpty()){
               System.out.println("\n Au revoir !");
               break;
           }
           User authenticatedUser = user.get();
           if (authenticatedUser.getRole() == Role.ADMIN) {
               menuAdminView.afficher();
           } else if (authenticatedUser.getRole() == Role.ENSEIGNANT) {
               menuEnseignantView.afficher();
           } else if (authenticatedUser.getRole() == Role.ETUDIANT) {
               menuEtudiantView.afficher();
           } else {
               System.out.println(" Rôle non reconnu");
           }
       }
       arreter();
    }

    /**
     * Affiche la bannière de l'application
     */
    private void afficherBanniere() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                ║");
        System.out.println("║          SYSTÈME DE GESTION DES NOTES ETUDIANTS                ║");
        System.out.println("║          Version 2.0                                           ║");
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
                noteView.menu();
                break;

            case "2":
                bulletinView.menu();
                break;

            case "3":
                etudiantView.menu();
                break;

            case "4":
                matiereView.menu();
                break;

            case "5":
                enseignantView.menu();
                break;

            case "6":
                classeView.menu();
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
        System.out.println("║  Version       : 2.0.0                                         ║");
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