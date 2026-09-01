package fxui.screen;

import fxui.ScreenUtils;
import gestion_Classe.model.Classe;
import gestion_Classe.service.ClasseService;
import gestion_Etudiant.model.Etudiant;
import gestion_Etudiant.service.EtudiantService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
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
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Écran CRUD "Étudiant". Remplace gestion_Etudiant.vue.EtudiantView.menu(). */
public class EtudiantScreen {

    private final EtudiantService etudiantService;
    private final ClasseService classeService;
    private final DataSource dataSource;
    private final Runnable onBack;
    private final TableView<Etudiant> table = new TableView<>();
    private final ChoiceBox<Classe> filtreClasse = new ChoiceBox<>();

    public EtudiantScreen(EtudiantService etudiantService, ClasseService classeService,
                           DataSource dataSource, Runnable onBack) {
        this.etudiantService = etudiantService;
        this.classeService = classeService;
        this.dataSource = dataSource;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Gestion des Étudiants");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        configurerColonnes();
        rafraichir();

        filtreClasse.setItems(FXCollections.observableArrayList(classeService.listerToutesLesClasses()));
        filtreClasse.setConverter(classeConverter());
        filtreClasse.setPrefWidth(150);
        Button rechercherClasse = new Button("Rechercher");
        rechercherClasse.setOnAction(e -> filtrerParClasse());
        Button toutes = new Button("Tous les étudiants");
        toutes.setOnAction(e -> rafraichir());

        HBox filtres = new HBox(8, new Label("Filtrer par classe :"), filtreClasse, rechercherClasse, toutes);
        filtres.setAlignment(Pos.CENTER_LEFT);
        filtres.setPadding(new Insets(0, 10, 10, 10));

        VBox top = new VBox(header, filtres);

        Button nouveau = new Button("Nouvel étudiant");
        nouveau.getStyleClass().addAll("btn", "btn-success");
        nouveau.setOnAction(e -> ouvrirFormulaire(null));

        Button modifier = new Button("Modifier");
        modifier.getStyleClass().addAll("btn", "btn-warning");
        modifier.setOnAction(e -> avecSelection(this::ouvrirFormulaire));

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().addAll("btn", "btn-danger");
        supprimer.setOnAction(e -> avecSelection(this::supprimer));

        Button toggleActif = new Button("Activer/Désactiver");
        toggleActif.getStyleClass().addAll("btn", "btn-info");
        toggleActif.setOnAction(e -> avecSelection(this::toggleActif));

        Button resetPassword = new Button("Réinitialiser mot de passe");
        resetPassword.getStyleClass().addAll("btn", "btn-maroon");
        resetPassword.setOnAction(e -> avecSelection(this::resetPassword));

        HBox actions = new HBox(10, nouveau, modifier, supprimer, toggleActif, resetPassword);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        root.setBottom(actions);
        return root;
    }

