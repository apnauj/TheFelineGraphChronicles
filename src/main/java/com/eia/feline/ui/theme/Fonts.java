package com.eia.feline.ui.theme;

import javafx.scene.text.Font;

import java.io.InputStream;

/**
 * Carga las tipografias del comic.
 *
 * JavaFX registra una fuente por su FAMILIA una vez que Font.loadFont la leyo, y
 * a partir de ahi el CSS puede pedirla por nombre. Hay dos detalles que obligan a
 * hacerlo asi y no solo con @font-face en el CSS:
 *
 *  1. La regla @font-face de JavaFX acepta UN solo nombre de familia y no admite
 *     listas de respaldo separadas por comas. Si la fuente no esta, no hay a que
 *     caer: el texto sale con la fuente por defecto sin avisar.
 *  2. Las fuentes tienen que estar cargadas ANTES de que se aplique la hoja de
 *     estilos, o la primera pasada de CSS no las encuentra.
 *
 * Por eso se cargan aqui, explicitamente, al arrancar; y si por lo que sea no
 * estuvieran, DISPLAY y BODY caen a una familia del sistema y la aplicacion se ve
 * peor pero funciona igual.
 */
public final class Fonts {

    /** Titulos, numeros grandes y onomatopeyas. Lettering de comic. */
    public static final String DISPLAY;

    /** Texto de interfaz: botones, etiquetas, descripciones. */
    public static final String BODY;

    /** Entrada y salida. Tiene que ser monoespaciada: la salida se compara caracter a caracter. */
    public static final String MONO = "Menlo, Consolas, 'DejaVu Sans Mono', monospace";

    private static boolean loaded;

    static {
        String display = load("/com/eia/feline/ui/fonts/Bangers-Regular.ttf");
        String body = load("/com/eia/feline/ui/fonts/Fredoka-SemiBold.ttf");

        DISPLAY = (display != null) ? display : "Impact";
        BODY = (body != null) ? body : "Avenir Next";
        loaded = display != null && body != null;
    }

    private Fonts() {}

    /**
     * Fuerza la carga. Es idempotente: solo dispara el inicializador estatico.
     * Llamarlo antes de construir la escena.
     */
    public static void install() {
        // El trabajo real lo hace el bloque static de arriba; tocar la clase basta.
    }

    /** false si alguna fuente no se pudo cargar y se esta usando el respaldo del sistema. */
    public static boolean usingBundledFonts() { return loaded; }

    private static String load(String resource) {
        try (InputStream in = Fonts.class.getResourceAsStream(resource)) {
            if (in == null) return null;
            Font font = Font.loadFont(in, 12);
            return (font == null) ? null : font.getFamily();
        } catch (Exception e) {
            return null;
        }
    }
}
