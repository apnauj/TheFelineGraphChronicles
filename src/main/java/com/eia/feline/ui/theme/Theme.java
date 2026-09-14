package com.eia.feline.ui.theme;

import javafx.scene.paint.Color;

/**
 * Paleta unica de la aplicacion.
 *
 * Los mismos colores viven en dos sitios porque JavaFX los necesita en dos
 * formatos distintos: theme.css los usa para los controles (botones, paneles,
 * areas de texto) y estas constantes para lo que se pinta a mano sobre un Canvas
 * (la cuadricula, los grafos, las particulas). Se definen aqui una sola vez y el
 * CSS los repite; si cambia uno hay que cambiar el otro.
 *
 * El codigo de color cuenta la historia: naranja Pola para las heroinas y lo
 * optimo, cian Minerva para lo secundario, magenta Limon para las bombas y lo
 * negativo, dorado churun para los pesos.
 */
public final class Theme {

    private Theme() {}

    public static final String STYLESHEET = "/com/eia/feline/ui/theme.css";

    // Fondo y superficies
    public static final Color BG        = Color.web("#14121F");
    public static final Color PANEL     = Color.web("#1F1B2E");
    public static final Color PANEL_ALT = Color.web("#282239");
    public static final Color STROKE    = Color.web("#322C4A");
    public static final Color TEXT      = Color.web("#EDE9F5");
    public static final Color MUTED     = Color.web("#9A92B8");

    // Personajes / roles
    public static final Color POLA    = Color.web("#FF8A3D");   // heroina, BFS, MST aceptado
    public static final Color MINERVA = Color.web("#4DD0E1");   // secundario, DFS, nodos resueltos
    public static final Color LIMON   = Color.web("#C2185B");   // villano, bombas, pesos negativos
    public static final Color CHURUN  = Color.web("#FFD54F");   // pesos, relajacion, cables del MST
    public static final Color NINA    = Color.web("#F06292");   // el objetivo

    public static final Color OK    = Color.web("#66BB6A");
    public static final Color ERROR = Color.web("#EF5350");

    /** El mismo color con otra opacidad, para halos y rastros. */
    public static Color fade(Color base, double opacity) {
        return Color.color(base.getRed(), base.getGreen(), base.getBlue(), opacity);
    }

    /** Mezcla lineal entre dos colores; util para degradados de "antiguedad" en la animacion. */
    public static Color mix(Color from, Color to, double t) {
        double k = Math.max(0, Math.min(1, t));
        return from.interpolate(to, k);
    }
}
