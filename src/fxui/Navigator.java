package fxui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Gère la fenêtre principale et le passage d'un écran à l'autre en remplaçant
 * simplement la racine de la Scene (une seule Scene/Stage pour toute l'application).
 */
public class Navigator {

    private final Stage primaryStage;
    private Scene scene;
    private Runnable loginScreenSupplier;

    public Navigator(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setLoginScreenSupplier(Runnable loginScreenSupplier) {
        this.loginScreenSupplier = loginScreenSupplier;
    }

    public void show(Parent root, String title) {
        if (scene == null) {
            scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/fxui/theme.css").toExternalForm());
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle(title);
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    public void showLogin() {
        if (loginScreenSupplier != null) {
            loginScreenSupplier.run();
        }
    }
}
