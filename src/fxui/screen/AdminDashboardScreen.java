package fxui.screen;

import MVC.User;
import core.AppContext;
import fxui.Navigator;
import fxui.Sidebar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/** Tableau de bord Administrateur : sidebar persistante + contenu central (refonte Figma). */
public class AdminDashboardScreen {

    private final AppContext appContext;
    private final Navigator navigator;
    private final User admin;
    private final StackPane content = new StackPane();
    private Sidebar sidebar;

    public AdminDashboardScreen(AppContext appContext, Navigator navigator, User admin) {
        this.appContext = appContext;
        this.navigator = navigator;
        this.admin = admin;
    }

    public Parent build() {
        content.getStyleClass().add("content-card");

        List<Sidebar.Entry> entries = List.of(
                new Sidebar.Entry("🧑‍🏫", "Gestion des\nEnseignants", () ->
                        new EnseignantScreen(appContext.getEnseignantService(), appContext.getDataSource(),
                                this::showOverview).build()),
                new Sidebar.Entry("🎓", "Gestion des\nÉtudiants", () ->
                        new EtudiantScreen(appContext.getEtudiantService(), appContext.getClasseService(),
                                appContext.getDataSource(), this::showOverview).build()),
                new Sidebar.Entry("🏫", "Gestion des\nClasses", () ->
                        new ClasseScreen(appContext.getClasseService(), appContext.getDataSource(),
                                this::showOverview).build()),
                new Sidebar.Entry("📘", "Gestion des\nMatières", () ->
                        new MatiereScreen(appContext.getMatiereService(), navigator, this::showOverview).build()),
                new Sidebar.Entry("📝", "Gestion des\nNotes", () ->
                        new NoteScreen(appContext.getNoteService(), appContext.getEtudiantService(),
                                appContext.getDataSource(), this::showOverview).build()),
                new Sidebar.Entry("📊", "Gestion des\nBulletins", () ->
                        new BulletinScreen(appContext.getBulletinService(), appContext.getEtudiantService(),
                                appContext.getDataSource(), this::showOverview).build()),
                new Sidebar.Entry("👤", "Mon Profil", () ->
                        new ProfilScreen(appContext.getAuthenticationService(), this::showOverview).build())
        );

        sidebar = new Sidebar(entries,
                () -> setContent(new NotificationScreen(appContext.getNotificationListener(), admin.getId(),
                        this::showOverview).build()),
                () -> {
                    appContext.getAuthenticationService().logout();
                    navigator.showLogin();
                },
                this::setContent);

        showOverview();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("content-shell");
        root.setLeft(sidebar.getNode());
        root.setCenter(content);
        return root;
    }

    private void setContent(Parent node) {
        content.getChildren().setAll(node);
    }

    private void showOverview() {
        sidebar.clearSelection();
        Label titre = new Label("Bienvenue, " + admin.getNomComplet());
        titre.getStyleClass().add("overview-title");
        Label sousTitre = new Label("Administrateur — utilisez le menu à gauche pour gérer l'établissement.");
        sousTitre.getStyleClass().add("overview-subtitle");
        VBox box = new VBox(14, titre, sousTitre);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        setContent(box);
    }
}
