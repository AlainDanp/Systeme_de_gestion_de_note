package fxui.screen;

import fxui.EtudiantPicker;
import fxui.ScreenUtils;
import gestion_Etudiant.model.Etudiant;
import gestion_Etudiant.service.EtudiantService;
import gestion_Note.model.Note;
import gestion_Note.service.NoteService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Écran CRUD "Note". Remplace gestion_Note.vue.NoteView.menu(). */
public class NoteScreen {

    private final NoteService noteService;
    private final EtudiantService etudiantService;
    private final DataSource dataSource;
    private final Runnable onBack;
    private final TableView<Note> table = new TableView<>();

    private final ComboBox<Etudiant> filtreEtudiant;
    private final TextField filtrePeriode = new TextField();
    private final ChoiceBox<String> filtreMatiere = new ChoiceBox<>();

    public NoteScreen(NoteService noteService, EtudiantService etudiantService, DataSource dataSource, Runnable onBack) {
        this.noteService = noteService;
        this.etudiantService = etudiantService;
        this.dataSource = dataSource;
        this.onBack = onBack;
        this.filtreEtudiant = EtudiantPicker.build(etudiantService.listerTousLesEtudiants());
    }

    public Parent build() {
        Label titre = new Label("Gestion des Notes");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        configurerColonnes();
        filtrePeriode.setPromptText("Période (YYYY-T1)");
        filtrePeriode.setPrefWidth(130);
        filtreMatiere.setItems(FXCollections.observableArrayList(noteService.listerMatieres()));
        filtreMatiere.setPrefWidth(150);

        Button rechercherEtudiant = new Button("Rechercher");
        rechercherEtudiant.getStyleClass().add("btn-outline");
        rechercherEtudiant.setOnAction(e -> filtrerParEtudiant());

        Button rechercherPeriode = new Button("Rechercher");
        rechercherPeriode.getStyleClass().add("btn-outline");
        rechercherPeriode.setOnAction(e -> filtrerParPeriode());

        Button rechercherMatiere = new Button("Rechercher");
        rechercherMatiere.getStyleClass().add("btn-outline");
        rechercherMatiere.setOnAction(e -> filtrerParMatiere());

        Button etudiantEtPeriode = new Button("Étudiant + période");
        etudiantEtPeriode.getStyleClass().addAll("btn", "btn-outline");
        etudiantEtPeriode.setOnAction(e -> filtrerParEtudiantEtPeriode());

        Button moyenne = new Button("Calculer moyenne");
        moyenne.getStyleClass().addAll("btn", "btn-info");
        moyenne.setOnAction(e -> calculerMoyenne());

        Button toutes = new Button("Toutes les notes");
        toutes.getStyleClass().addAll("btn", "btn-primary");
        toutes.setOnAction(e -> rafraichir());

        FlowPane filtres = new FlowPane(12, 10,
                ScreenUtils.filterGroup("Étudiant", filtreEtudiant, rechercherEtudiant),
                ScreenUtils.filterGroup("Période", filtrePeriode, rechercherPeriode),
                ScreenUtils.filterGroup("Matière", filtreMatiere, rechercherMatiere),
                etudiantEtPeriode, moyenne, toutes);
        filtres.getStyleClass().add("filter-bar");

        VBox top = new VBox(header, filtres);

        rafraichir();

        Button nouvelle = new Button("Nouvelle note");
        nouvelle.getStyleClass().addAll("btn", "btn-success");
        nouvelle.setOnAction(e -> ouvrirFormulaire(null));

        Button modifier = new Button("Modifier");
        modifier.getStyleClass().addAll("btn", "btn-warning");
        modifier.setOnAction(e -> avecSelection(this::ouvrirFormulaire));

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().addAll("btn", "btn-danger");
        supprimer.setOnAction(e -> avecSelection(this::supprimer));

        FlowPane actions = new FlowPane(10, 10, nouvelle, modifier, supprimer);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("action-bar");

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
        table.refresh();
    }

    private Integer etudiantSelectionne() {
        Etudiant etudiant = filtreEtudiant.getValue();
        if (etudiant == null) {
            ScreenUtils.showError("Sélectionnez un étudiant dans la liste (recherche par nom/prénom).");
            return null;
        }
        return etudiant.getIdEtudiant();
    }

    private void filtrerParEtudiant() {
        Integer id = etudiantSelectionne();
        if (id == null) return;
        try {
            table.setItems(FXCollections.observableArrayList(noteService.listerNotesParEtudiant(id)));
            table.refresh();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void filtrerParEtudiantEtPeriode() {
        Integer id = etudiantSelectionne();
        if (id == null) return;
        try {
            table.setItems(FXCollections.observableArrayList(
                    noteService.listerNotesParEtudiantEtPeriode(id, filtrePeriode.getText().trim())));
            table.refresh();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void filtrerParPeriode() {
        try {
            table.setItems(FXCollections.observableArrayList(
                    noteService.listerNotesParPeriode(filtrePeriode.getText().trim())));
            table.refresh();
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
            table.refresh();
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void calculerMoyenne() {
        Integer id = etudiantSelectionne();
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

        ComboBox<Etudiant> etudiantChoice = EtudiantPicker.build(etudiantService.listerTousLesEtudiants());
        if (edition) {
            etudiantChoice.getItems().stream()
                    .filter(et -> et.getIdEtudiant().equals(existante.getEtudiantId()))
                    .findFirst()
                    .ifPresent(etudiantChoice::setValue);
            etudiantChoice.setDisable(true);
        }

        TextField periodeField = new TextField(edition ? existante.getPeriode() : "");
        periodeField.setPromptText("YYYY-T1 ou YYYY-S1");
        ChoiceBox<String> matiereChoice = new ChoiceBox<>(FXCollections.observableArrayList(noteService.listerMatieres()));
        if (edition) matiereChoice.setValue(existante.getMatiere());
        TextField valeurField = new TextField(edition ? String.valueOf(existante.getValeur()) : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Étudiant :"), etudiantChoice);
        grid.addRow(1, new Label("Période :"), periodeField);
        grid.addRow(2, new Label("Matière :"), matiereChoice);
        grid.addRow(3, new Label("Valeur (/20) :"), valeurField);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        Etudiant etudiant = etudiantChoice.getValue();
        if (etudiant == null) {
            ScreenUtils.showError("Sélectionnez un étudiant dans la liste.");
            return;
        }

        Double valeur;
        try {
            valeur = Double.parseDouble(valeurField.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("La valeur doit être numérique.");
            return;
        }

        try {
            if (edition) {
                existante.setPeriode(periodeField.getText());
                existante.setMatiere(matiereChoice.getValue());
                existante.setValeur(valeur);
                noteService.modifierNote(existante);
            } else {
                Note nouvelle = new Note(null, etudiant.getIdEtudiant(), periodeField.getText(), matiereChoice.getValue(), valeur, null);
                noteService.creeNote(nouvelle);
            }
            rafraichir();
        } catch (IllegalArgumentException | SecurityException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }
}
