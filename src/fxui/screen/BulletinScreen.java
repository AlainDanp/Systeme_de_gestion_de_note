package fxui.screen;

import fxui.ScreenUtils;
import gestion_Bulletin.model.Bulletin;
import gestion_Bulletin.model.NoteDetail;
import gestion_Bulletin.service.BulletinService;
import javafx.collections.FXCollections;
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
import javafx.scene.layout.VBox;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Écran CRUD "Bulletin". Remplace gestion_Bulletin.vue.BulletinView.menu(). */
public class BulletinScreen {

    private final BulletinService bulletinService;
    private final DataSource dataSource;
    private final Runnable onBack;
    private final TableView<Bulletin> table = new TableView<>();
    private final TextField filtreEtudiantId = new TextField();

    public BulletinScreen(BulletinService bulletinService, DataSource dataSource, Runnable onBack) {
        this.bulletinService = bulletinService;
        this.dataSource = dataSource;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Gestion des Bulletins");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button retour = new Button("Retour");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));

        configurerColonnes();

        filtreEtudiantId.setPromptText("ID étudiant");
        filtreEtudiantId.setPrefWidth(100);
        Button rechercher = new Button("Rechercher");
        rechercher.setOnAction(e -> listerParEtudiant());
        Button toutes = new Button("Tous les bulletins");
        toutes.setOnAction(e -> rafraichir());

        HBox filtres = new HBox(8, new Label("Filtrer par étudiant :"), filtreEtudiantId, rechercher, toutes);
        filtres.setAlignment(Pos.CENTER_LEFT);
        filtres.setPadding(new Insets(0, 10, 10, 10));

        VBox top = new VBox(header, filtres);

        rafraichir();

        Button generer = new Button("Générer");
        generer.setOnAction(e -> genererBulletin());

        Button creerManuel = new Button("Créer manuel");
        creerManuel.setOnAction(e -> creerManuel());

        Button modifier = new Button("Modifier");
        modifier.setOnAction(e -> avecSelection(this::modifier));

        Button supprimer = new Button("Supprimer");
        supprimer.setOnAction(e -> avecSelection(this::supprimer));

        Button voirDetail = new Button("Voir détail des notes");
        voirDetail.setOnAction(e -> avecSelection(this::voirDetail));

        HBox actions = new HBox(10, generer, creerManuel, modifier, supprimer, voirDetail);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        root.setBottom(actions);
        return root;
    }

