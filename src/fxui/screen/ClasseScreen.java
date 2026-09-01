package fxui.screen;

import fxui.ScreenUtils;
import gestion_Classe.model.Classe;
import gestion_Classe.service.ClasseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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

/** Écran CRUD "Classe", même gabarit que MatiereScreen. Remplace gestion_Classe.vue.ClasseView.menu(). */
public class ClasseScreen {

    private final ClasseService classeService;
    private final DataSource dataSource;
    private final Runnable onBack;
    private final TableView<Classe> table = new TableView<>();

    public ClasseScreen(ClasseService classeService, DataSource dataSource, Runnable onBack) {
        this.classeService = classeService;
        this.dataSource = dataSource;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Gestion des Classes");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        configurerColonnes();
        rafraichir();

        Button nouvelle = new Button("Nouvelle classe");
        nouvelle.getStyleClass().addAll("btn", "btn-success");
        nouvelle.setOnAction(e -> ouvrirFormulaire(null));

        Button modifier = new Button("Modifier");
        modifier.getStyleClass().addAll("btn", "btn-warning");
        modifier.setOnAction(e -> avecSelection(this::ouvrirFormulaire));

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().addAll("btn", "btn-danger");
        supprimer.setOnAction(e -> avecSelection(this::supprimer));

        Button rafraichirEffectif = new Button("Rafraîchir nb élèves");
        rafraichirEffectif.getStyleClass().addAll("btn", "btn-info");
        rafraichirEffectif.setOnAction(e -> avecSelection(this::rafraichirEffectif));

        Button actualiser = new Button("Actualiser");
        actualiser.getStyleClass().addAll("btn", "btn-primary");
        actualiser.setOnAction(e -> rafraichir());

        HBox actions = new HBox(10, nouvelle, modifier, supprimer, rafraichirEffectif, actualiser);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(table);
        root.setBottom(actions);
        return root;
    }

    private void configurerColonnes() {
        TableColumn<Classe, String> colNiveau = new TableColumn<>("Niveau");
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colNiveau.setPrefWidth(300);

        TableColumn<Classe, Number> colEffectif = new TableColumn<>("Nombre d'élèves");
        colEffectif.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleIntegerProperty(
                        classeService.compterEtudiants(cell.getValue().getIdClasse())));
        colEffectif.setPrefWidth(300);

        table.getColumns().setAll(List.of(colNiveau, colEffectif));
    }

    private void rafraichir() {
        ObservableList<Classe> data = FXCollections.observableArrayList(classeService.listerToutesLesClasses());
        table.setItems(data);
        table.refresh();
    }

    private void avecSelection(java.util.function.Consumer<Classe> action) {
        Classe selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            ScreenUtils.showError("Sélectionnez une classe.");
            return;
        }
        action.accept(selection);
    }

    private void rafraichirEffectif(Classe classe) {
        try {
            classeService.rafraichirNombreEleves(classe.getIdClasse());
            rafraichir();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void supprimer(Classe classe) {
        int nbEtudiants = classeService.compterEtudiants(classe.getIdClasse());
        String message = "Supprimer la classe \"" + classe.getNiveau() + "\" ?";
        if (nbEtudiants > 0) {
            message += " (" + nbEtudiants + " étudiant(s) y sont rattachés, la suppression sera refusée)";
        }
        if (!ScreenUtils.showConfirm(message)) {
            return;
        }
        try (Connection c = dataSource.getConnection()) {
            classeService.supprimerClasse(c, classe.getIdClasse());
            rafraichir();
        } catch (SQLException ex) {
            ScreenUtils.showError("Erreur de connexion : " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void ouvrirFormulaire(Classe existante) {
        boolean edition = existante != null;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(edition ? "Modifier la classe" : "Nouvelle classe");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField niveauField = new TextField(edition ? existante.getNiveau() : "");
        TextField effectifField = new TextField(
                edition && existante.getNombreEleves() != null ? String.valueOf(existante.getNombreEleves()) : "0");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Niveau :"), niveauField);
        grid.addRow(1, new Label("Nombre d'élèves :"), effectifField);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        Integer effectif;
        try {
            effectif = effectifField.getText().isBlank() ? 0 : Integer.parseInt(effectifField.getText().trim());
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("Le nombre d'élèves doit être un entier.");
            return;
        }

        try {
            if (edition) {
                existante.setNiveau(niveauField.getText());
                existante.setNombreEleves(effectif);
                classeService.modifierClasse(existante);
            } else {
                Classe nouvelle = new Classe(null, niveauField.getText(), effectif);
                classeService.creerClasse(nouvelle);
            }
            rafraichir();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }
}
