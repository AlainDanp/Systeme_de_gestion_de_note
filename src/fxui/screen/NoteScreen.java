package fxui.screen;

import fxui.ScreenUtils;
import gestion_Note.model.Note;
import gestion_Note.service.NoteService;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Écran CRUD "Note". Remplace gestion_Note.vue.NoteView.menu(). */
public class NoteScreen {

    private final NoteService noteService;
    private final DataSource dataSource;
    private final Runnable onBack;
    private final TableView<Note> table = new TableView<>();

    private final TextField filtreEtudiantId = new TextField();
    private final TextField filtrePeriode = new TextField();
    private final ChoiceBox<String> filtreMatiere = new ChoiceBox<>();

    public NoteScreen(NoteService noteService, DataSource dataSource, Runnable onBack) {
        this.noteService = noteService;
        this.dataSource = dataSource;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Gestion des Notes");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button retour = new Button("Retour");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));

        configurerColonnes();
        filtreEtudiantId.setPromptText("ID étudiant");
        filtreEtudiantId.setPrefWidth(90);
        filtrePeriode.setPromptText("Période (YYYY-T1)");
        filtrePeriode.setPrefWidth(130);
        filtreMatiere.setItems(FXCollections.observableArrayList(noteService.listerMatieres()));
        filtreMatiere.setPrefWidth(150);

        Button rechercherEtudiant = new Button("Rechercher");
        rechercherEtudiant.setOnAction(e -> filtrerParEtudiant());
        HBox ligneEtudiant = new HBox(8, new Label("Filtrer par étudiant :"), filtreEtudiantId, rechercherEtudiant);
        ligneEtudiant.setAlignment(Pos.CENTER_LEFT);

        Button rechercherPeriode = new Button("Rechercher");
        rechercherPeriode.setOnAction(e -> filtrerParPeriode());
        HBox lignePeriode = new HBox(8, new Label("Filtrer par période :"), filtrePeriode, rechercherPeriode);
        lignePeriode.setAlignment(Pos.CENTER_LEFT);

        Button rechercherMatiere = new Button("Rechercher");
        rechercherMatiere.setOnAction(e -> filtrerParMatiere());
        HBox ligneMatiere = new HBox(8, new Label("Filtrer par matière :"), filtreMatiere, rechercherMatiere);
        ligneMatiere.setAlignment(Pos.CENTER_LEFT);

        Button rechercherEtudiantPeriode = new Button("Rechercher");
        rechercherEtudiantPeriode.setOnAction(e -> filtrerParEtudiantEtPeriode());
        Button moyenne = new Button("Calculer moyenne");
        moyenne.setOnAction(e -> calculerMoyenne());
        HBox ligneCombinee = new HBox(8, new Label("Étudiant + période (mêmes champs ci-dessus) :"),
                rechercherEtudiantPeriode, moyenne);
        ligneCombinee.setAlignment(Pos.CENTER_LEFT);

        Button toutes = new Button("Toutes les notes");
        toutes.setOnAction(e -> rafraichir());
        HBox ligneToutes = new HBox(8, toutes);
        ligneToutes.setAlignment(Pos.CENTER_LEFT);

        VBox filtres = new VBox(6, ligneEtudiant, lignePeriode, ligneMatiere, ligneCombinee, ligneToutes);
        filtres.setPadding(new Insets(0, 10, 10, 10));

        VBox top = new VBox(header, filtres);

        rafraichir();

        Button nouvelle = new Button("Nouvelle note");
        nouvelle.setOnAction(e -> ouvrirFormulaire(null));

        Button modifier = new Button("Modifier");
        modifier.setOnAction(e -> avecSelection(this::ouvrirFormulaire));

        Button supprimer = new Button("Supprimer");
        supprimer.setOnAction(e -> avecSelection(this::supprimer));

        HBox actions = new HBox(10, nouvelle, modifier, supprimer);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        root.setBottom(actions);
        return root;
    }

