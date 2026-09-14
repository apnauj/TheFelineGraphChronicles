package com.eia.feline.ui.screen;

import com.eia.feline.ui.fx.CatArt;
import com.eia.feline.ui.theme.Theme;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

/**
 * Pantalla de carga: Pola corriendo por el laboratorio mientras se descifran las
 * pistas de Nero.
 *
 * Tiene una duracion minima a proposito. Sin ella, en una maquina rapida la
 * pantalla parpadearia durante 80 ms y pareceria un error de dibujo en vez de
 * una intencion.
 */
public final class LoadingScreen extends StackPane {

    private static final Duration MINIMUM = Duration.millis(2600);
    /** Altura de la linea del suelo, como fraccion de la ventana. */
    private static final double FLOOR = 0.72;
    /** Cuanto bajan las patas por debajo del centro del gato, para apoyarlo en el suelo. */
    private static final double FEET_OFFSET = 60;
    private static final String MESSAGE = "DESCIFRANDO LAS PISTAS DE NERO...";

    private final Runnable onFinished;
    private final ProgressBar progress = new ProgressBar(0);
    private final Label typed = new Label("");
    private final Pane backdrop = new Pane();

    private Timeline typewriter;
    private Timeline parallax;
    private Animation gait;

    public LoadingScreen(Runnable onFinished) {
        this.onFinished = onFinished;

        setStyle("-fx-background-color: #14121F;");
        backdrop.setMouseTransparent(true);
        buildBackdrop();

        Label title = new Label("The Feline Graph Chronicles");
        title.getStyleClass().add("display");

        Label subtitle = new Label("Pola y Minerva contra Limon");
        subtitle.getStyleClass().add("subtitle");

        typed.getStyleClass().addAll("mono", "caption");
        typed.setTextFill(Theme.CHURUN);

        progress.setPrefWidth(420);
        progress.setMinHeight(14);

        // El gato va en su propia capa, anclado a la linea del suelo del fondo, para
        // que corra SOBRE el suelo y no flotando en el centro de la ventana.
        Group runner = buildRunningCat();
        Pane catLayer = new Pane(runner);
        catLayer.setMouseTransparent(true);
        runner.layoutXProperty().bind(catLayer.widthProperty().divide(2));
        runner.layoutYProperty().bind(catLayer.heightProperty().multiply(FLOOR).subtract(FEET_OFFSET));

        VBox heading = new VBox(8, title, subtitle);
        heading.setAlignment(Pos.CENTER);
        heading.setPadding(new Insets(96, 40, 0, 40));
        // Sin esto el VBox se estira a toda la altura del StackPane y centra su
        // contenido verticalmente, ignorando la alineacion de arriba.
        heading.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(heading, Pos.TOP_CENTER);

        VBox footer = new VBox(14, progress, typed);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 40, 84, 40));
        footer.setMaxWidth(Region.USE_PREF_SIZE);
        footer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(footer, Pos.BOTTOM_CENTER);

        getChildren().addAll(backdrop, catLayer, heading, footer);
    }

    /** Tres capas de "laboratorio" que se desplazan a distinta velocidad. */
    private void buildBackdrop() {
        Random rnd = new Random(7);
        Color[] layerColor = {
                Theme.fade(Theme.PANEL, 0.45),
                Theme.fade(Theme.PANEL_ALT, 0.55),
                Theme.fade(Theme.STROKE, 0.75)
        };
        double[] speed = { 26, 16, 9 };            // segundos por vuelta: lejos = mas lento
        double[] height = { 60, 96, 150 };

        parallax = new Timeline();
        parallax.setCycleCount(Animation.INDEFINITE);

        for (int layer = 0; layer < 3; layer++) {
            Group strip = new Group();
            // Dos copias seguidas: al desplazar el ancho completo, la segunda ocupa
            // el lugar de la primera y el bucle es invisible.
            for (int copy = 0; copy < 2; copy++) {
                for (int i = 0; i < 14; i++) {
                    double w = 30 + rnd.nextInt(70);
                    double h = height[layer] * (0.45 + rnd.nextDouble() * 0.55);
                    Rectangle block = new Rectangle(copy * 1400 + i * 100, -h, w, h);
                    block.setFill(layerColor[layer]);
                    block.setArcWidth(8);
                    block.setArcHeight(8);
                    strip.getChildren().add(block);
                }
            }
            strip.setTranslateY(0);
            strip.layoutYProperty().bind(backdrop.heightProperty().multiply(FLOOR - layer * 0.03));

            Timeline slide = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(strip.translateXProperty(), 0)),
                    new KeyFrame(Duration.seconds(speed[layer]),
                            new KeyValue(strip.translateXProperty(), -1400, Interpolator.LINEAR)));
            slide.setCycleCount(Animation.INDEFINITE);
            slide.play();

            backdrop.getChildren().add(strip);
        }

        // Suelo
        Rectangle floor = new Rectangle();
        floor.setFill(Theme.fade(Theme.STROKE, 0.6));
        floor.widthProperty().bind(backdrop.widthProperty());
        floor.setHeight(2);
        floor.layoutYProperty().bind(backdrop.heightProperty().multiply(FLOOR));
        backdrop.getChildren().add(floor);
    }

    /** Pola corriendo en el sitio: las patas rotan en contrafase y la cola ondea. */
    private Group buildRunningCat() {
        CatArt.Runner runner = CatArt.runner(Theme.POLA, 150);
        Group holder = new Group(runner.node());

        SequentialTransition legCycle = new SequentialTransition();
        Timeline stride = new Timeline();
        for (int i = 0; i < runner.legs().length; i++) {
            Line leg = runner.legs()[i];
            leg.getTransforms().clear();
            // Las patas 0 y 2 van en fase; las 1 y 3, en contrafase.
            double phase = (i % 2 == 0) ? 1 : -1;
            leg.setRotate(0);
            stride.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(leg.rotateProperty(), 26 * phase)),
                    new KeyFrame(Duration.millis(140), new KeyValue(leg.rotateProperty(), -26 * phase,
                            Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(280), new KeyValue(leg.rotateProperty(), 26 * phase,
                            Interpolator.EASE_BOTH)));
        }
        stride.setCycleCount(Animation.INDEFINITE);
        stride.play();
        gait = stride;
        legCycle.getChildren().add(new PauseTransition(Duration.ONE));

        // Rebote del cuerpo, sincronizado con el paso.
        Timeline bounce = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(holder.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(140),
                        new KeyValue(holder.translateYProperty(), -7, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(280),
                        new KeyValue(holder.translateYProperty(), 0, Interpolator.EASE_BOTH)));
        bounce.setCycleCount(Animation.INDEFINITE);
        bounce.play();

        RotateTransition tailWave = new RotateTransition(Duration.millis(620), runner.tail());
        tailWave.setFromAngle(-9);
        tailWave.setToAngle(11);
        tailWave.setAutoReverse(true);
        tailWave.setCycleCount(Animation.INDEFINITE);
        tailWave.play();

        // Polvo bajo las patas.
        Group dust = new Group();
        for (int i = 0; i < 5; i++) {
            Circle puff = new Circle(2.6, Theme.fade(Theme.MUTED, 0.5));
            puff.setCenterY(48);
            dust.getChildren().add(puff);
            Timeline drift = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(puff.centerXProperty(), -10),
                            new KeyValue(puff.opacityProperty(), 0.7),
                            new KeyValue(puff.radiusProperty(), 2.6)),
                    new KeyFrame(Duration.millis(900),
                            new KeyValue(puff.centerXProperty(), -96, Interpolator.LINEAR),
                            new KeyValue(puff.opacityProperty(), 0),
                            new KeyValue(puff.radiusProperty(), 7)));
            drift.setDelay(Duration.millis(i * 180));
            drift.setCycleCount(Animation.INDEFINITE);
            drift.play();
        }

        return new Group(dust, holder);
    }

    /** Arranca la carga; al terminar llama a onFinished en el hilo de JavaFX. */
    public void play() {
        typewriter = new Timeline();
        for (int i = 0; i <= MESSAGE.length(); i++) {
            final int upTo = i;
            typewriter.getKeyFrames().add(new KeyFrame(
                    Duration.millis(34.0 * i), e -> typed.setText(MESSAGE.substring(0, upTo))));
        }
        typewriter.play();

        Timeline fill = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress.progressProperty(), 0)),
                new KeyFrame(MINIMUM, new KeyValue(progress.progressProperty(), 1,
                        Interpolator.EASE_OUT)));
        fill.setOnFinished(e -> {
            stopAnimations();
            onFinished.run();
        });
        fill.play();
    }

    private void stopAnimations() {
        if (typewriter != null) typewriter.stop();
        if (parallax != null) parallax.stop();
        if (gait != null) gait.stop();
    }
}
