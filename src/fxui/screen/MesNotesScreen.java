package fxui.screen;

import gestion_Note.model.Note;
import gestion_Note.service.NoteService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.List;

/** Vue lecture-seule "Mes Notes" pour l'étudiant. Remplace MenuEtudiantView.voirMesNotes(). */
public class MesNotesScreen {

    private final NoteService noteService;
    private final Integer etudiantId;
    private final Runnable onBack;

    public MesNotesScreen(NoteService noteService, Integer etudiantId, Runnable onBack) {
        this.noteService = noteService;
        this.etudiantId = etudiantId;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Mes Notes");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        TableView<Note> table = new TableView<>();

        TableColumn<Note, String> colPeriode = new TableColumn<>("Période");
        colPeriode.setCellValueFactory(new PropertyValueFactory<>("periode"));

        TableColumn<Note, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));

        TableColumn<Note, Number> colValeur = new TableColumn<>("Note");
        colValeur.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getValeur()));

        TableColumn<Note, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEnseignantNomComplet()));

        table.getColumns().setAll(List.of(colPeriode, colMatiere, colValeur, colEnseignant));

        List<Note> notes = noteService.listerNotesParEtudiant(etudiantId);
        table.setItems(FXCollections.observableArrayList(notes));

        Label total = new Label(notes.isEmpty() ? "Aucune note enregistrée." : "Total : " + notes.size() + " note(s)");
        total.setPadding(new Insets(0, 10, 10, 10));

        BorderPane root = new BorderPane();
        root.setTop(new javafx.scene.layout.VBox(header, total));
        root.setCenter(table);
        return root;
    }
}