    private void configurerColonnes() {
        TableColumn<Note, String> colEtudiant = new TableColumn<>("Étudiant");
        colEtudiant.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEtudiantNomComplet()));

        TableColumn<Note, String> colPeriode = new TableColumn<>("Période");
        colPeriode.setCellValueFactory(new PropertyValueFactory<>("periode"));

        TableColumn<Note, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));

        TableColumn<Note, Number> colValeur = new TableColumn<>("Valeur");
        colValeur.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getValeur()));

        TableColumn<Note, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEnseignantNomComplet()));

        table.getColumns().setAll(List.of(colEtudiant, colPeriode, colMatiere, colValeur, colEnseignant));
    }

    private void rafraichir() {
        table.setItems(FXCollections.observableArrayList(noteService.listerToutesLesNotes()));
    }

    private Integer lireEtudiantId() {
        try {
            return Integer.parseInt(filtreEtudiantId.getText().trim());
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("L'ID étudiant doit être un entier.");
            return null;
        }
    }

    private void filtrerParEtudiant() {
        Integer id = lireEtudiantId();
        if (id == null) return;
        try {
            table.setItems(FXCollections.observableArrayList(noteService.listerNotesParEtudiant(id)));
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void filtrerParEtudiantEtPeriode() {
        Integer id = lireEtudiantId();
        if (id == null) return;
        try {
            table.setItems(FXCollections.observableArrayList(
                    noteService.listerNotesParEtudiantEtPeriode(id, filtrePeriode.getText().trim())));
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void filtrerParPeriode() {
        try {
            table.setItems(FXCollections.observableArrayList(
                    noteService.listerNotesParPeriode(filtrePeriode.getText().trim())));
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void filtrerParMatiere() {
        String matiere = filtreMatiere.getValue();
        if (matiere == null) {
            ScreenUtils.showError("Sélectionnez une matière dans la liste.");
            return;
        }
        try {
            table.setItems(FXCollections.observableArrayList(noteService.listerNotesParMatiere(matiere)));
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void calculerMoyenne() {
        Integer id = lireEtudiantId();
        if (id == null) return;
        try {
            Double moyenne = noteService.calculerMoyenneEtudiant(id, filtrePeriode.getText().trim());
            ScreenUtils.infoAlert(moyenne == null
                    ? "Aucune note pour cet étudiant sur cette période."
                    : "Moyenne : " + moyenne + " / 20");
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void avecSelection(java.util.function.Consumer<Note> action) {
        Note selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            ScreenUtils.showError("Sélectionnez une note.");
            return;
        }
        action.accept(selection);
    }

    private void supprimer(Note note) {
        if (!ScreenUtils.showConfirm("Supprimer la note de " + note.getEtudiantNomComplet()
                + " en " + note.getMatiere() + " ?")) {
            return;
        }
        try (Connection c = dataSource.getConnection()) {
            noteService.supprimerNote(c, note.getId());
            rafraichir();
        } catch (SQLException ex) {
            ScreenUtils.showError("Erreur de connexion : " + ex.getMessage());
        } catch (IllegalArgumentException | SecurityException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void ouvrirFormulaire(Note existante) {
        boolean edition = existante != null;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(edition ? "Modifier la note" : "Nouvelle note");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField etudiantIdField = new TextField(edition ? String.valueOf(existante.getEtudiantId()) : "");
        etudiantIdField.setDisable(edition);
        TextField periodeField = new TextField(edition ? existante.getPeriode() : "");
        periodeField.setPromptText("YYYY-T1 ou YYYY-S1");
        ChoiceBox<String> matiereChoice = new ChoiceBox<>(FXCollections.observableArrayList(noteService.listerMatieres()));
        if (edition) matiereChoice.setValue(existante.getMatiere());
        TextField valeurField = new TextField(edition ? String.valueOf(existante.getValeur()) : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("ID étudiant :"), etudiantIdField);
        grid.addRow(1, new Label("Période :"), periodeField);
        grid.addRow(2, new Label("Matière :"), matiereChoice);
        grid.addRow(3, new Label("Valeur (/20) :"), valeurField);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        Integer etudiantId;
        Double valeur;
        try {
            etudiantId = edition ? existante.getEtudiantId() : Integer.parseInt(etudiantIdField.getText().trim());
            valeur = Double.parseDouble(valeurField.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("ID étudiant et valeur doivent être numériques.");
            return;
        }

        try {
            if (edition) {
                existante.setPeriode(periodeField.getText());
                existante.setMatiere(matiereChoice.getValue());
                existante.setValeur(valeur);
                noteService.modifierNote(existante);
            } else {
                Note nouvelle = new Note(null, etudiantId, periodeField.getText(), matiereChoice.getValue(), valeur, null);
                noteService.creeNote(nouvelle);
            }
            rafraichir();
        } catch (IllegalArgumentException | SecurityException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }
}
