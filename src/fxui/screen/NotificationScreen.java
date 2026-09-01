package fxui.screen;

import Event.listener.NotificationListener;
import MVC.Notification;
import fxui.ScreenUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Centre de notifications. Remplace MVC.NotificationView.afficherNotifications(). */
public class NotificationScreen {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationListener notificationListener;
    private final Integer userId;
    private final Runnable onBack;
    private final ListView<Notification> liste = new ListView<>();
    private boolean nonLuesSeulement = false;

    private Label compteur;

    public NotificationScreen(NotificationListener notificationListener, Integer userId, Runnable onBack) {
        this.notificationListener = notificationListener;
        this.userId = userId;
        this.onBack = onBack;
    }

    public Parent build() {
        Label titre = new Label("Centre de notifications");
        titre.getStyleClass().add("screen-title");

        Button retour = new Button("Retour");
        retour.getStyleClass().add("btn-outline");
        retour.setOnAction(e -> onBack.run());

        HBox header = new HBox(20, retour, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("screen-header");

        compteur = new Label();
        compteur.setPadding(new Insets(0, 10, 10, 10));

        VBox top = new VBox(header, compteur);

        liste.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setText(null);
                } else {
                    String statut = n.isLu() ? "✓" : "●";
                    setText(String.format("%s %s %s\n%s", statut, n.getIcon(), n.getMessage(),
                            n.getCreatedAt().format(DTF)));
                    setStyle(n.isLu() ? "" : "-fx-font-weight: bold;");
                }
            }
        });

        Button toutes = new Button("Toutes");
        toutes.getStyleClass().add("btn-outline");
        toutes.setOnAction(e -> {
            nonLuesSeulement = false;
            rafraichir();
        });

        Button nonLues = new Button("Non lues");
        nonLues.getStyleClass().add("btn-outline");
        nonLues.setOnAction(e -> {
            nonLuesSeulement = true;
            rafraichir();
        });

        Button marquerLues = new Button("Marquer toutes comme lues");
        marquerLues.getStyleClass().addAll("btn", "btn-info");
        marquerLues.setOnAction(e -> {
            notificationListener.markAllAsRead(userId);
            rafraichir();
        });

        Button effacer = new Button("Effacer toutes les notifications");
        effacer.getStyleClass().addAll("btn", "btn-danger");
        effacer.setOnAction(e -> {
            if (ScreenUtils.showConfirm("Supprimer toutes les notifications ?")) {
                notificationListener.clearNotifications(userId);
                rafraichir();
            }
        });

        Button actualiser = new Button("Actualiser");
        actualiser.getStyleClass().addAll("btn", "btn-primary");
        actualiser.setOnAction(e -> rafraichir());

        HBox actions = new HBox(10, toutes, nonLues, marquerLues, effacer, actualiser);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        rafraichir();

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(liste);
        root.setBottom(actions);
        return root;
    }

    private void rafraichir() {
        List<Notification> notifications = nonLuesSeulement
                ? notificationListener.getUnreadNotificationsForUser(userId)
                : notificationListener.getNotificationsForUser(userId);
        liste.setItems(FXCollections.observableArrayList(notifications));
        liste.refresh();

        int total = notificationListener.getNotificationsForUser(userId).size();
        int nonLues = notificationListener.countUnreadNotifications(userId);
        compteur.setText("Total : " + total + "   |   Non lues : " + nonLues);
    }
}
