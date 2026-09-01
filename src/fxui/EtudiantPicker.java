package fxui;

import gestion_Etudiant.model.Etudiant;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Champ de recherche d'étudiant par nom/prénom : un TextField avec une liste de suggestions
 * affichée dans un popup pendant la saisie, en remplacement de la recherche par ID étudiant.
 * Implémenté "à la main" (TextField + Popup + ListView) plutôt qu'avec une ComboBox éditable :
 * remplacer les items d'une ComboBox pendant la frappe entre en conflit avec son propre
 * mécanisme de popup/sélection et empêche le filtrage de fonctionner de façon fiable.
 */
public final class EtudiantPicker {

    private final List<Etudiant> tous;
    private final TextField field = new TextField();
    private final ListView<Etudiant> suggestions = new ListView<>();
    private final Popup popup = new Popup();
    private Etudiant valeur;

    public EtudiantPicker(List<Etudiant> etudiants) {
        this.tous = etudiants;

        field.setPromptText("Nom ou prénom...");
        field.setPrefWidth(220);

        suggestions.setPrefSize(240, 140);
        suggestions.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Etudiant e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : nomComplet(e));
            }
        });

        popup.getContent().add(suggestions);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        field.textProperty().addListener((obs, ancien, texte) -> {
            if (valeur != null && nomComplet(valeur).equals(texte)) {
                return;
            }
            valeur = null;
            filtrer(texte);
        });

        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN && popup.isShowing() && !suggestions.getItems().isEmpty()) {
                suggestions.requestFocus();
                suggestions.getSelectionModel().selectFirst();
            }
        });

        suggestions.setOnMouseClicked(e -> choisir(suggestions.getSelectionModel().getSelectedItem()));
        suggestions.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                choisir(suggestions.getSelectionModel().getSelectedItem());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                popup.hide();
                field.requestFocus();
            }
        });
    }

    private void filtrer(String texte) {
        String recherche = texte == null ? "" : texte.toLowerCase().trim();
        if (recherche.isEmpty()) {
            popup.hide();
            return;
        }

        List<Etudiant> correspondances = tous.stream()
                .filter(e -> nomComplet(e).toLowerCase().contains(recherche))
                .collect(Collectors.toList());
        suggestions.setItems(FXCollections.observableArrayList(correspondances));

        if (correspondances.isEmpty() || field.getScene() == null || field.getScene().getWindow() == null) {
            popup.hide();
            return;
        }

        Point2D position = field.localToScreen(0, field.getHeight());
        if (popup.isShowing()) {
            popup.setX(position.getX());
            popup.setY(position.getY());
        } else {
            popup.show(field, position.getX(), position.getY());
        }
    }

    private void choisir(Etudiant e) {
        if (e == null) {
            return;
        }
        valeur = e;
        field.setText(nomComplet(e));
        field.positionCaret(field.getText().length());
        popup.hide();
        field.requestFocus();
    }

    private static String nomComplet(Etudiant e) {
        return e.getPrenom() + " " + e.getNom();
    }

    public Node getNode() {
        return field;
    }

    public Etudiant getValue() {
        return valeur;
    }

    public void setValue(Etudiant e) {
        valeur = e;
        field.setText(e == null ? "" : nomComplet(e));
    }

    public void setDisable(boolean disable) {
        field.setDisable(disable);
    }
}
