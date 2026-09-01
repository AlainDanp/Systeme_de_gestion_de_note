package fxui.screen;

import Auhentifcation.AuthenticationService;
import MVC.Role;
import MVC.SecurityContext;
import MVC.User;
import core.AppContext;
import fxui.Navigator;
import fxui.ScreenUtils;
import gestion_Enseignant.model.Enseignant;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Optional;

/** Écran de connexion (refonte Figma) : panneau illustré à gauche, formulaire à droite. */
public class LoginScreen {

    private final AppContext appContext;
    private final Navigator navigator;

    public LoginScreen(AppContext appContext, Navigator navigator) {
        this.appContext = appContext;
        this.navigator = navigator;
    }

    public Parent build() {
        VBox left = buildIllustrationPane();
        VBox right = buildFormPane();
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox root = new HBox(left, right);
        root.getStyleClass().add("login-root");
        return root;
    }

    /** Silhouettes stylisées en formes vectorielles, en attendant l'export de l'illustration Figma. */
    private VBox buildIllustrationPane() {
        VBox pane = new VBox(16);
        pane.getStyleClass().add("login-left");
        pane.setAlignment(Pos.CENTER);

        Pane figures = new Pane();
        figures.setPrefSize(260, 220);

        Circle decoHaut = new Circle(30, 30, 22, Color.web("#3E5D82"));
        Circle decoBas = new Circle(230, 190, 16, Color.web("#3E5D82"));

        Rectangle corps1 = new Rectangle(55, 90, 70, 110);
        corps1.setArcWidth(40);
        corps1.setArcHeight(40);
        corps1.setFill(Color.web("#E8B98F"));
        Circle tete1 = new Circle(90, 75, 26, Color.web("#F1C9A4"));

        Rectangle corps2 = new Rectangle(140, 70, 80, 130);
        corps2.setArcWidth(40);
        corps2.setArcHeight(40);
        corps2.setFill(Color.web("#CFE0EC"));
        Circle tete2 = new Circle(180, 55, 28, Color.web("#F1C9A4"));

        figures.getChildren().addAll(decoHaut, decoBas, corps1, tete1, corps2, tete2);

        Label caption = new Label("Espace de connexion sécurisé");
        caption.getStyleClass().add("login-caption");

        pane.getChildren().addAll(figures, caption);
        return pane;
    }

    private VBox buildFormPane() {
        Label bienvenue = new Label("Bienvenue sur");
        bienvenue.getStyleClass().add("login-welcome");

        Label brand = new Label("Système de Gestion des Notes");
        brand.getStyleClass().add("login-brand");
        brand.setWrapText(true);
        brand.setMaxWidth(320);

        Label heading = new Label("Connexion");
        heading.getStyleClass().add("login-heading");

        Label loginLabel = new Label("E-mail ou nom d'utilisateur *");
        loginLabel.getStyleClass().add("login-label");
        TextField loginField = new TextField();
        loginField.getStyleClass().add("login-field");
        loginField.setPromptText("Identifiant ou nom d'utilisateur");

        Label passwordLabel = new Label("Mot de passe *");
        passwordLabel.getStyleClass().add("login-label");
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("login-field");
        passwordField.setPromptText("Mot de passe");

        Hyperlink oublie = new Hyperlink("Mot de passe oublié ?");
        oublie.getStyleClass().add("login-link");
        oublie.setOnAction(e -> ScreenUtils.infoAlert(
                "Contactez votre administrateur pour réinitialiser votre mot de passe."));
        HBox oublieBox = new HBox(oublie);
        oublieBox.setAlignment(Pos.CENTER_RIGHT);
        oublieBox.setMaxWidth(320);

        Label erreur = new Label();
        erreur.getStyleClass().add("login-error");

        Button seConnecter = new Button("Connexion");
        seConnecter.getStyleClass().add("login-button");
        seConnecter.setDefaultButton(true);
        seConnecter.setOnAction(e -> tenterConnexion(loginField.getText(), passwordField.getText(), erreur));

        HBox buttonBox = new HBox(seConnecter);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(320);
        buttonBox.setPadding(new Insets(6, 0, 0, 0));

        VBox form = new VBox(8, loginLabel, loginField, passwordLabel, passwordField, oublieBox);
        form.setMaxWidth(320);
        form.setPadding(new Insets(20, 0, 20, 0));

        VBox right = new VBox(6, bienvenue, brand, heading, form, buttonBox, erreur);
        right.getStyleClass().add("login-right");
        right.setAlignment(Pos.CENTER_LEFT);
        return right;
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
