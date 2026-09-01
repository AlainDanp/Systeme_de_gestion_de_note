package fxui.screen;

import fxui.Navigator;
import fxui.ScreenUtils;
import gestion_Matiere.model.Matiere;
import gestion_Matiere.service.EnseignantInfo;
import gestion_Matiere.service.MatiereService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * Écran CRUD "Matière", gabarit pour les 5 écrans CRUD suivants (Classe, Enseignant,
 * Étudiant, Note, Bulletin). Remplace gestion_Matiere.vue.MatiereView.menu().
 */
public class MatiereScreen {

    private final MatiereService matiereService;
    private final Navigator navigator;
    private final Runnable onBack;
    private final TableView<Matiere> table = new TableView<>();

    public MatiereScreen(MatiereService matiereService, Navigator navigator, Runnable onBack) {
        this.matiereService = matiereService;
        this.navigator = navigator;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Gestion des Matières");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        configurerColonnes();
        rafraichir();

        Button nouvelle = new Button("Nouvelle matière");
        nouvelle.getStyleClass().addAll("btn", "btn-success");
        nouvelle.setOnAction(e -> ouvrirFormulaire(null));

        Button modifier = new Button("Modifier");
        modifier.getStyleClass().addAll("btn", "btn-warning");
        modifier.setOnAction(e -> {
            Matiere selection = table.getSelectionModel().getSelectedItem();
            if (selection == null) {
                ScreenUtils.showError("Sélectionnez une matière à modifier.");
                return;
            }
            ouvrirFormulaire(selection);
        });

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().addAll("btn", "btn-danger");
        supprimer.setOnAction(e -> supprimerSelection());

        Button actualiser = new Button("Actualiser");
        actualiser.getStyleClass().addAll("btn", "btn-primary");
        actualiser.setOnAction(e -> rafraichir());

        FlowPane actions = new FlowPane(10, 10, nouvelle, modifier, supprimer, actualiser);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("action-bar");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(table);
        root.setBottom(actions);
        return root;
    }

    private void configurerColonnes() {
        TableColumn<Matiere, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(300);

        TableColumn<Matiere, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEnseignantNomComplet()));
        colEnseignant.setPrefWidth(300);

        TableColumn<Matiere, String> colClasses = new TableColumn<>("Classes");
        colClasses.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                String.join(", ", matiereService.listerClassesConcernees(cell.getValue().getNom()))));
        colClasses.setPrefWidth(250);

        table.getColumns().setAll(List.of(colNom, colEnseignant, colClasses));
    }

    private void rafraichir() {
        ObservableList<Matiere> data = FXCollections.observableArrayList(matiereService.listerToutesLesMatieres());
        table.setItems(data);
        table.refresh();
    }

    private void supprimerSelection() {
        Matiere selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            ScreenUtils.showError("Sélectionnez une matière à supprimer.");
            return;
        }
        int nbNotes = matiereService.compterNotesMatiere(selection.getNom());
        String message = "Supprimer la matière \"" + selection.getNom() + "\" ?";
        if (nbNotes > 0) {
            message += " (" + nbNotes + " note(s) y sont associées, la suppression sera refusée par le système)";
        }
        if (!ScreenUtils.showConfirm(message)) {
            return;
        }
        try {
            matiereService.supprimerMatiere(selection.getNom());
            rafraichir();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    /** @param existante null pour une création, sinon la matière à modifier. */
    private void ouvrirFormulaire(Matiere existante) {
        boolean edition = existante != null;
        List<EnseignantInfo> enseignants = matiereService.listerEnseignantsDisponibles();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(edition ? "Modifier la matière" : "Nouvelle matière");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nomField = new TextField(edition ? existante.getNom() : "");
        nomField.setDisable(edition);

        ChoiceBox<EnseignantInfo> enseignantChoice = new ChoiceBox<>(FXCollections.observableArrayList(enseignants));
        enseignantChoice.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(EnseignantInfo info) {
                return info == null ? "" : info.toString();
            }
            @Override
            public EnseignantInfo fromString(String string) {
                return null;
            }
        });
        if (edition) {
            enseignants.stream()
                    .filter(info -> info.getId().equals(existante.getIdEnseignant()))
                    .findFirst()
                    .ifPresent(enseignantChoice::setValue);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Nom :"), nomField);
        grid.addRow(1, new Label("Enseignant :"), enseignantChoice);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        EnseignantInfo enseignantChoisi = enseignantChoice.getValue();
        if (nomField.getText().isBlank() || enseignantChoisi == null) {
            ScreenUtils.showError("Le nom et l'enseignant sont obligatoires.");
            return;
        }

        try {
            if (edition) {
                existante.setIdEnseignant(enseignantChoisi.getId());
                matiereService.modifierMatiere(existante);
            } else {
                Matiere nouvelle = new Matiere(nomField.getText(), enseignantChoisi.getId());
                matiereService.creerMatiere(nouvelle);
            }
            rafraichir();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }
}
