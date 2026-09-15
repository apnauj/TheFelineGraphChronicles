package com.eia.feline.ui.screen;

import com.eia.feline.ui.fx.CatArt;
import com.eia.feline.ui.fx.Ink;
import com.eia.feline.ui.theme.Fonts;
import com.eia.feline.ui.theme.Theme;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Portada del numero uno: Pola corriendo mientras se descifran las pistas de Nero.
 *
 * Esta pantalla es la que fija el lenguaje visual antes de que el usuario vea
 * nada mas, asi que lleva de golpe todos los recursos del comic: papel tramado,
 * lineas de velocidad, rotulo con contorno, estrella de impacto y sombra de
 * registro mal alineado.
 *
 * La duracion minima es deliberada. Sin ella, en una maquina rapida la portada
 * parpadearia durante 80 ms y pareceria un fallo de dibujo en vez de una
 * intencion.
 */
public final class LoadingScreen extends StackPane {

    private static final Duration MINIMUM = Duration.millis(3000);
    private static final String MESSAGE = "DESCIFRANDO LAS PISTAS DE NERO...";

    /** Altura de la linea del suelo, como fraccion de la ventana. */
    private static final double FLOOR = 0.74;
    /** Cuanto bajan las patas por debajo del centro del gato, para apoyarlo en el suelo. */
    private static final double FEET_OFFSET = 74;

    private final Runnable onFinished;
    private final boolean autoAdvance;
    private final ProgressBar progress = new ProgressBar(0);
    private final Label typed = new Label("");
    private final Button start = new Button("COMENZAR");
    private final Pane backdrop = new Pane();

    private Timeline typewriter;
    private Animation gait;

    /** La portada del arranque: la barra se llena y pasa sola a la seleccion. */
    public static LoadingScreen intro(Runnable onFinished) {
        return new LoadingScreen(onFinished, true);
    }

    /**
     * La misma portada, pero alcanzada desde el boton de volver del menu. Aqui NO
     * puede avanzar sola: si lo hiciera, volver al menu desde la portada rebotaria
     * al instante y el boton de volver no serviria de nada. Se espera al usuario.
     */
    public static LoadingScreen cover(Runnable onContinue) {
        return new LoadingScreen(onContinue, false);
    }

