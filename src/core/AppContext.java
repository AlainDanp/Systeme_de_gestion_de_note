package core;

import Admin.dao.AdminDao;
import Auhentifcation.AuthenticationService;
import Auhentifcation.AuthenticationServiceImpl;
import BD.DataSourceProvider;
import Event.EventDispatcher;
import Event.listener.ConsoleLoggerListener;
import Event.listener.EmailNotificationListener;
import Event.listener.FileLoggerListener;
import Event.listener.NotificationListener;
import Event.listener.StatisticsListener;
import MVC.SecurityContext;
import gestion_Bulletin.dao.BulletinDao;
import gestion_Bulletin.service.BulletinService;
import gestion_Bulletin.service.BulletinServiceImpl;
import gestion_Classe.dao.ClasseDao;
import gestion_Classe.service.ClasseService;
import gestion_Classe.service.ClasseServiceImpl;
import gestion_Enseignant.dao.EnseignantDao;
import gestion_Enseignant.service.EnseignantService;
import gestion_Enseignant.service.EnseignantServiceImpl;
import gestion_Etudiant.dao.EtudiantDao;
import gestion_Etudiant.service.EtudiantService;
import gestion_Etudiant.service.EtudiantServiceImpl;
import gestion_Matiere.dao.MatiereDao;
import gestion_Matiere.service.MatiereService;
import gestion_Matiere.service.MatiereServiceImpl;
import gestion_Note.dao.NoteDao;
import gestion_Note.service.NoteService;
import gestion_Note.service.NoteServiceImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Point d'entrée unique du bootstrap applicatif (DataSource, DAO, services, événements).
 * Partagé par l'application console (Application/Main) et l'application JavaFX (fxui.JavaFxApp)
 * pendant la migration progressive vers l'interface graphique.
 */
public final class AppContext {

    private final DataSource dataSource;
    private final SecurityContext securityContext;
    private final EventDispatcher eventDispatcher;
    private final NotificationListener notificationListener;
    private final StatisticsListener statisticsListener;

    private final AuthenticationService authenticationService;
    private final NoteService noteService;
    private final BulletinService bulletinService;
    private final MatiereService matiereService;
    private final EnseignantService enseignantService;
    private final EtudiantService etudiantService;
    private final ClasseService classeService;

    private AppContext(DataSource dataSource, SecurityContext securityContext,
                        EventDispatcher eventDispatcher, NotificationListener notificationListener,
                        StatisticsListener statisticsListener, AuthenticationService authenticationService,
                        NoteService noteService, BulletinService bulletinService,
                        MatiereService matiereService, EnseignantService enseignantService,
                        EtudiantService etudiantService, ClasseService classeService) {
        this.dataSource = dataSource;
        this.securityContext = securityContext;
        this.eventDispatcher = eventDispatcher;
        this.notificationListener = notificationListener;
        this.statisticsListener = statisticsListener;
        this.authenticationService = authenticationService;
        this.noteService = noteService;
        this.bulletinService = bulletinService;
        this.matiereService = matiereService;
        this.enseignantService = enseignantService;
        this.etudiantService = etudiantService;
        this.classeService = classeService;
    }

    /**
     * Construit le contexte applicatif : DataSource, test de connexion, système d'événements,
     * DAO et services. Quitte le processus si la connexion à la base échoue (comportement
     * identique à l'ancien Application.initialiser()).
     */
    public static AppContext build() {
        DataSource dataSource;
        try {
            dataSource = DataSourceProvider.getDataSource();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du DataSource");
            e.printStackTrace();
            System.exit(1);
            return null;
        }

        if (!testerConnexion(dataSource)) {
            System.err.println(" Impossible de se connecter à la base de données");
            System.err.println("Vérifiez votre configuration (JDBC_URL, JDBC_USER, JDBC_PASSWORD)");
            System.exit(1);
            return null;
        }

        EventDispatcher eventDispatcher = EventDispatcher.getInstance();
        NotificationListener notificationListener = new NotificationListener();
        StatisticsListener statisticsListener = new StatisticsListener();

        eventDispatcher.registerListener(new ConsoleLoggerListener());
        eventDispatcher.registerListener(notificationListener);
        eventDispatcher.registerListener(new FileLoggerListener("logs/application.log"));
        eventDispatcher.registerListener(statisticsListener);
        eventDispatcher.registerListener(new EmailNotificationListener());

        AdminDao adminDao = new AdminDao(dataSource);
        BulletinDao bulletinDao = new BulletinDao(dataSource);
        NoteDao noteDao = new NoteDao(dataSource);
        MatiereDao matiereDao = new MatiereDao(dataSource);
        EnseignantDao enseignantDao = new EnseignantDao(dataSource);
        EtudiantDao etudiantDao = new EtudiantDao(dataSource);
        ClasseDao classeDao = new ClasseDao(dataSource);

        SecurityContext securityContext = new SecurityContext();

        NoteService noteService = new NoteServiceImpl(dataSource, noteDao, securityContext);
        BulletinService bulletinService = new BulletinServiceImpl(dataSource, bulletinDao, securityContext);
        MatiereService matiereService = new MatiereServiceImpl(dataSource, matiereDao, securityContext);
        EnseignantService enseignantService = new EnseignantServiceImpl(dataSource, enseignantDao, securityContext);
        EtudiantService etudiantService = new EtudiantServiceImpl(dataSource, etudiantDao, securityContext);
        ClasseService classeService = new ClasseServiceImpl(dataSource, classeDao, securityContext);
        AuthenticationService authenticationService = new AuthenticationServiceImpl(adminDao, enseignantDao, etudiantDao);

        return new AppContext(dataSource, securityContext, eventDispatcher, notificationListener,
                statisticsListener, authenticationService, noteService, bulletinService,
                matiereService, enseignantService, etudiantService, classeService);
    }

    private static boolean testerConnexion(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            return true;
        } catch (SQLException e) {
            System.err.println(" Erreur de connexion : " + e.getMessage());
            return false;
        }
    }

    public DataSource getDataSource() { return dataSource; }
    public SecurityContext getSecurityContext() { return securityContext; }
    public EventDispatcher getEventDispatcher() { return eventDispatcher; }
    public NotificationListener getNotificationListener() { return notificationListener; }
    public StatisticsListener getStatisticsListener() { return statisticsListener; }
    public AuthenticationService getAuthenticationService() { return authenticationService; }
    public NoteService getNoteService() { return noteService; }
    public BulletinService getBulletinService() { return bulletinService; }
    public MatiereService getMatiereService() { return matiereService; }
    public EnseignantService getEnseignantService() { return enseignantService; }
    public EtudiantService getEtudiantService() { return etudiantService; }
    public ClasseService getClasseService() { return classeService; }
}
