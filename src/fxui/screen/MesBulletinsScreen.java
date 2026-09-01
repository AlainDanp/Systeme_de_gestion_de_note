package fxui.screen;

import gestion_Bulletin.model.Bulletin;
import gestion_Bulletin.service.BulletinService;
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
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Vue lecture-seule "Mes Bulletins" pour l'étudiant. Remplace MenuEtudiantView.voirMesBulletins(). */
public class MesBulletinsScreen {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BulletinService bulletinService;
    private final Integer etudiantId;
    private final Runnable onBack;

    public MesBulletinsScreen(BulletinService bulletinService, Integer etudiantId, Runnable onBack) {
        this.bulletinService = bulletinService;
        this.etudiantId = etudiantId;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Mes Bulletins");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        TableView<Bulletin> table = new TableView<>();

        TableColumn<Bulletin, String> colPeriode = new TableColumn<>("Période");
        colPeriode.setCellValueFactory(new PropertyValueFactory<>("periode"));

        TableColumn<Bulletin, String> colMoyenne = new TableColumn<>("Moyenne");
        colMoyenne.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                formatNote(cell.getValue().getMoyenne())));

        TableColumn<Bulletin, String> colMoyenneClasse = new TableColumn<>("Moyenne classe");
        colMoyenneClasse.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                formatNote(cell.getValue().getMoyennDelaClasse())));

        TableColumn<Bulletin, String> colPosition = new TableColumn<>("Position");
        colPosition.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                position(cell.getValue())));
        colPosition.setPrefWidth(200);

        TableColumn<Bulletin, String> colDate = new TableColumn<>("Généré le");
        colDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getCreatedAt() == null ? "" : DTF.format(cell.getValue().getCreatedAt().toLocalDateTime())));

        table.getColumns().setAll(List.of(colPeriode, colMoyenne, colMoyenneClasse, colPosition, colDate));

        List<Bulletin> bulletins = bulletinService.listerParEtudiant(etudiantId);
        table.setItems(FXCollections.observableArrayList(bulletins));

        Label total = new Label(bulletins.isEmpty() ? "Aucun bulletin disponible." : "Total : " + bulletins.size() + " bulletin(s)");
        total.setPadding(new Insets(0, 10, 10, 10));

        BorderPane root = new BorderPane();
        root.setTop(new VBox(header, total));
        root.setCenter(table);
        return root;
    }

    private String formatNote(Double note) {
        return note == null ? "N/A" : String.format("%.2f/20", note);
    }

    private String position(Bulletin b) {
        if (b.getMoyenne() == null || b.getMoyennDelaClasse() == null) {
            return "-";
        }
        double diff = b.getMoyenne() - b.getMoyennDelaClasse();
        String position = diff > 0 ? "au-dessus" : diff < 0 ? "en-dessous" : "égale à";
        return String.format("%.2f pts %s la moyenne", Math.abs(diff), position);
    }
}
