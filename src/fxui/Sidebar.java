package fxui;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Barre de navigation latérale persistante (façon maquette Figma), partagée par les
 * tableaux de bord Admin/Enseignant/Étudiant : une section reste mise en évidence pendant
 * que seul le contenu central change, au lieu de remplacer toute la fenêtre à chaque clic.
 */
public final class Sidebar {

    private final VBox root = new VBox();
    private final List<ToggleButton> items = new ArrayList<>();

    public Sidebar(List<Entry> entries, Runnable onNotifications, Runnable onLogout, Consumer<Parent> onSelect) {
        root.getStyleClass().add("sidebar");

        VBox nav = new VBox(4);
        for (Entry entry : entries) {
            ToggleButton btn = new ToggleButton(entry.icon() + "   " + entry.label());
            btn.getStyleClass().add("nav-item");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setWrapText(true);
            btn.setOnAction(e -> {
                items.forEach(b -> b.setSelected(b == btn));
                onSelect.accept(entry.content().get());
            });
            items.add(btn);
            nav.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button notif = new Button("🔔 Notification");
        notif.getStyleClass().addAll("btn", "btn-notif");
        notif.setMaxWidth(Double.MAX_VALUE);
        notif.setOnAction(e -> {
            clearSelection();
            onNotifications.run();
        });

        Button logout = new Button("⏻ Déconnexion");
        logout.getStyleClass().addAll("btn", "btn-danger");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> onLogout.run());

        HBox bottom = new HBox(8, notif, logout);
        HBox.setHgrow(notif, Priority.ALWAYS);
        HBox.setHgrow(logout, Priority.ALWAYS);
        bottom.getStyleClass().add("sidebar-bottom");

        root.getChildren().addAll(nav, spacer, bottom);
    }

    public Parent getNode() {
        return root;
    }

    /** Retire la mise en évidence de toutes les sections (retour à l'accueil du tableau de bord). */
    public void clearSelection() {
        items.forEach(b -> b.setSelected(false));
    }

    public record Entry(String icon, String label, Supplier<Parent> content) {}
}
