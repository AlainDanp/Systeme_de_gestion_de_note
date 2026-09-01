package fxui.screen;

import fxui.BulletinPdfExporter;
import fxui.EtudiantPicker;
import fxui.ScreenUtils;
import gestion_Bulletin.model.Bulletin;
import gestion_Bulletin.model.NoteDetail;
import gestion_Bulletin.service.BulletinService;
import gestion_Etudiant.model.Etudiant;
import gestion_Etudiant.service.EtudiantService;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Écran CRUD "Bulletin". Remplace gestion_Bulletin.vue.BulletinView.menu(). */
public class BulletinScreen {

    private final BulletinService bulletinService;
    private final EtudiantService etudiantService;
    private final DataSource dataSource;
    private final Runnable onBack;
    private final TableView<Bulletin> table = new TableView<>();
    private final EtudiantPicker filtreEtudiant;

    public BulletinScreen(BulletinService bulletinService, EtudiantService etudiantService,
                           DataSource dataSource, Runnable onBack) {
        this.bulletinService = bulletinService;
        this.etudiantService = etudiantService;
        this.dataSource = dataSource;
        this.onBack = onBack;
        this.filtreEtudiant = new EtudiantPicker(etudiantService.listerTousLesEtudiants());
    }

    public Parent build() {
        Label titre = new Label("Gestion des Bulletins");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        configurerColonnes();

        Button rechercher = new Button("Rechercher");
        rechercher.getStyleClass().add("btn-outline");
        rechercher.setOnAction(e -> listerParEtudiant());
        Button toutes = new Button("Tous les bulletins");
        toutes.getStyleClass().addAll("btn", "btn-primary");
        toutes.setOnAction(e -> rafraichir());

        FlowPane filtres = new FlowPane(12, 10,
                ScreenUtils.filterGroup("Étudiant", filtreEtudiant.getNode(), rechercher), toutes);
        filtres.getStyleClass().add("filter-bar");

        VBox top = new VBox(header, filtres);

        rafraichir();

        Button generer = new Button("Générer");
        generer.getStyleClass().addAll("btn", "btn-success");
        generer.setOnAction(e -> genererBulletin());

        Button creerManuel = new Button("Créer manuel");
        creerManuel.getStyleClass().addAll("btn", "btn-info");
        creerManuel.setOnAction(e -> creerManuel());

        Button modifier = new Button("Modifier");
        modifier.getStyleClass().addAll("btn", "btn-warning");
        modifier.setOnAction(e -> avecSelection(this::modifier));

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().addAll("btn", "btn-danger");
        supprimer.setOnAction(e -> avecSelection(this::supprimer));

        Button voirDetail = new Button("Voir détail des notes");
        voirDetail.getStyleClass().addAll("btn", "btn-primary");
        voirDetail.setOnAction(e -> avecSelection(this::voirDetail));

        Button exportPdf = new Button("🖶 Exporter en PDF");
        exportPdf.getStyleClass().addAll("btn", "btn-secondary");
        exportPdf.setOnAction(e -> avecSelection(this::exporterPdf));

        FlowPane actions = new FlowPane(10, 10, generer, creerManuel, modifier, supprimer, voirDetail, exportPdf);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("action-bar");

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
        table.refresh();
    }

    private void listerParEtudiant() {
        Etudiant etudiant = filtreEtudiant.getValue();
        if (etudiant == null) {
            ScreenUtils.showError("Sélectionnez un étudiant dans la liste (recherche par nom/prénom).");
            return;
        }
        try {
            table.setItems(FXCollections.observableArrayList(bulletinService.listerParEtudiant(etudiant.getIdEtudiant())));
            table.refresh();
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
        EtudiantPicker etudiantChoice = new EtudiantPicker(etudiantService.listerTousLesEtudiants());
        TextField periodeField = new TextField();
        periodeField.setPromptText("YYYY-T1 ou YYYY-S1");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Étudiant :"), etudiantChoice.getNode());
        grid.addRow(1, new Label("Période :"), periodeField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Générer un bulletin");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
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

        try {
            Integer etudiantId = etudiant.getIdEtudiant();
            Bulletin bulletin = bulletinService.genererEtEnregistrerBulletin(etudiantId, periodeField.getText().trim());
            table.setItems(FXCollections.observableArrayList(bulletinService.listerParEtudiant(etudiantId)));
            table.refresh();
            ScreenUtils.infoAlert("Bulletin généré : moyenne = "
                    + (bulletin.getMoyenne() == null ? "-" : bulletin.getMoyenne())
                    + ", moyenne classe = "
                    + (bulletin.getMoyennDelaClasse() == null ? "-" : bulletin.getMoyennDelaClasse()));
        } catch (IllegalArgumentException | SecurityException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    private void creerManuel() {
        EtudiantPicker etudiantChoice = new EtudiantPicker(etudiantService.listerTousLesEtudiants());
        TextField periodeField = new TextField();
        periodeField.setPromptText("YYYY-T1 ou YYYY-S1");
        TextField moyenneField = new TextField();
        TextField moyenneClasseField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Étudiant :"), etudiantChoice.getNode());
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

        Etudiant etudiant = etudiantChoice.getValue();
        if (etudiant == null) {
            ScreenUtils.showError("Sélectionnez un étudiant dans la liste.");
            return;
        }

        try {
            Integer etudiantId = etudiant.getIdEtudiant();
            Double moyenne = moyenneField.getText().isBlank() ? null : Double.parseDouble(moyenneField.getText().trim().replace(',', '.'));
            Double moyenneClasse = moyenneClasseField.getText().isBlank() ? null : Double.parseDouble(moyenneClasseField.getText().trim().replace(',', '.'));

            Bulletin bulletin = new Bulletin();
            bulletin.setEtudiantId(etudiantId);
            bulletin.setPeriode(periodeField.getText());
            bulletin.setMoyenne(moyenne);
            bulletin.setMoyennDelaClasse(moyenneClasse);

            bulletinService.creeBulletin(bulletin);
            table.setItems(FXCollections.observableArrayList(bulletinService.listerParEtudiant(etudiantId)));
            table.refresh();
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("Les moyennes doivent être numériques.");
        } catch (IllegalArgumentException | SecurityException ex) {
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
            rafraichir();
        } catch (NumberFormatException ex) {
            ScreenUtils.showError("Les moyennes doivent être numériques.");
        } catch (IllegalArgumentException | SecurityException ex) {
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
        } catch (IllegalArgumentException | SecurityException ex) {
            ScreenUtils.showError(ex.getMessage());
        }
    }

    /** Génère le PDF du bulletin sélectionné, selon le gabarit fixe de {@link BulletinPdfExporter}. */
    private void exporterPdf(Bulletin bulletin) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter le bulletin en PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
        String nomFichier = ("bulletin_" + bulletin.getEtudiantNomComplet() + "_" + bulletin.getPeriode())
                .replaceAll("[^a-zA-Z0-9À-ÿ_-]", "_") + ".pdf";
        chooser.setInitialFileName(nomFichier);

        File fichier = chooser.showSaveDialog(table.getScene().getWindow());
        if (fichier == null) {
            return;
        }

        try {
            List<NoteDetail> notes = bulletinService.getNotesAvecEnseignants(bulletin.getEtudiantId(), bulletin.getPeriode());
            BulletinPdfExporter.export(fichier, bulletin, notes);
            ScreenUtils.infoAlert("Bulletin exporté : " + fichier.getAbsolutePath());
        } catch (IOException ex) {
            ScreenUtils.showError("Échec de l'export PDF : " + ex.getMessage());
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
