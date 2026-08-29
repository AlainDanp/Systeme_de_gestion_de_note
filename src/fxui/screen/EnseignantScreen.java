package fxui.screen;

import fxui.ScreenUtils;
import gestion_Enseignant.model.Enseignant;
import gestion_Enseignant.service.EnseignantService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Écran CRUD "Enseignant". Remplace gestion_Enseignant.vue.EnseignantView.menu(). */
public class EnseignantScreen {

    private final EnseignantService enseignantService;
    private final DataSource dataSource;
    private final Runnable onBack;
    private final TableView<Enseignant> table = new TableView<>();

    public EnseignantScreen(EnseignantService enseignantService, DataSource dataSource, Runnable onBack) {
        this.enseignantService = enseignantService;
        this.dataSource = dataSource;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Gestion des Enseignants");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button retour = new Button("Retour");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));

        configurerColonnes();
        rafraichir();

        Button nouveau = new Button("Nouvel enseignant");
        nouveau.setOnAction(e -> ouvrirFormulaire(null));

        Button modifier = new Button("Modifier");
        modifier.setOnAction(e -> avecSelection(this::ouvrirFormulaire));

        Button supprimer = new Button("Supprimer");
        supprimer.setOnAction(e -> avecSelection(this::supprimer));

        Button toggleActif = new Button("Activer/Désactiver");
        toggleActif.setOnAction(e -> avecSelection(this::toggleActif));

        Button resetPassword = new Button("Réinitialiser mot de passe");
        resetPassword.setOnAction(e -> avecSelection(this::resetPassword));

        Button actualiser = new Button("Actualiser");
        actualiser.setOnAction(e -> rafraichir());

        HBox actions = new HBox(10, nouveau, modifier, supprimer, toggleActif, resetPassword, actualiser);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(table);
        root.setBottom(actions);
        return root;
    }

    private void configurerColonnes() {
        TableColumn<Enseignant, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Enseignant, String> colPrenom = new TableColumn<>("Prénom");
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Enseignant, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Enseignant, String> colLogin = new TableColumn<>("Login");
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn<Enseignant, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getMatiereNom()));

        TableColumn<Enseignant, String> colActif = new TableColumn<>("Actif");
        colActif.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().isActif() ? "Oui" : "Non"));

        table.getColumns().setAll(List.of(colNom, colPrenom, colEmail, colLogin, colMatiere, colActif));
    }

    private void rafraichir() {
        ObservableList<Enseignant> data = FXCollections.observableArrayList(enseignantService.listerTousLesEnseignants());
        table.setItems(data);
    }

    private void avecSelection(Consumer<Enseignant> action) {
        Enseignant selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            ScreenUtils.showError("Sélectionnez un enseignant.");
            return;
        }
        action.accept(selection);
    }

    private void toggleActif(Enseignant enseignant) {
        try {
            enseignantService.toggleActif(enseignant.getIdEnseignant());
            rafraichir();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void resetPassword(Enseignant enseignant) {
        if (!ScreenUtils.showConfirm("Réinitialiser le mot de passe de " + enseignant.getNomComplet() + " ?")) {
            return;
        }
        try {
            String nouveau = enseignantService.resetPassword(enseignant.getIdEnseignant());
            ScreenUtils.infoAlert("Nouveau mot de passe (à communiquer, il ne sera plus affiché) :\n" + nouveau);
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void supprimer(Enseignant enseignant) {
        if (!ScreenUtils.showConfirm("Supprimer l'enseignant \"" + enseignant.getNomComplet() + "\" ?")) {
            return;
        }
        try (Connection c = dataSource.getConnection()) {
            enseignantService.supprimerEnseignant(c, enseignant.getIdEnseignant());
            rafraichir();
        } catch (SQLException ex) {
            ScreenUtils.showError("Erreur de connexion : " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void ouvrirFormulaire(Enseignant existant) {
        boolean edition = existant != null;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(edition ? "Modifier l'enseignant" : "Nouvel enseignant");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nomField = new TextField(edition ? existant.getNom() : "");
        TextField prenomField = new TextField(edition ? existant.getPrenom() : "");
        TextField emailField = new TextField(edition ? existant.getEmail() : "");
        TextField loginField = new TextField(edition ? existant.getLogin() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(edition ? "(non modifiable ici)" : "Mot de passe initial");
        passwordField.setDisable(edition);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Nom :"), nomField);
        grid.addRow(1, new Label("Prénom :"), prenomField);
        grid.addRow(2, new Label("Email :"), emailField);
        grid.addRow(3, new Label("Login :"), loginField);
        grid.addRow(4, new Label("Mot de passe :"), passwordField);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        try {
            if (edition) {
                existant.setNom(nomField.getText());
                existant.setPrenom(prenomField.getText());
                existant.setEmail(emailField.getText());
                existant.setLogin(loginField.getText());
                enseignantService.modifierEnseignant(existant);
            } else {
                Enseignant nouveau = new Enseignant(null, nomField.getText(), prenomField.getText(), emailField.getText());
                nouveau.setLogin(loginField.getText());
                enseignantService.creerEnseignant(nouveau, passwordField.getText());
            }
            rafraichir();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }
}
