package com.eia.feline.ui.screen;

import com.eia.feline.ui.fx.Art;
import com.eia.feline.ui.fx.CatArt;
import com.eia.feline.ui.fx.Ink;
import com.eia.feline.ui.fx.Music;
import com.eia.feline.ui.theme.Fonts;
import com.eia.feline.ui.theme.Theme;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Portada del numero uno: Pola en pose de heroina mientras se descifran las
 * pistas de Nero.
 *
 * NOTAS DE RENDIMIENTO -- la version anterior iba a tirones y merece explicacion,
 * porque el motivo no era obvio:
 *
 *  - Las lineas de velocidad GIRABAN. Eran 34 poligonos dentro de un Group de 900
 *    px de radio, y rotarlo obliga a JavaFX a volver a rasterizar toda esa area en
 *    cada fotograma. Era, con diferencia, el mayor coste de la pantalla. Ahora
 *    estan quietas, que ademas es como se dibujan en un comic de verdad: las
 *    lineas de velocidad no se mueven, sugieren el movimiento.
 *  - Habia doce Timeline a la vez (cuatro patas, rebote, cola, cinco motas de
 *    polvo, maquina de escribir, barra). Ahora son tres.
 *  - Lo que si se anima lleva setCache(true): JavaFX lo rasteriza una vez y luego
 *    mueve el mapa de bits, en vez de volver a dibujar las figuras cada fotograma.
 *
 * El gato tambien cambio: ya hay arte real, asi que no hace falta articular patas
 * con pivotes. La heroina entra de golpe, con sobrepaso, y flota suavemente.
 */
public final class LoadingScreen extends StackPane {

    private static final Duration MINIMUM = Duration.millis(3000);

    /** Altura de la linea del suelo, como fraccion de la ventana. */
    private static final double FLOOR = 0.86;

    private final Runnable onFinished;
    private final boolean autoAdvance;
    private final ProgressBar progress = new ProgressBar(0);
    private final Button start = new Button("COMENZAR");
    private final Pane backdrop = new Pane();

    private Node hero;
    private Timeline float_;

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
        Ink.paperBackground(this, Theme.PAPER, Theme.LIMON, 0.26);

        backdrop.setMouseTransparent(true);
        buildBackdrop();

        Text title = Ink.letter("THE FELINE GRAPH CHRONICLES", 54, Theme.CHURUN);
        title.setEffect(Ink.misprint(Theme.fade(Theme.LIMON, 0.55), 5, 5));

        Text subtitle = Ink.letter("POLA Y MINERVA CONTRA LIMON", 21, Theme.PAPER);

        progress.setPrefWidth(440);
        progress.setMinHeight(22);

        start.getStyleClass().add("button-primary");
        start.setOnAction(e -> {
            stopAnimations();
            onFinished.run();
        });
        start.setVisible(!autoAdvance);
        start.setManaged(!autoAdvance);
        progress.setVisible(autoAdvance);
        progress.setManaged(autoAdvance);

        VBox heading = new VBox(8, title, subtitle);
        heading.setAlignment(Pos.CENTER);
        heading.setPadding(new Insets(46, 40, 0, 40));
        // Sin esto el VBox se estira a toda la altura del StackPane y centra su
        // contenido verticalmente, ignorando la alineacion de arriba.
        heading.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(heading, Pos.TOP_CENTER);

