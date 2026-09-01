package fxui;

import gestion_Etudiant.model.Etudiant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ComboBox éditable avec filtrage en direct par nom/prénom, en remplacement de la recherche
 * par ID étudiant brut dans les écrans Notes/Bulletins.
 */
public final class EtudiantPicker {

    private EtudiantPicker() {}

    public static ComboBox<Etudiant> build(List<Etudiant> etudiants) {
        StringConverter<Etudiant> converter = new StringConverter<>() {
            @Override
            public String toString(Etudiant e) {
                return e == null ? "" : e.getPrenom() + " " + e.getNom();
            }
            @Override
            public Etudiant fromString(String s) {
                return null;
            }
        };

        ObservableList<Etudiant> tous = FXCollections.observableArrayList(etudiants);
        ObservableList<Etudiant> filtres = FXCollections.observableArrayList(tous);

        ComboBox<Etudiant> combo = new ComboBox<>(filtres);
        combo.setEditable(true);
        combo.setPromptText("Nom ou prénom...");
        combo.setPrefWidth(220);
        combo.setConverter(converter);

        combo.getEditor().textProperty().addListener((obs, ancien, texte) -> {
            if (combo.getValue() != null && converter.toString(combo.getValue()).equals(texte)) {
                return;
            }
            String recherche = texte == null ? "" : texte.toLowerCase();
            List<Etudiant> correspondances = tous.stream()
                    .filter(e -> converter.toString(e).toLowerCase().contains(recherche))
                    .collect(Collectors.toList());
            filtres.setAll(correspondances);
            if (!correspondances.isEmpty()) {
                combo.show();
            } else {
                combo.hide();
            }
        });

        return combo;
    }
}
