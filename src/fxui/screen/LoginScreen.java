package fxui.screen;

import Auhentifcation.AuthenticationService;
import MVC.Role;
import MVC.SecurityContext;
import MVC.User;
import core.AppContext;
import fxui.Navigator;
import gestion_Enseignant.model.Enseignant;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Optional;

/** Écran de connexion, remplace Auhentifcation.LoginView.afficherLogin() côté console. */
public class LoginScreen {

    private final AppContext appContext;
    private final Navigator navigator;

    public LoginScreen(AppContext appContext, Navigator navigator) {
        this.appContext = appContext;
        this.navigator = navigator;
    }

    public Parent build() {
        Label titre = new Label("Système de Gestion des Notes");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField loginField = new TextField();
        loginField.setPromptText("Identifiant");
        loginField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(250);

        Label erreur = new Label();
        erreur.setStyle("-fx-text-fill: red;");

        Button seConnecter = new Button("Se connecter");
        seConnecter.setDefaultButton(true);
        seConnecter.setOnAction(e -> tenterConnexion(loginField.getText(), passwordField.getText(), erreur));

        VBox root = new VBox(12, titre, loginField, passwordField, seConnecter, erreur);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        return root;
    }

    private void tenterConnexion(String login, String password, Label erreur) {
        AuthenticationService authService = appContext.getAuthenticationService();
        try {
            Optional<User> resultat = authService.authenticate(login, password);
            if (resultat.isEmpty()) {
                erreur.setText("Identifiant ou mot de passe incorrect.");
                return;
            }

            User user = resultat.get();
            SecurityContext securityContext = appContext.getSecurityContext();
            String matiere = (user instanceof Enseignant) ? ((Enseignant) user).getMatiereNom() : null;
            securityContext.setUser(user.getId(), user.getNomComplet(), user.getRole(), matiere);

            if (user.getRole() == Role.ADMIN) {
                navigator.show(new AdminDashboardScreen(appContext, navigator, user).build(),
                        "Tableau de bord - Administrateur");
            } else if (user.getRole() == Role.ENSEIGNANT) {
                navigator.show(new EnseignantDashboardScreen(appContext, navigator, user).build(),
                        "Tableau de bord - Enseignant");
            } else if (user.getRole() == Role.ETUDIANT) {
                navigator.show(new EtudiantDashboardScreen(appContext, navigator, user).build(),
                        "Tableau de bord - Étudiant");
            } else {
                erreur.setText("Rôle non reconnu.");
            }
        } catch (IllegalStateException ex) {
            erreur.setText(ex.getMessage());
        }
    }
}
