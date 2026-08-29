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
 * Tableau de bord Étudiant. Remplace gestion_Etudiant.vue.MenuEtudiantView.afficher().
 * Coquille de navigation : les écrans (mes notes/bulletins/statistiques) restent à migrer.
 */
public class EtudiantDashboardScreen {

    private final AppContext appContext;
    private final Navigator navigator;
    private final User etudiant;

    public EtudiantDashboardScreen(AppContext appContext, Navigator navigator, User etudiant) {
        this.appContext = appContext;
        this.navigator = navigator;
        this.etudiant = etudiant;
    }

    public Parent build() {
        Label titre = new Label("Bienvenue, " + etudiant.getNomComplet() + " (Étudiant)");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button mesNotes = boutonAVenir("Mes Notes");
        Button mesBulletins = boutonAVenir("Mes Bulletins");
        Button mesStatistiques = boutonAVenir("Mes Statistiques");
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

        VBox root = new VBox(10, titre, mesNotes, mesBulletins, mesStatistiques, notifications, profil, deconnexion);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        return root;
    }

    private void retour() {
        navigator.show(build(), "Tableau de bord - Étudiant");
    }

    private Button boutonAVenir(String texte) {
        Button bouton = new Button(texte);
        bouton.setDisable(true);
        bouton.setTooltip(new Tooltip("À venir"));
        return bouton;
    }
}
