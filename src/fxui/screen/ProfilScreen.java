package fxui.screen;

import Auhentifcation.AuthenticationService;
import MVC.User;
import fxui.ScreenUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/** Écran "Mon Profil" : infos du compte + changement de mot de passe. Remplace Auhentifcation.ProfilView. */
public class ProfilScreen {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AuthenticationService authenticationService;
    private final Runnable onBack;

    public ProfilScreen(AuthenticationService authenticationService, Runnable onBack) {
        this.authenticationService = authenticationService;
        this.onBack = onBack;
    }

    public Parent build() {
        User user = authenticationService.getCurrentUser().orElse(null);

        Label titre = new Label("Mon Profil");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        VBox root = new VBox(20, header);
        root.setPadding(new Insets(10));

        if (user == null) {
            root.getChildren().add(new Label("Session expirée."));
            return root;
        }

        GridPane infos = new GridPane();
        infos.setHgap(10);
        infos.setVgap(8);
        infos.setPadding(new Insets(10, 30, 10, 30));
        int row = 0;
        infos.addRow(row++, new Label("ID :"), new Label(String.valueOf(user.getId())));
        infos.addRow(row++, new Label("Nom :"), new Label(user.getNom()));
        infos.addRow(row++, new Label("Prénom :"), new Label(user.getPrenom()));
        infos.addRow(row++, new Label("Email :"), new Label(user.getEmail() == null ? "-" : user.getEmail()));
        infos.addRow(row++, new Label("Login :"), new Label(user.getLogin()));
        infos.addRow(row++, new Label("Rôle :"), new Label(user.getRole().getLibelle()));
        infos.addRow(row++, new Label("Statut :"), new Label(user.isActif() ? "Actif" : "Désactivé"));
        infos.addRow(row++, new Label("Dernière connexion :"),
                new Label(user.getDerniereConnexion() == null ? "-" : DTF.format(user.getDerniereConnexion().toLocalDateTime())));

        Label sousTitre = new Label("Changer mon mot de passe");
        sousTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        PasswordField ancien = new PasswordField();
        ancien.setPromptText("Ancien mot de passe");
        PasswordField nouveau = new PasswordField();
        nouveau.setPromptText("Nouveau mot de passe (min 6 caractères)");
        PasswordField confirmation = new PasswordField();
        confirmation.setPromptText("Confirmer le nouveau mot de passe");

        Button changer = new Button("Changer le mot de passe");
        changer.getStyleClass().addAll("btn", "btn-primary");
        Label message = new Label();

        changer.setOnAction(e -> {
            message.setText("");
            if (!nouveau.getText().equals(confirmation.getText())) {
                message.setStyle("-fx-text-fill: red;");
                message.setText("Les mots de passe ne correspondent pas.");
                return;
            }
            try {
                authenticationService.changePassword(ancien.getText(), nouveau.getText());
                ancien.clear();
                nouveau.clear();
                confirmation.clear();
                ScreenUtils.infoAlert("Mot de passe changé avec succès !");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(ex.getMessage());
            }
        });

        GridPane motDePasse = new GridPane();
        motDePasse.setHgap(10);
        motDePasse.setVgap(8);
        motDePasse.setPadding(new Insets(10, 30, 10, 30));
        motDePasse.addRow(0, new Label("Ancien :"), ancien);
        motDePasse.addRow(1, new Label("Nouveau :"), nouveau);
        motDePasse.addRow(2, new Label("Confirmation :"), confirmation);
        motDePasse.add(changer, 1, 3);

        root.getChildren().addAll(infos, sousTitre, motDePasse, message);
        return root;
    }
}
