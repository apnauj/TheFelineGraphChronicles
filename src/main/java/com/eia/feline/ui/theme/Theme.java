package com.eia.feline.ui.theme;

import javafx.scene.paint.Color;

/**
 * Paleta de comic impreso.
 *
 * Los mismos colores viven en dos sitios porque JavaFX los necesita en dos
 * formatos: theme.css los usa para los controles y estas constantes para lo que
 * se pinta a mano sobre un Canvas (la cuadricula, los grafos, las particulas). Se
 * definen aqui una sola vez y el CSS los repite; si cambia uno hay que cambiar el
 * otro.
 *
 * Tres reglas del lenguaje visual, que salen de como se imprimia un comic:
 *
 *  - EL FONDO ES PAPEL, no una pantalla negra. Crema de papel prensa.
 *  - LA TINTA NO ES NEGRO PURO. #16131A: el negro de imprenta tira a calido, y
 *    el #000000 sobre crema se ve digital y muerto.
 *  - LOS COLORES SON PLANOS Y SATURADOS. Nada de degradados sutiles: en un comic
 *    el color se imprimia en bloques, y la profundidad la daban la linea negra y
 *    los puntos de trama.
 */
public final class Theme {

    private Theme() {}

    public static final String STYLESHEET = "/com/eia/feline/ui/theme.css";

    // Papel y tinta
    public static final Color PAPER      = Color.web("#F4ECD8");   // papel prensa
    public static final Color PAPER_WARM = Color.web("#E9DCC0");   // papel en sombra
    public static final Color PAPER_DEEP = Color.web("#D8C8A6");   // papel al fondo del panel
    public static final Color INK        = Color.web("#16131A");   // la linea negra
    public static final Color INK_SOFT   = Color.web("#4A4157");   // texto secundario

    // Personajes / roles. Saturados, planos.
    public static final Color POLA    = Color.web("#FF6B1A");   // heroina, BFS optimo, MST aceptado
    public static final Color MINERVA = Color.web("#00A6C7");   // secundario, DFS, nodos resueltos
    public static final Color LIMON   = Color.web("#D81B4A");   // villano, bombas, pesos negativos
    public static final Color CHURUN  = Color.web("#FFC42E");   // pesos, rutas, cables del MST
    public static final Color NINA    = Color.web("#FF6FA8");   // el objetivo
    public static final Color CAPE    = Color.web("#2B5FD9");   // azul de capa, acentos de heroe

    public static final Color OK    = Color.web("#2FA84F");
    public static final Color ERROR = Color.web("#D81B4A");

    /** Desplazamiento del "registro mal alineado" de la impresion, en pixeles. */
    public static final double MISPRINT_DX = 4;
    public static final double MISPRINT_DY = 4;

    /** Grosor de la linea de tinta. */
    public static final double INK_WIDTH = 3;

    /** El mismo color con otra opacidad, para halos y rastros. */
    public static Color fade(Color base, double opacity) {
        return Color.color(base.getRed(), base.getGreen(), base.getBlue(), opacity);
    }

    /** Mezcla lineal entre dos colores. */
    public static Color mix(Color from, Color to, double t) {
        return from.interpolate(to, Math.max(0, Math.min(1, t)));
    }
}