    private void configurerColonnes() {
        TableColumn<Bulletin, String> colEtudiant = new TableColumn<>("Étudiant");
        colEtudiant.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEtudiantNomComplet()));

        TableColumn<Bulletin, String> colPeriode = new TableColumn<>("Période");
        colPeriode.setCellValueFactory(new PropertyValueFactory<>("periode"));

        TableColumn<Bulletin, String> colMoyenne = new TableColumn<>("Moyenne");
        colMoyenne.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getMoyenne() == null ? "-" : String.valueOf(cell.getValue().getMoyenne())));

        TableColumn<Bulletin, String> colMoyenneClasse = new TableColumn<>("Moyenne classe");
        colMoyenneClasse.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getMoyennDelaClasse() == null ? "-" : String.valueOf(cell.getValue().getMoyennDelaClasse())));

        table.getColumns().setAll(List.of(colEtudiant, colPeriode, colMoyenne, colMoyenneClasse));
    }

    private void rafraichir() {
        table.setItems(FXCollections.observableArrayList(bulletinService.listerTousLesBulletins()));
    }

    private Integer lireEtudiantId() {
        try {
            return Integer.parseInt(filtreEtudiantId.getText().trim());
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("L'ID étudiant doit être un entier.");
            return null;
        }
    }

    private void listerParEtudiant() {
        Integer id = lireEtudiantId();
        if (id == null) return;
        try {
            table.setItems(FXCollections.observableArrayList(bulletinService.listerParEtudiant(id)));
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void avecSelection(Consumer<Bulletin> action) {
        Bulletin selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            ScreenUtils.showError("Sélectionnez un bulletin.");
            return;
        }
        action.accept(selection);
    }

    private void genererBulletin() {
        TextField etudiantIdField = new TextField();
        TextField periodeField = new TextField();
        periodeField.setPromptText("YYYY-T1 ou YYYY-S1");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("ID étudiant :"), etudiantIdField);
        grid.addRow(1, new Label("Période :"), periodeField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Générer un bulletin");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        try {
            Integer etudiantId = Integer.parseInt(etudiantIdField.getText().trim());
            Bulletin bulletin = bulletinService.genererEtEnregistrerBulletin(etudiantId, periodeField.getText().trim());
            table.setItems(FXCollections.observableArrayList(bulletinService.listerParEtudiant(etudiantId)));
            ScreenUtils.infoAlert("Bulletin généré : moyenne = "
                    + (bulletin.getMoyenne() == null ? "-" : bulletin.getMoyenne())
                    + ", moyenne classe = "
                    + (bulletin.getMoyennDelaClasse() == null ? "-" : bulletin.getMoyennDelaClasse()));
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("L'ID étudiant doit être un entier.");
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void creerManuel() {
        TextField etudiantIdField = new TextField();
        TextField periodeField = new TextField();
        periodeField.setPromptText("YYYY-T1 ou YYYY-S1");
        TextField moyenneField = new TextField();
        TextField moyenneClasseField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("ID étudiant :"), etudiantIdField);
        grid.addRow(1, new Label("Période :"), periodeField);
        grid.addRow(2, new Label("Moyenne :"), moyenneField);
        grid.addRow(3, new Label("Moyenne classe :"), moyenneClasseField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Créer un bulletin manuellement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        try {
            Integer etudiantId = Integer.parseInt(etudiantIdField.getText().trim());
            Double moyenne = moyenneField.getText().isBlank() ? null : Double.parseDouble(moyenneField.getText().trim().replace(',', '.'));
            Double moyenneClasse = moyenneClasseField.getText().isBlank() ? null : Double.parseDouble(moyenneClasseField.getText().trim().replace(',', '.'));

            Bulletin bulletin = new Bulletin();
            bulletin.setEtudiantId(etudiantId);
            bulletin.setPeriode(periodeField.getText());
            bulletin.setMoyenne(moyenne);
            bulletin.setMoyennDelaClasse(moyenneClasse);

            bulletinService.creeBulletin(bulletin);
            table.setItems(FXCollections.observableArrayList(bulletinService.listerParEtudiant(etudiantId)));
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("ID étudiant et moyennes doivent être numériques.");
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void modifier(Bulletin bulletin) {
        TextField moyenneField = new TextField(bulletin.getMoyenne() == null ? "" : String.valueOf(bulletin.getMoyenne()));
        TextField moyenneClasseField = new TextField(bulletin.getMoyennDelaClasse() == null ? "" : String.valueOf(bulletin.getMoyennDelaClasse()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Moyenne :"), moyenneField);
        grid.addRow(1, new Label("Moyenne classe :"), moyenneClasseField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le bulletin");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultat = dialog.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        try {
            bulletin.setMoyenne(moyenneField.getText().isBlank() ? null : Double.parseDouble(moyenneField.getText().trim().replace(',', '.')));
            bulletin.setMoyennDelaClasse(moyenneClasseField.getText().isBlank() ? null : Double.parseDouble(moyenneClasseField.getText().trim().replace(',', '.')));
            bulletinService.update(bulletin);
            listerParEtudiant();
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("Les moyennes doivent être numériques.");
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void supprimer(Bulletin bulletin) {
        if (!ScreenUtils.showConfirm("Supprimer le bulletin de " + bulletin.getEtudiantNomComplet()
                + " (" + bulletin.getPeriode() + ") ?")) {
            return;
        }
        try (Connection c = dataSource.getConnection()) {
            bulletinService.delete(c, bulletin.getId());
            table.getItems().remove(bulletin);
        } catch (SQLException ex) {
            ScreenUtils.showError("Erreur de connexion : " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void voirDetail(Bulletin bulletin) {
        List<NoteDetail> details = bulletinService.getNotesAvecEnseignants(bulletin.getEtudiantId(), bulletin.getPeriode());

        TableView<NoteDetail> detailTable = new TableView<>();
        TableColumn<NoteDetail, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        TableColumn<NoteDetail, Number> colValeur = new TableColumn<>("Valeur");
        colValeur.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getValeur()));
        TableColumn<NoteDetail, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEnseignantNomComplet()));
        detailTable.getColumns().setAll(List.of(colMatiere, colValeur, colEnseignant));
        detailTable.setItems(FXCollections.observableArrayList(details));
        detailTable.setPrefHeight(250);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Détail des notes - " + bulletin.getEtudiantNomComplet() + " (" + bulletin.getPeriode() + ")");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(detailTable);
        dialog.showAndWait();
    }
}