        VBox footer = new VBox(12, progress, start);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 40, 34, 40));
        footer.setMaxWidth(Region.USE_PREF_SIZE);
        footer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(footer, Pos.BOTTOM_CENTER);

        getChildren().addAll(backdrop, buildHero(), heading, footer,
                buildCaption(), buildStolen(), buildPolaLine(), buildVillainLine(),
                buildKapow(), Music.toggleButton());
        Node musicToggle = getChildren().get(getChildren().size() - 1);
        StackPane.setAlignment(musicToggle, Pos.TOP_LEFT);
        StackPane.setMargin(musicToggle, new Insets(18, 0, 0, 22));

        Music.start();
    }

    /** Lineas de velocidad quietas y una banda de suelo, todo estatico. */
    private void buildBackdrop() {
        // QUIETAS a proposito: ver la nota de rendimiento de la clase.
        Group lines = Ink.speedLines(880, 30, Theme.fade(Theme.CHURUN, 0.50), 11);
        lines.layoutXProperty().bind(backdrop.widthProperty().divide(2));
        lines.layoutYProperty().bind(backdrop.heightProperty().multiply(0.50));
        // Se rasteriza una vez; no cambia nunca.
        lines.setCache(true);
        lines.setCacheHint(CacheHint.SPEED);

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

        backdrop.getChildren().addAll(lines, ground, groundInk);
    }

    /**
     * Pola, en grande y apoyada en el suelo. Flota muy despacio: un solo Timeline
     * sobre translateY, y el nodo cacheado para que solo se mueva el mapa de bits.
     */
    private Node buildHero() {
        hero = Art.portrait("pola", 430, () -> CatArt.head(Theme.POLA, 220));

        StackPane holder = new StackPane(hero);
        holder.setMouseTransparent(true);
        StackPane.setAlignment(hero, Pos.BOTTOM_CENTER);
        // El retrato se apoya justo encima de la linea del suelo.
        holder.paddingProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(
                () -> new Insets(0, 0, getHeight() * (1 - FLOOR) + 6, 0), heightProperty()));

        hero.setCache(true);
        hero.setCacheHint(CacheHint.SPEED);
        return holder;
    }

    /**
     * La caja amarilla de narracion, como en una vineta. Ademas de contar algo,
     * llena el hueco entre el rotulo y el suelo.
     */
    private Label buildCaption() {
        Label caption = new Label(
                "EN EL LABORATORIO DE LENGUAJES Y COMPILADORES,\n"
                        + "LIMON Y NERO SE LO HAN LLEVADO TODO...");
        caption.getStyleClass().add("caption-box");
        caption.setRotate(-2.2);
        caption.setMaxWidth(Region.USE_PREF_SIZE);
        caption.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(caption, Pos.TOP_LEFT);
        StackPane.setMargin(caption, new Insets(330, 0, 0, 52));
        return caption;
    }

    /** Segundo cartucho de narracion, debajo del primero. */
    private Label buildStolen() {
        Label caption = new Label(
                "...LAS CUENTAS DE CLAUDE, Y A NINA\n"
                        + "DE LA CASA DE SEBAS.");
        caption.getStyleClass().add("caption-box");
        caption.setRotate(1.6);
        caption.setMaxWidth(Region.USE_PREF_SIZE);
        caption.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(caption, Pos.TOP_LEFT);
        StackPane.setMargin(caption, new Insets(432, 0, 0, 76));
        return caption;
    }

    /** Bocadillo de Pola, con el rabito apuntando hacia ella. */
    private Node buildPolaLine() {
        Node bubble = speech("\u00A1VAMOS, MINERVA!\nCADA PISTA ES UN GRAFO...\nY LOS GRAFOS SE RESUELVEN.",
                Theme.PAPER, Ink.Tail.BOTTOM_LEFT, -3);
        StackPane.setAlignment(bubble, Pos.TOP_RIGHT);
        StackPane.setMargin(bubble, new Insets(268, 66, 0, 0));
        return bubble;
    }

    /** Bocadillo del villano, arriba, en su color. */
    private Node buildVillainLine() {
        Node bubble = speech("\u00A1EL CHURUN SERA MIO!\n- LIMON", Theme.LIMON, Ink.Tail.TOP_RIGHT, 4);
        StackPane.setAlignment(bubble, Pos.TOP_LEFT);
        StackPane.setMargin(bubble, new Insets(176, 0, 0, 62));
        return bubble;
    }

    /**
     * Un bocadillo con su texto dentro. La figura y la etiqueta van en un
     * StackPane porque el bocadillo es una Shape y no sabe colocar texto; se
     * dibuja detras y el texto encima.
     */
    private Node speech(String text, javafx.scene.paint.Color fill, Ink.Tail tail, double angle) {
        Label label = new Label(text);
        label.setTextFill(fill == Theme.LIMON ? Theme.PAPER : Theme.INK);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // La fuente se fija AQUI y no desde el CSS.
        //
        // applyCss() sobre un nodo que todavia no esta en una escena no aplica las
        // reglas de la hoja de estilos, porque la hoja cuelga de la escena. La
        // medida salia con la fuente por defecto, mas estrecha que Bangers, y el
        // bocadillo quedaba pequeno: el texto se salia por abajo.
        label.setFont(javafx.scene.text.Font.font(Fonts.DISPLAY, 17));
        label.applyCss();

        double w = Math.max(220, label.prefWidth(-1) + 46);
        double h = Math.max(70, label.prefHeight(w) + 34);

        javafx.scene.shape.Shape shape = Ink.inkedBubble(w, h, tail, fill);
        shape.setEffect(Ink.misprint(Theme.fade(Theme.INK, 0.35), 4, 4));

        // El rabito sobresale unos 20 px del cuerpo del bocadillo, asi que los
        // limites de la figura son mas altos que el bocadillo en si. El StackPane
        // centra el texto dentro de ESOS limites y lo deja descolgado hacia el
        // rabito, cruzando el borde. Se compensa medio rabito en sentido contrario.
        double tailShift = switch (tail) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> -10;
            case TOP_LEFT, TOP_RIGHT -> 10;
            default -> 0;
        };
        label.setTranslateY(tailShift);

        StackPane holder = new StackPane(shape, label);
        holder.setMaxWidth(Region.USE_PREF_SIZE);
        holder.setMaxHeight(Region.USE_PREF_SIZE);
        holder.setRotate(angle);
        holder.setMouseTransparent(true);
        return holder;
    }

    /** La estrella de impacto de la esquina, con el numero del comic. */
    private Group buildKapow() {
        Polygon star = Ink.inkedStar(13, 72, 44, Theme.LIMON, 7);
        Text number = Ink.letter("N. 1", 21, Theme.CHURUN);
        number.setTranslateX(-number.getLayoutBounds().getWidth() / 2);
        number.setTranslateY(8);

        Group kapow = new Group(star, number);
        kapow.setRotate(-14);
        StackPane.setAlignment(kapow, Pos.TOP_RIGHT);
        StackPane.setMargin(kapow, new Insets(74, 92, 0, 0));
        kapow.setMouseTransparent(true);
        kapow.setCache(true);
        kapow.setCacheHint(CacheHint.SPEED);

        Ink.throb(kapow, 1.08, Duration.millis(1600)).play();
        return kapow;
    }

    /** Arranca la portada. En modo intro avanza sola; en modo portada espera. */
    public void play() {
        // Entrada de comic: la heroina llega pasada de tamano y rebota a su sitio.
        Ink.pop(hero, Duration.millis(560)).play();

        Timeline drift = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(hero.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(1700),
                        new KeyValue(hero.translateYProperty(), -14, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(3400),
                        new KeyValue(hero.translateYProperty(), 0, Interpolator.EASE_BOTH)));
        drift.setCycleCount(Animation.INDEFINITE);
        drift.setDelay(Duration.millis(560));
        drift.play();
        float_ = drift;

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
        if (float_ != null) float_.stop();
    }
}
