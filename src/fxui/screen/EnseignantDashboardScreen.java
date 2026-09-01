package fxui.screen;

import MVC.Role;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Tableau de bord Enseignant : sidebar persistante + contenu central (refonte Figma).
 * Réutilise les mêmes écrans Notes/Bulletins/Matières que l'Admin (même service/DAO, la couche
 * SecurityContext restreint déjà l'écriture des notes à la matière de l'enseignant connecté).
 * "Gestion des Bulletins" n'est visible que pour un enseignant Titulaire (droit vérifié en
 * plus côté service par {@code SecurityContext.exigerDroitBulletin()}).
 */
public class EnseignantDashboardScreen {

    private final AppContext appContext;
    private final Navigator navigator;
    private final User enseignant;
    private final StackPane content = new StackPane();
    private Sidebar sidebar;

    public EnseignantDashboardScreen(AppContext appContext, Navigator navigator, User enseignant) {
        this.appContext = appContext;
        this.navigator = navigator;
        this.enseignant = enseignant;
    }

    public Parent build() {
        content.getStyleClass().add("content-card");

        List<Sidebar.Entry> entries = new ArrayList<>();
        entries.add(new Sidebar.Entry("📝", "Gestion des\nNotes", () ->
                new NoteScreen(appContext.getNoteService(), appContext.getEtudiantService(),
                        appContext.getDataSource(), this::showOverview).build()));
        if (enseignant.getRole() == Role.TITULAIRE) {
            entries.add(new Sidebar.Entry("📊", "Gestion des\nBulletins", () ->
                    new BulletinScreen(appContext.getBulletinService(), appContext.getEtudiantService(),
                            appContext.getDataSource(), this::showOverview).build()));
        }
        entries.add(new Sidebar.Entry("📘", "Mes Matières", () ->
                new MatiereScreen(appContext.getMatiereService(), navigator, this::showOverview).build()));
        entries.add(new Sidebar.Entry("👤", "Mon Profil", () ->
                new ProfilScreen(appContext.getAuthenticationService(), this::showOverview).build()));

        sidebar = new Sidebar(entries,
                () -> setContent(new NotificationScreen(appContext.getNotificationListener(), enseignant.getId(),
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
        Label titre = new Label("Bienvenue, " + enseignant.getNomComplet());
        titre.getStyleClass().add("overview-title");
        Label sousTitre = new Label(enseignant.getRole().getLibelle()
                + " — utilisez le menu à gauche pour gérer vos notes"
                + (enseignant.getRole() == Role.TITULAIRE ? " et bulletins." : "."));
        sousTitre.getStyleClass().add("overview-subtitle");
        VBox box = new VBox(14, titre, sousTitre);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        setContent(box);
    }
}