    private void configurerColonnes() {
        TableColumn<Etudiant, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Etudiant, String> colPrenom = new TableColumn<>("Prénom");
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Etudiant, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Etudiant, String> colLogin = new TableColumn<>("Login");
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn<Etudiant, String> colClasse = new TableColumn<>("Classe");
        colClasse.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getClasseNiveau()));

        TableColumn<Etudiant, String> colActif = new TableColumn<>("Actif");
        colActif.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().isActif() ? "Oui" : "Non"));

        table.getColumns().setAll(List.of(colNom, colPrenom, colEmail, colLogin, colClasse, colActif));
    }

    private void rafraichir() {
        ObservableList<Etudiant> data = FXCollections.observableArrayList(etudiantService.listerTousLesEtudiants());
        table.setItems(data);
        table.refresh();
    }

    private void filtrerParClasse() {
        if (filtreClasse.getValue() == null) {
            ScreenUtils.showError("Sélectionnez une classe.");
            return;
        }
        ObservableList<Etudiant> data = FXCollections.observableArrayList(
                etudiantService.listerEtudiantsParClasse(filtreClasse.getValue().getIdClasse()));
        table.setItems(data);
        table.refresh();
    }

    private void avecSelection(Consumer<Etudiant> action) {
        Etudiant selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            ScreenUtils.showError("Sélectionnez un étudiant.");
            return;
        }
        action.accept(selection);
    }

    private void toggleActif(Etudiant etudiant) {
        try {
            etudiantService.toggleActif(etudiant.getIdEtudiant());
            rafraichir();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void resetPassword(Etudiant etudiant) {
        if (!ScreenUtils.showConfirm("Réinitialiser le mot de passe de " + etudiant.getNomComplet() + " ?")) {
            return;
        }
        try {
            String nouveau = etudiantService.resetPassword(etudiant.getIdEtudiant());
            ScreenUtils.infoAlert("Nouveau mot de passe (à communiquer, il ne sera plus affiché) :\n" + nouveau);
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void supprimer(Etudiant etudiant) {
        if (!ScreenUtils.showConfirm("Supprimer l'étudiant \"" + etudiant.getNomComplet() + "\" ?")) {
            return;
        }
        try (Connection c = dataSource.getConnection()) {
            etudiantService.supprimerEtudiant(c, etudiant.getIdEtudiant());
            rafraichir();
        } catch (SQLException ex) {
            ScreenUtils.showError("Erreur de connexion : " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void ouvrirFormulaire(Etudiant existant) {
        boolean edition = existant != null;
        List<Classe> classes = classeService.listerToutesLesClasses();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(edition ? "Modifier l'étudiant" : "Nouvel étudiant");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nomField = new TextField(edition ? existant.getNom() : "");
        TextField prenomField = new TextField(edition ? existant.getPrenom() : "");
        TextField emailField = new TextField(edition && existant.getEmail() != null ? existant.getEmail() : "");
        TextField loginField = new TextField(edition ? existant.getLogin() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(edition ? "(non modifiable ici)" : "Mot de passe initial");
        passwordField.setDisable(edition);

        List<Classe> options = new ArrayList<>();
        options.add(null);
        options.addAll(classes);
        ChoiceBox<Classe> classeChoice = new ChoiceBox<>(FXCollections.observableArrayList(options));
        classeChoice.setConverter(classeConverter());
        if (edition && existant.getClasseId() != null) {
            classes.stream()
                    .filter(cl -> cl.getIdClasse().equals(existant.getClasseId()))
                    .findFirst()
                    .ifPresent(classeChoice::setValue);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Nom :"), nomField);
        grid.addRow(1, new Label("Prénom :"), prenomField);
        grid.addRow(2, new Label("Email :"), emailField);
        grid.addRow(3, new Label("Login :"), loginField);
        grid.addRow(4, new Label("Mot de passe :"), passwordField);
        grid.addRow(5, new Label("Classe :"), classeChoice);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        Integer classeId = classeChoice.getValue() == null ? null : classeChoice.getValue().getIdClasse();

        try {
            if (edition) {
                existant.setNom(nomField.getText());
                existant.setPrenom(prenomField.getText());
                existant.setEmail(emailField.getText().isBlank() ? null : emailField.getText());
                existant.setLogin(loginField.getText());
                existant.setClasseId(classeId);
                etudiantService.modifierEtudiant(existant);
            } else {
                Etudiant nouveau = new Etudiant(null, nomField.getText(), prenomField.getText(), classeId);
                nouveau.setEmail(emailField.getText().isBlank() ? null : emailField.getText());
                nouveau.setLogin(loginField.getText());
                etudiantService.creerEtudiant(nouveau, passwordField.getText());
            }
            rafraichir();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private StringConverter<Classe> classeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Classe classe) {
                return classe == null ? "(aucune)" : classe.getNiveau();
            }
            @Override
            public Classe fromString(String string) {
                return null;
            }
        };
    }
}
