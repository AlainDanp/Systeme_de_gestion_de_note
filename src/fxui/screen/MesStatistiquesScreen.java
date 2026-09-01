package fxui.screen;

import gestion_Note.model.Note;
import gestion_Note.service.NoteService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/** Vue lecture-seule "Mes Statistiques" pour l'étudiant. Remplace MenuEtudiantView.voirMesStatistiques(). */
public class MesStatistiquesScreen {

    private final NoteService noteService;
    private final Integer etudiantId;
    private final Runnable onBack;

    public MesStatistiquesScreen(NoteService noteService, Integer etudiantId, Runnable onBack) {
        this.noteService = noteService;
        this.etudiantId = etudiantId;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Mes Statistiques");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        VBox root = new VBox(20, header);
        root.setPadding(new Insets(10));

        List<Note> notes = noteService.listerNotesParEtudiant(etudiantId);

        if (notes.isEmpty()) {
            root.getChildren().add(new Label("Aucune donnée disponible."));
            return root;
        }

        double somme = 0;
        double min = 20;
        double max = 0;
        for (Note n : notes) {
            somme += n.getValeur();
            if (n.getValeur() < min) min = n.getValeur();
            if (n.getValeur() > max) max = n.getValeur();
        }
        double moyenne = somme / notes.size();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 30, 10, 30));
        grid.addRow(0, new Label("Nombre de notes :"), new Label(String.valueOf(notes.size())));
        grid.addRow(1, new Label("Moyenne générale :"), new Label(String.format("%.2f/20", moyenne)));
        grid.addRow(2, new Label("Note la plus basse :"), new Label(String.format("%.2f/20", min)));
        grid.addRow(3, new Label("Note la plus haute :"), new Label(String.format("%.2f/20", max)));

        Label appreciationTitre = new Label("Appréciation :");
        appreciationTitre.setStyle("-fx-font-weight: bold;");

        String texte;
        if (moyenne >= 16) texte = "Excellent ! Continuez ainsi !";
        else if (moyenne >= 14) texte = "Très bien ! Bon travail !";
        else if (moyenne >= 12) texte = "Bien ! Vous progressez !";
        else if (moyenne >= 10) texte = "Assez bien. Vous pouvez mieux faire !";
        else texte = "Il faut travailler davantage pour progresser.";

        Label appreciation = new Label(texte);
        appreciation.setPadding(new Insets(0, 0, 0, 30));

        root.getChildren().addAll(grid, appreciationTitre, appreciation);
        return root;
    }
}
