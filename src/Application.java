import Admin.vue.MenuAdminView;
import Auhentifcation.AuthenticationService;
import Auhentifcation.LoginView;
import Auhentifcation.ProfilView;
import MVC.NotificationView;
import MVC.Role;
import MVC.SecurityContext;
import MVC.User;
import BD.DataSourceProvider;
import Event.listener.StatisticsListener;
import core.AppContext;
import gestion_Bulletin.service.BulletinService;
import gestion_Bulletin.service.BulletinServiceImpl;
import gestion_Bulletin.vue.BulletinView;
import gestion_Classe.service.ClasseService;
import gestion_Classe.service.ClasseServiceImpl;
import gestion_Classe.vue.ClasseView;
import gestion_Enseignant.model.Enseignant;
import gestion_Enseignant.service.EnseignantService;
import gestion_Enseignant.service.EnseignantServiceImpl;
import gestion_Enseignant.vue.EnseignantView;
import gestion_Enseignant.vue.MenuEnseignantView;
import gestion_Etudiant.service.EtudiantService;
import gestion_Etudiant.service.EtudiantServiceImpl;
import gestion_Etudiant.vue.EtudiantView;
import gestion_Etudiant.vue.MenuEtudiantView;
import gestion_Matiere.service.MatiereService;
import gestion_Matiere.vue.MatiereView;
import gestion_Note.service.NoteService;
import gestion_Note.service.NoteServiceImpl;
import gestion_Note.vue.NoteView;


import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class Application {

    private AppContext appContext;
    private DataSource dataSource;
    private Scanner scanner;

    // Services
    private BulletinService bulletinService;
    private NoteService noteService;
    private MatiereService matiereService;
    private EnseignantService enseignantService;
    private EtudiantService etudiantService;
    private ClasseService classeService;
    private AuthenticationService authenticationService;

    private StatisticsListener statisticsListener;

    // Vues communes
    private LoginView loginView;
    private ProfilView profilView;
    private NotificationView notificationView;

    // Vues partagées
    private NoteView noteView;
    private BulletinView bulletinView;
    private MatiereView matiereView;
    private EnseignantView enseignantView;
    private EtudiantView etudiantView;
    private ClasseView classeView;

    // Menus principaux
    private MenuAdminView menuAdminView;
    private MenuEnseignantView menuEnseignantView;
    private MenuEtudiantView menuEtudiantView;

    private SecurityContext securityContext;


    private void initialiser() {
        System.out.println("Démarrage de l'application...");

        appContext = AppContext.build();

        dataSource = appContext.getDataSource();
        securityContext = appContext.getSecurityContext();
        statisticsListener = appContext.getStatisticsListener();

        scanner = new Scanner(System.in);

        noteService = appContext.getNoteService();
        bulletinService = appContext.getBulletinService();
        matiereService = appContext.getMatiereService();
        enseignantService = appContext.getEnseignantService();
        etudiantService = appContext.getEtudiantService();
        classeService = appContext.getClasseService();
        authenticationService = appContext.getAuthenticationService();

        loginView = new LoginView(authenticationService, scanner);
        profilView = new ProfilView(authenticationService, scanner);
        notificationView = new NotificationView(appContext.getNotificationListener(), scanner);

        // Initialisation des vues
        bulletinView = new BulletinView(bulletinService, scanner, dataSource);
        noteView = new NoteView(noteService, scanner, dataSource);
        classeView = new ClasseView(classeService, scanner,dataSource);
        matiereView = new MatiereView(matiereService, scanner);
        enseignantView = new EnseignantView(enseignantService, scanner, dataSource);
        etudiantView = new EtudiantView(etudiantService, scanner, dataSource);

        menuEnseignantView = new MenuEnseignantView(
                authenticationService, scanner,
                noteView, bulletinView, matiereView,
                profilView, notificationView
        );
        menuEtudiantView = new MenuEtudiantView(
                authenticationService, noteService, bulletinService,
                profilView, notificationView, scanner //  Passée correctement
        );
        menuAdminView = new MenuAdminView(
                authenticationService, scanner,
                enseignantView, etudiantView, classeView,
                matiereView, noteView, bulletinView,
                profilView, notificationView
        );

        //System.out.println(" Vues initialisées");

        System.out.println("Application prête !\n");


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
           configurerUtilisateurCourant(authenticatedUser);

           if (authenticatedUser.getRole() == Role.ADMIN) {
               menuAdminView.afficher();
           } else if (authenticatedUser.getRole() == Role.ENSEIGNANT || authenticatedUser.getRole() == Role.TITULAIRE) {
               menuEnseignantView.afficher();
           } else if (authenticatedUser.getRole() == Role.ETUDIANT) {
               menuEtudiantView.afficher();
           } else {
               System.out.println(" Rôle non reconnu");
           }
           afficherStatistiquesSession();
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
    private void configurerUtilisateurCourant(User user) {
        if (noteService instanceof NoteServiceImpl) {
            ((NoteServiceImpl) noteService).setCurrentUser(user.getId(), user.getNomComplet());
        }
        if (bulletinService instanceof BulletinServiceImpl) {
            ((BulletinServiceImpl) bulletinService).setCurrentUser(user.getId(), user.getNomComplet());
        }
        if (enseignantService instanceof EnseignantServiceImpl) {
            ((EnseignantServiceImpl) enseignantService).setCurrentUser(user.getId(), user.getNomComplet());
        }
        if (etudiantService instanceof EtudiantServiceImpl) {
            ((EtudiantServiceImpl) etudiantService).setCurrentUser(user.getId(), user.getNomComplet());
        }

        String matiere = (user instanceof Enseignant) ? ((Enseignant) user).getMatiereNom() : null;
        List<Integer> classeIds = (user instanceof Enseignant)
                ? enseignantService.listerClasseIds(user.getId())
                : List.of();
        securityContext.setUser(user.getId(), user.getNomComplet(), user.getRole(), matiere, classeIds);
    }
    private void afficherStatistiquesSession() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              STATISTIQUES DE LA SESSION                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");

        var stats = statisticsListener.getAllEventCounts();
        if (stats.isEmpty()) {
            System.out.println("║  Aucune activité enregistrée                                   ║");
        } else {
            stats.forEach((type, count) -> {
                System.out.printf("║  %-40s : %5d          ║%n", type, count);
            });
        }

        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }
}