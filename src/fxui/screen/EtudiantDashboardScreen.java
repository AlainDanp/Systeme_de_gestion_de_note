package fxui.screen;

import MVC.User;
import core.AppContext;
import fxui.Navigator;
import fxui.Sidebar;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/** Tableau de bord Étudiant : sidebar persistante + contenu central (refonte Figma). */
public class EtudiantDashboardScreen {

    private final AppContext appContext;
    private final Navigator navigator;
    private final User etudiant;
    private final StackPane content = new StackPane();
    private Sidebar sidebar;

    public EtudiantDashboardScreen(AppContext appContext, Navigator navigator, User etudiant) {
        this.appContext = appContext;
        this.navigator = navigator;
        this.etudiant = etudiant;
    }

    public Parent build() {
        content.getStyleClass().add("content-card");

        List<Sidebar.Entry> entries = List.of(
                new Sidebar.Entry("📝", "Mes Notes", () ->
                        new MesNotesScreen(appContext.getNoteService(), etudiant.getId(),
                                this::showOverview).build()),
                new Sidebar.Entry("📊", "Mes Bulletins", () ->
                        new MesBulletinsScreen(appContext.getBulletinService(), etudiant.getId(),
                                this::showOverview).build()),
                new Sidebar.Entry("📈", "Mes\nStatistiques", () ->
                        new MesStatistiquesScreen(appContext.getNoteService(), etudiant.getId(),
                                this::showOverview).build()),
                new Sidebar.Entry("👤", "Mon Profil", () ->
                        new ProfilScreen(appContext.getAuthenticationService(), this::showOverview).build())
        );

        sidebar = new Sidebar(entries,
                () -> setContent(new NotificationScreen(appContext.getNotificationListener(), etudiant.getId(),
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
        Label titre = new Label("Bienvenue, " + etudiant.getNomComplet());
        titre.getStyleClass().add("screen-title");
        Label sousTitre = new Label("Étudiant — utilisez le menu à gauche pour consulter vos résultats.");
        VBox box = new VBox(10, titre, sousTitre);
        box.setPadding(new Insets(20));
        setContent(box);
    }
}
