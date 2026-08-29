package fxui.screen;

import MVC.User;
import core.AppContext;
import fxui.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * Tableau de bord Administrateur. Remplace Admin.vue.MenuAdminView.afficher() côté console.
 * Seule la gestion des matières est câblée pour l'instant (voir MatiereScreen) ; les autres
 * domaines restent des boutons désactivés en attendant leur migration JavaFX.
 */
public class AdminDashboardScreen {

    private final AppContext appContext;
    private final Navigator navigator;
    private final User admin;

    public AdminDashboardScreen(AppContext appContext, Navigator navigator, User admin) {
        this.appContext = appContext;
        this.navigator = navigator;
        this.admin = admin;
    }

    public Parent build() {
        Label titre = new Label("Bienvenue, " + admin.getNomComplet() + " (Administrateur)");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button matieres = new Button("Gestion des Matières");
        matieres.setOnAction(e -> navigator.show(
                new MatiereScreen(appContext.getMatiereService(), navigator, this::retour).build(),
                "Gestion des Matières"));

        Button enseignants = new Button("Gestion des Enseignants");
        enseignants.setOnAction(e -> navigator.show(
                new EnseignantScreen(appContext.getEnseignantService(), appContext.getDataSource(), this::retour).build(),
                "Gestion des Enseignants"));

        Button etudiants = new Button("Gestion des Étudiants");
        etudiants.setOnAction(e -> navigator.show(
                new EtudiantScreen(appContext.getEtudiantService(), appContext.getClasseService(),
                        appContext.getDataSource(), this::retour).build(),
                "Gestion des Étudiants"));

        Button classes = new Button("Gestion des Classes");
        classes.setOnAction(e -> navigator.show(
                new ClasseScreen(appContext.getClasseService(), appContext.getDataSource(), this::retour).build(),
                "Gestion des Classes"));

        Button notes = new Button("Gestion des Notes");
        notes.setOnAction(e -> navigator.show(
                new NoteScreen(appContext.getNoteService(), appContext.getDataSource(), this::retour).build(),
                "Gestion des Notes"));

        Button bulletins = new Button("Gestion des Bulletins");
        bulletins.setOnAction(e -> navigator.show(
                new BulletinScreen(appContext.getBulletinService(), appContext.getDataSource(), this::retour).build(),
                "Gestion des Bulletins"));

        Button notifications = boutonAVenir("Notifications");

        Button profil = new Button("Mon Profil");
        profil.setOnAction(e -> navigator.show(
                new ProfilScreen(appContext.getAuthenticationService(), this::retour).build(),
                "Mon Profil"));

        Button deconnexion = new Button("Déconnexion");
        deconnexion.setOnAction(e -> {
            appContext.getAuthenticationService().logout();
            navigator.showLogin();
        });

        VBox root = new VBox(10, titre, enseignants, etudiants, classes, matieres, notes, bulletins,
                notifications, profil, deconnexion);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        return root;
    }

    private void retour() {
        navigator.show(build(), "Tableau de bord - Administrateur");
    }

    private Button boutonAVenir(String texte) {
        Button bouton = new Button(texte);
        bouton.setDisable(true);
        bouton.setTooltip(new Tooltip("À venir"));
        return bouton;
    }
}
