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
 * Tableau de bord Enseignant. Remplace gestion_Enseignant.vue.MenuEnseignantView.afficher().
 * Coquille de navigation : aucun écran enseignant n'est encore migré dans cette passe
 * (Notes/Bulletins/Matières restent accessibles via la console pour l'instant).
 */
public class EnseignantDashboardScreen {

    private final AppContext appContext;
    private final Navigator navigator;
    private final User enseignant;

    public EnseignantDashboardScreen(AppContext appContext, Navigator navigator, User enseignant) {
        this.appContext = appContext;
        this.navigator = navigator;
        this.enseignant = enseignant;
    }

    public Parent build() {
        Label titre = new Label("Bienvenue, " + enseignant.getNomComplet() + " (Enseignant)");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button notes = boutonAVenir("Gestion des Notes");
        Button bulletins = boutonAVenir("Gestion des Bulletins");
        Button matieres = boutonAVenir("Mes Matières");
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

        VBox root = new VBox(10, titre, notes, bulletins, matieres, notifications, profil, deconnexion);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        return root;
    }

    private void retour() {
        navigator.show(build(), "Tableau de bord - Enseignant");
    }

    private Button boutonAVenir(String texte) {
        Button bouton = new Button(texte);
        bouton.setDisable(true);
        bouton.setTooltip(new Tooltip("À venir"));
        return bouton;
    }
}
