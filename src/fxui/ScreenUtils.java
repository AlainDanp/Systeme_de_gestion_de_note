package fxui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.Optional;

/** Petits helpers pour éviter de dupliquer le boilerplate Alert dans chaque écran. */
public final class ScreenUtils {

    private ScreenUtils() {}

    /** Un "chip" de filtre (étiquette + champ + bouton), utilisé dans les barres de filtres. */
    public static HBox filterGroup(String label, Node field, Button action) {
        HBox group = filterGroup(label, field);
        group.getChildren().add(action);
        return group;
    }

    /** Variante sans bouton (ex. simple champ de recherche). */
    public static HBox filterGroup(String label, Node field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("filter-group-label");
        HBox group = new HBox(6, lbl, field);
        group.getStyleClass().add("filter-group");
        group.setAlignment(Pos.CENTER_LEFT);
        return group;
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Erreur");
        alert.showAndWait();
    }

    public static void infoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Information");
        alert.showAndWait();
    }

    public static boolean showConfirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Confirmation");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }
}
