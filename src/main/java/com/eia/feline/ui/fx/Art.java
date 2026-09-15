package com.eia.feline.ui.fx;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Carga los retratos de los personajes desde los PNG, con respaldo a las figuras
 * dibujadas por codigo cuando el arte todavia no existe.
 *
 * Asi el arte definitivo puede ir entrando pieza a pieza: cada personaje que ya
 * tenga su PNG lo usa, y el que no, sigue con el marcador de posicion de CatArt.
 * No hay un dia en que haya que cambiarlo todo de golpe.
 *
 * JavaFX NO carga archivos .svg, asi que los retratos entran como PNG. Estan
 * exportados a unas tres veces el tamano en que se muestran para que aguanten
 * pantallas de alta densidad.
 */
public final class Art {

    private static final String BASE = "/com/eia/feline/ui/art/png/";

    /** Las imagenes se cargan una sola vez: varias pantallas piden el mismo retrato. */
    private static final Map<String, Image> CACHE = new HashMap<>();

    /**
     * Alto al que se decodifican los retratos.
     *
     * Los PNG vienen a 900 px de alto y se muestran a 340 como mucho.
     * Decodificarlos a tamano completo y dejar que la tarjeta grafica los escale
     * en cada fotograma gasta unas siete veces mas memoria y mas trabajo por
     * cuadro del necesario. Se decodifican una vez, ya al tamano util.
     *
     * Se deja algo de margen (450 y no 340) para que en pantallas de alta
     * densidad, donde un punto logico son dos fisicos, no se vean pastosos.
     */
    private static final int DECODE_HEIGHT = 450;

    private Art() {}

    /**
     * Retrato de un personaje, escalado a la altura pedida.
     *
     * @param name     nombre del archivo sin extension, por ejemplo "limon"
     * @param height   altura en pixeles
     * @param fallback que dibujar si el PNG no existe todavia
     */
    public static Node portrait(String name, double height, Supplier<Node> fallback) {
        Image image = load(name);
        if (image == null) return fallback.get();

        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitHeight(height);
        view.setSmooth(true);
        return view;
    }

    /**
     * Igual que portrait, pero la altura sigue a la del contenedor. Para vinetas
     * que crecen con la ventana.
     */
    public static Node fittedPortrait(String name, StackPane holder, Supplier<Node> fallback) {
        Image image = load(name);
        if (image == null) return fallback.get();

        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        // Se deja un margen para que el retrato no toque el borde de la vineta.
        view.fitHeightProperty().bind(holder.heightProperty().subtract(12));
        return view;
    }

    /** true si el PNG de ese personaje ya esta en el proyecto. */
    public static boolean has(String name) { return load(name) != null; }

    private static Image load(String name) {
        if (CACHE.containsKey(name)) return CACHE.get(name);
        Image image = null;
        try (var in = Art.class.getResourceAsStream(BASE + name + ".png")) {
            if (in != null) {
                // requestedWidth 0 con preserveRatio: el ancho sale del alto.
                Image candidate = new Image(in, 0, DECODE_HEIGHT, true, true);
                if (!candidate.isError()) image = candidate;
            }
        } catch (Exception ignored) {
            // Sin arte se usa el respaldo; no es un error que deba romper la interfaz.
        }
        CACHE.put(name, image);
        return image;
    }
}
