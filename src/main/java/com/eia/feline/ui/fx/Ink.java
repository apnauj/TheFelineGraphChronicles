package com.eia.feline.ui.fx;

import com.eia.feline.ui.theme.Theme;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Caja de herramientas del lenguaje de comic: trama de puntos, sombra de registro
 * mal alineado, estrellas de impacto, bocadillos, lineas de velocidad y lettering
 * con contorno.
 *
 * Existe porque JavaFX no trae nada de esto y porque el CSS de JavaFX se queda
 * corto justo aqui: no hay funciones de filtro, no hay animaciones ni
 * transiciones en CSS (todo movimiento tiene que ser un Timeline de Java) y solo
 * hay dos efectos, dropshadow e innershadow. Asi que lo que en la web serian
 * cuatro lineas de CSS aqui son figuras y Canvas.
 *
 * Todo lo que devuelve es un nodo o una forma corriente de JavaFX: quien lo usa
 * no necesita saber como esta hecho.
 */
public final class Ink {

    private Ink() {}

    // ------------------------------------------------------------------ trama

    /** Las tramas son caras de generar y siempre iguales, asi que se reutilizan. */
    private static final Map<String, ImagePattern> HALFTONE_CACHE = new HashMap<>();

    /**
     * Trama de puntos (Ben-Day), el sombreado con el que se imprimian los comics.
     *
     * Se genera dibujando un mosaico en un Canvas y fotografiandolo. Los puntos se
     * colocan en las cuatro esquinas y en el centro para que el mosaico case
     * consigo mismo por los cuatro lados y no se vea la costura al repetirlo.
     *
     * @param dot       color del punto
     * @param tile      lado del mosaico en pixeles; mas grande = trama mas suelta
     * @param radius    radio del punto; mas grande = trama mas densa
     */
    public static ImagePattern halftone(Color dot, double tile, double radius) {
        String key = dot + "|" + tile + "|" + radius;
        ImagePattern cached = HALFTONE_CACHE.get(key);
        if (cached != null) return cached;

        Canvas canvas = new Canvas(tile, tile);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(dot);
        // Las cuatro esquinas mas el centro: asi el mosaico es continuo al repetirse.
        double[][] centres = {
                { 0, 0 }, { tile, 0 }, { 0, tile }, { tile, tile }, { tile / 2, tile / 2 }
        };
        for (double[] c : centres) {
            g.fillOval(c[0] - radius, c[1] - radius, radius * 2, radius * 2);
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = canvas.snapshot(params, new WritableImage((int) tile, (int) tile));

        ImagePattern pattern = new ImagePattern(image, 0, 0, tile, tile, false);
        HALFTONE_CACHE.put(key, pattern);
        return pattern;
    }

    /**
     * Fondo de papel tramado para una Region: color plano abajo, puntos encima.
     *
     * Se hace con Background y NO con un Rectangle dentro del contenedor. Un
     * Rectangle atado al tamano de su propio contenedor crea un ciclo de
     * medicion: al no ser redimensionable, sus limites entran en el tamano
     * preferido del padre, que agranda al padre, que agranda al rectangulo. El
     * sintoma es una pantalla mas grande que la ventana y el contenido
     * desplazado fuera de la vista. Un Background no participa en la medicion.
     */
    public static void paperBackground(Region region, Color paper, Color dot, double opacity) {
        region.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(paper, null, null),
                new javafx.scene.layout.BackgroundFill(
                        halftone(Theme.fade(dot, opacity), 10, 2.0), null, null)));
    }

    /** Rectangulo tramado, para poner de fondo dentro de un panel. */
    public static Rectangle halftonePanel(double w, double h, Color dot, double opacity) {
        Rectangle r = new Rectangle(w, h);
        r.setFill(halftone(dot, 8, 1.6));
        r.setOpacity(opacity);
        r.setMouseTransparent(true);
        return r;
    }

    // ------------------------------------------------- registro mal alineado

    /**
     * La sombra dura y desplazada que imita el error de registro de la impresion a
     * cuatro tintas: la plancha de color no cae exactamente sobre la del negro.
     *
     * Es un dropshadow con radio 0 y spread 1, que en JavaFX da un borde duro sin
     * difuminar. Sin el radio 0 seria una sombra moderna y se pierde el efecto.
     */
    public static DropShadow misprint(Color color) {
        return misprint(color, Theme.MISPRINT_DX, Theme.MISPRINT_DY);
    }

    public static DropShadow misprint(Color color, double dx, double dy) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(color);
        shadow.setRadius(0);
        shadow.setSpread(1);
        shadow.setOffsetX(dx);
        shadow.setOffsetY(dy);
        return shadow;
    }

    // ------------------------------------------------------------- estrellas

    /**
     * Estrella de impacto tipo KAPOW: un poligono que alterna radio exterior e
     * interior, con las puntas ligeramente irregulares para que parezca dibujada a
     * mano y no generada.
     *
     * @param points numero de puntas
     * @param outer  radio de las puntas
     * @param inner  radio de los valles
     * @param seed   semilla del temblor; la misma semilla da siempre la misma estrella
     */
    public static Polygon starburst(int points, double outer, double inner, long seed) {
        Random rnd = new Random(seed);
        Polygon star = new Polygon();
        for (int i = 0; i < points * 2; i++) {
            boolean tip = (i % 2 == 0);
            double radius = tip ? outer : inner;
            radius *= 0.86 + rnd.nextDouble() * 0.28;          // temblor de mano
            double angle = Math.PI * i / points - Math.PI / 2;
            angle += (rnd.nextDouble() - 0.5) * 0.10;
            star.getPoints().addAll(Math.cos(angle) * radius, Math.sin(angle) * radius);
        }
        star.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return star;
    }

    /** Estrella ya entintada y con su sombra de registro: lista para usar. */
    public static Polygon inkedStar(int points, double outer, double inner, Color fill, long seed) {
        Polygon star = starburst(points, outer, inner, seed);
        star.setFill(fill);
        star.setStroke(Theme.INK);
        star.setStrokeWidth(Theme.INK_WIDTH);
        star.setStrokeType(StrokeType.OUTSIDE);
        star.setEffect(misprint(Theme.fade(Theme.INK, 0.30), 5, 5));
        return star;
    }

    // ------------------------------------------------------------ bocadillos

    public enum Tail { BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT, NONE }

    /**
     * Bocadillo de dialogo: un rectangulo redondeado unido a un triangulo que hace
     * de rabito. Shape.union los funde en una sola figura, de modo que la linea de
     * tinta recorre el contorno completo y no queda una costura donde se tocan.
     */
    public static Shape speechBubble(double w, double h, Tail tail) {
        Rectangle body = new Rectangle(w, h);
        body.setArcWidth(26);
        body.setArcHeight(26);
        if (tail == Tail.NONE) return body;

        double tw = Math.min(26, w * 0.18);
        Polygon spike = switch (tail) {
            case BOTTOM_LEFT  -> new Polygon(w * 0.18, h - 2, w * 0.18 + tw, h - 2, w * 0.10, h + 20);
            case BOTTOM_RIGHT -> new Polygon(w * 0.72, h - 2, w * 0.72 + tw, h - 2, w * 0.86, h + 20);
            case TOP_LEFT     -> new Polygon(w * 0.18, 2, w * 0.18 + tw, 2, w * 0.10, -20);
            case TOP_RIGHT    -> new Polygon(w * 0.72, 2, w * 0.72 + tw, 2, w * 0.86, -20);
            default           -> new Polygon();
        };
        return Shape.union(body, spike);
    }

    /** Bocadillo ya entintado. */
    public static Shape inkedBubble(double w, double h, Tail tail, Color fill) {
        Shape bubble = speechBubble(w, h, tail);
        bubble.setFill(fill);
        bubble.setStroke(Theme.INK);
        bubble.setStrokeWidth(Theme.INK_WIDTH);
        bubble.setStrokeType(StrokeType.OUTSIDE);
        return bubble;
    }

    // --------------------------------------------------- lineas de velocidad

    /**
     * Lineas radiales que salen del centro, la forma clasica de decir "aqui pasa
     * algo". Se dejan como cunas finas en vez de lineas rectas porque en el dibujo
     * original se entintaban con pincel y se van afinando.
     */
    public static javafx.scene.Group speedLines(double radius, int count, Color color, long seed) {
        Random rnd = new Random(seed);
        javafx.scene.Group group = new javafx.scene.Group();
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count + (rnd.nextDouble() - 0.5) * 0.12;
            double inner = radius * (0.30 + rnd.nextDouble() * 0.22);
            double thickness = 0.016 + rnd.nextDouble() * 0.028;

            Polygon wedge = new Polygon(
                    Math.cos(angle) * inner, Math.sin(angle) * inner,
                    Math.cos(angle - thickness) * radius, Math.sin(angle - thickness) * radius,
                    Math.cos(angle + thickness) * radius, Math.sin(angle + thickness) * radius);
            wedge.setFill(color);
            group.getChildren().add(wedge);
        }
        group.setMouseTransparent(true);
        return group;
    }

    // --------------------------------------------------------- lettering

    /**
     * Rotulo de comic: relleno de color con un grueso contorno de tinta POR FUERA.
     *
     * StrokeType.OUTSIDE es la clave. Con el centrado (que es el valor por defecto)
     * la mitad del trazo se come el interior de la letra y el texto se cierra y
     * deja de leerse a tamanos grandes.
     */
    public static Text letter(String content, double size, Color fill) {
        Text text = new Text(content);
        text.setFont(Font.font(com.eia.feline.ui.theme.Fonts.DISPLAY, size));
        text.setFill(fill);
        text.setStroke(Theme.INK);
        text.setStrokeWidth(Math.max(2, size * 0.055));
        text.setStrokeType(StrokeType.OUTSIDE);
        text.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return text;
    }

    // ------------------------------------------------------------ movimiento

    /**
     * Entrada "de golpe": la figura llega pasada de tamano y rebota a su sitio.
     *
     * El sobrepaso es lo que distingue un movimiento de comic de uno de interfaz
     * corriente. Un fundido suave aqui se sentiria educado y equivocado.
     */
    public static Timeline pop(Node node, Duration duration) {
        node.setScaleX(0.2);
        node.setScaleY(0.2);
        node.setOpacity(0);
        return new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.scaleXProperty(), 0.2),
                        new KeyValue(node.scaleYProperty(), 0.2),
                        new KeyValue(node.opacityProperty(), 0)),
                new KeyFrame(duration.multiply(0.55),
                        new KeyValue(node.scaleXProperty(), 1.14, Interpolator.EASE_OUT),
                        new KeyValue(node.scaleYProperty(), 1.14, Interpolator.EASE_OUT),
                        new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_OUT)),
                new KeyFrame(duration,
                        new KeyValue(node.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleYProperty(), 1, Interpolator.EASE_BOTH)));
    }

    /** Sacudida corta, para errores y para los cables que cierran ciclo. */
    public static Timeline shake(Node node, double amplitude) {
        double origin = node.getTranslateX();
        Timeline t = new Timeline();
        int steps = 6;
        for (int i = 0; i <= steps; i++) {
            double offset = (i == steps) ? 0
                    : amplitude * (1 - i / (double) steps) * ((i % 2 == 0) ? 1 : -1);
            t.getKeyFrames().add(new KeyFrame(Duration.millis(i * 45),
                    new KeyValue(node.translateXProperty(), origin + offset, Interpolator.LINEAR)));
        }
        return t;
    }

    /** Latido lento e infinito, para lo que tiene que pedir atencion sin gritar. */
    public static Timeline throb(Node node, double to, Duration period) {
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.scaleXProperty(), 1),
                        new KeyValue(node.scaleYProperty(), 1)),
                new KeyFrame(period.divide(2),
                        new KeyValue(node.scaleXProperty(), to, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleYProperty(), to, Interpolator.EASE_BOTH)),
                new KeyFrame(period,
                        new KeyValue(node.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleYProperty(), 1, Interpolator.EASE_BOTH)));
        t.setCycleCount(Timeline.INDEFINITE);
        return t;
    }

    // ------------------------------------------------------------- utilidades

    /** Pone linea de tinta y sombra de registro a cualquier figura. */
    public static <T extends Shape> T inked(T shape) {
        shape.setStroke(Theme.INK);
        shape.setStrokeWidth(Theme.INK_WIDTH);
        shape.setStrokeType(StrokeType.OUTSIDE);
        shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return shape;
    }

    /** Recorta un nodo a su propio rectangulo, para que la trama no se salga del panel. */
    public static void clipToBounds(Region region) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        region.setClip(clip);
    }

    /** Circulo entintado, el nodo base de los grafos. */
    public static Circle inkedCircle(double radius, Color fill) {
        Circle c = new Circle(radius, fill);
        return inked(c);
    }
}
