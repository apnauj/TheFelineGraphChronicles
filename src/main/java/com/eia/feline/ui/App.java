package com.eia.feline.ui;

import com.eia.feline.ui.screen.LoadingScreen;
import com.eia.feline.ui.screen.MissionSelectScreen;
import com.eia.feline.ui.screen.Navigator;
import com.eia.feline.ui.theme.Theme;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Ventana principal.
 *
 * Toda la navegacion pasa por un StackPane: la pantalla nueva se agrega encima,
 * aparece con un fundido y la anterior se retira al terminar. Asi no hay dos
 * pantallas peleando por el mismo espacio y cualquier pantalla puede llevar a
 * cualquier otra sin conocerla, solo a traves de Navigator.
 */
public final class App extends Application {

    private static final double MIN_WIDTH = 1040;
    private static final double MIN_HEIGHT = 680;

    private final StackPane root = new StackPane();

    @Override
    public void start(Stage stage) {
        root.setStyle("-fx-background-color: #14121F;");

        Navigator navigator = this::swapTo;

        Scene scene = new Scene(root, 1280, 820);
        var stylesheet = App.class.getResource(Theme.STYLESHEET);
        if (stylesheet != null) scene.getStylesheets().add(stylesheet.toExternalForm());

        stage.setScene(scene);
        stage.setTitle("The Feline Graph Chronicles");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        // La pantalla de carga se muestra sola y avisa cuando termina.
        LoadingScreen loading = new LoadingScreen(() -> navigator.go(new MissionSelectScreen(navigator)));
        root.getChildren().add(loading);
        loading.play();

        stage.show();
    }

    /** Cambia de pantalla con un fundido corto. */
    private void swapTo(Node next) {
        next.setOpacity(0);
        root.getChildren().add(next);

        FadeTransition in = new FadeTransition(Duration.millis(260), next);
        in.setFromValue(0);
        in.setToValue(1);
        in.setOnFinished(e -> {
            // Se quita todo lo que quedo debajo, ya invisible.
            while (root.getChildren().size() > 1) root.getChildren().remove(0);
        });
        in.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