    private LoadingScreen(Runnable onFinished, boolean autoAdvance) {
        this.onFinished = onFinished;
        this.autoAdvance = autoAdvance;
        Fonts.install();

        // Papel tramado como fondo de la pantalla, no como nodo: un rectangulo
        // atado al tamano de su propio contenedor infla la medicion del padre.
        Ink.paperBackground(this, Theme.PAPER, Theme.LIMON, 0.28);
        backdrop.setMouseTransparent(true);
        buildBackdrop();

        // Rotulo con contorno de tinta. Bangers ya viene en mayusculas de diseno.
        Text title = Ink.letter("THE FELINE GRAPH CHRONICLES", 56, Theme.CHURUN);
        title.setEffect(Ink.misprint(Theme.fade(Theme.LIMON, 0.55), 5, 5));

        Text subtitle = Ink.letter("POLA Y MINERVA CONTRA LIMON", 22, Theme.PAPER);

        typed.getStyleClass().addAll("mono", "caption");
        typed.setTextFill(Theme.INK);

        progress.setPrefWidth(440);
        progress.setMinHeight(22);

        Group runner = buildRunningCat();
        Pane catLayer = new Pane(runner);
        catLayer.setMouseTransparent(true);
        runner.layoutXProperty().bind(catLayer.widthProperty().divide(2));
        runner.layoutYProperty().bind(catLayer.heightProperty().multiply(FLOOR).subtract(FEET_OFFSET));

        VBox heading = new VBox(10, title, subtitle);
        heading.setAlignment(Pos.CENTER);
        heading.setPadding(new Insets(74, 40, 0, 40));
        // Sin esto el VBox se estira a toda la altura del StackPane y centra su
        // contenido verticalmente, ignorando la alineacion de arriba.
        heading.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(heading, Pos.TOP_CENTER);

        start.getStyleClass().add("button-primary");
        start.setOnAction(e -> {
            stopAnimations();
            onFinished.run();
        });
        start.setVisible(!autoAdvance);
        start.setManaged(!autoAdvance);
        progress.setVisible(autoAdvance);
        progress.setManaged(autoAdvance);

        VBox footer = new VBox(12, progress, start, typed);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 40, 70, 40));
        footer.setMaxWidth(Region.USE_PREF_SIZE);
        footer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(footer, Pos.BOTTOM_CENTER);

        getChildren().addAll(backdrop, catLayer, heading, footer, buildCaption(), buildKapow());
    }

    /** Papel tramado, lineas de velocidad y una linea de suelo entintada. */
    private void buildBackdrop() {
        // Lineas de velocidad saliendo de detras del gato.
        Group lines = Ink.speedLines(900, 34, Theme.fade(Theme.CHURUN, 0.55), 11);
        lines.layoutXProperty().bind(backdrop.widthProperty().divide(2));
        lines.layoutYProperty().bind(backdrop.heightProperty().multiply(0.52));
        backdrop.getChildren().add(lines);

        RotateTransition spin = new RotateTransition(Duration.seconds(52), lines);
        spin.setByAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.play();

        // Suelo: una banda de color con su linea de tinta encima.
        Rectangle ground = new Rectangle();
        ground.setFill(Theme.PAPER_DEEP);
        ground.widthProperty().bind(backdrop.widthProperty());
        ground.heightProperty().bind(backdrop.heightProperty().multiply(1 - FLOOR));
        ground.layoutYProperty().bind(backdrop.heightProperty().multiply(FLOOR));

        Rectangle groundInk = new Rectangle();
        groundInk.setFill(Theme.INK);
        groundInk.widthProperty().bind(backdrop.widthProperty());
        groundInk.setHeight(4);
        groundInk.layoutYProperty().bind(backdrop.heightProperty().multiply(FLOOR));

        backdrop.getChildren().addAll(ground, groundInk);
    }

    /**
     * La caja amarilla de narracion, arriba a la izquierda, como en una vineta.
     * Ademas de contar algo, llena el hueco entre el rotulo y el suelo.
     */
    private Label buildCaption() {
        Label caption = new Label(
                "EN EL LABORATORIO DE LENGUAJES Y COMPILADORES,\n"
                        + "LIMON Y NERO SE LO HAN LLEVADO TODO...");
        caption.getStyleClass().add("caption-box");
        caption.setRotate(-2.2);
        caption.setMaxWidth(Region.USE_PREF_SIZE);
        caption.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(caption, Pos.CENTER_LEFT);
        StackPane.setMargin(caption, new Insets(0, 0, 150, 78));
        return caption;
    }

    /** La estrella de impacto de la esquina, con el numero del comic. */
    private Group buildKapow() {
        Polygon star = Ink.inkedStar(13, 74, 46, Theme.LIMON, 7);
        Text number = Ink.letter("N. 1", 22, Theme.CHURUN);
        number.setTranslateX(-number.getLayoutBounds().getWidth() / 2);
        number.setTranslateY(8);

        Group kapow = new Group(star, number);
        kapow.setRotate(-14);
        StackPane.setAlignment(kapow, Pos.TOP_RIGHT);
        StackPane.setMargin(kapow, new Insets(96, 108, 0, 0));
        kapow.setMouseTransparent(true);

        Ink.throb(kapow, 1.09, Duration.millis(1500)).play();
        return kapow;
    }

    /** Pola corriendo en el sitio: las patas rotan en contrafase y la cola ondea. */
    private Group buildRunningCat() {
        CatArt.Runner runner = CatArt.runner(Theme.POLA, 160);
        Group holder = new Group(runner.node());

        // Se anima el pivote de la cadera, que mueve a la vez el color y la tinta.
        Timeline stride = new Timeline();
        for (int i = 0; i < runner.hips().length; i++) {
            // Las patas 0 y 2 van en fase; las 1 y 3, en contrafase.
            double phase = (i % 2 == 0) ? 1 : -1;
            var hip = runner.hips()[i];
            stride.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(hip.angleProperty(), 30 * phase)),
                    new KeyFrame(Duration.millis(140),
                            new KeyValue(hip.angleProperty(), -30 * phase, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(280),
                            new KeyValue(hip.angleProperty(), 30 * phase, Interpolator.EASE_BOTH)));
        }
        stride.setCycleCount(Animation.INDEFINITE);
        stride.play();
        gait = stride;

        Timeline bounce = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(holder.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(140),
                        new KeyValue(holder.translateYProperty(), -8, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(280),
                        new KeyValue(holder.translateYProperty(), 0, Interpolator.EASE_BOTH)));
        bounce.setCycleCount(Animation.INDEFINITE);
        bounce.play();

        RotateTransition tailWave = new RotateTransition(Duration.millis(620), runner.tail());
        tailWave.setFromAngle(-10);
        tailWave.setToAngle(12);
        tailWave.setAutoReverse(true);
        tailWave.setCycleCount(Animation.INDEFINITE);
        tailWave.play();

        // Polvo entintado bajo las patas.
        Group dust = new Group();
        for (int i = 0; i < 5; i++) {
            Circle puff = new Circle(4, Theme.PAPER);
            puff.setStroke(Theme.INK);
            puff.setStrokeWidth(2);
            puff.setCenterY(50);
            dust.getChildren().add(puff);
            Timeline drift = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(puff.centerXProperty(), -14),
                            new KeyValue(puff.opacityProperty(), 1),
                            new KeyValue(puff.radiusProperty(), 4)),
                    new KeyFrame(Duration.millis(900),
                            new KeyValue(puff.centerXProperty(), -108, Interpolator.LINEAR),
                            new KeyValue(puff.opacityProperty(), 0),
                            new KeyValue(puff.radiusProperty(), 11)));
            drift.setDelay(Duration.millis(i * 180));
            drift.setCycleCount(Animation.INDEFINITE);
            drift.play();
        }

        return new Group(dust, holder);
    }

    /** Arranca la portada. En modo intro avanza sola; en modo portada espera. */
    public void play() {
        typewriter = new Timeline();
        for (int i = 0; i <= MESSAGE.length(); i++) {
            final int upTo = i;
            typewriter.getKeyFrames().add(new KeyFrame(
                    Duration.millis(38.0 * i), e -> typed.setText(MESSAGE.substring(0, upTo))));
        }
        typewriter.play();

        if (!autoAdvance) {
            Ink.pop(start, Duration.millis(420)).play();
            return;
        }

        Timeline fill = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress.progressProperty(), 0)),
                new KeyFrame(MINIMUM, new KeyValue(progress.progressProperty(), 1, Interpolator.EASE_OUT)));
        fill.setOnFinished(e -> {
            stopAnimations();
            onFinished.run();
        });
        fill.play();
    }

    private void stopAnimations() {
        if (typewriter != null) typewriter.stop();
        if (gait != null) gait.stop();
    }
}
