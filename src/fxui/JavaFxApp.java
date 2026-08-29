package fxui;

import core.AppContext;
import fxui.screen.LoginScreen;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée JavaFX, en remplacement progressif de Main/Application (console).
 * Lancer via `mvn javafx:run` (mainClass configuré dans pom.xml), ou directement
 * depuis l'IDE une fois le module réimporté en Maven.
 */
public class JavaFxApp extends Application {

    private AppContext appContext;

    @Override
    public void init() {
        appContext = AppContext.build();
    }

    @Override
    public void start(Stage primaryStage) {
        Navigator navigator = new Navigator(primaryStage);
        navigator.setLoginScreenSupplier(() ->
                navigator.show(new LoginScreen(appContext, navigator).build(), "Connexion"));

        navigator.showLogin();
    }
}
