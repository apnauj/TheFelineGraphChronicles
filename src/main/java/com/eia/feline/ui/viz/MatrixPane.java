package com.eia.feline.ui.viz;

import com.eia.feline.algo.maxwalk.FloydWarshall;
import com.eia.feline.ui.theme.Theme;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * La matriz N x N de Floyd-Warshall, en un panel con barras de desplazamiento.
 *
 * El enunciado la exige visible para todo N hasta 100. Con 100 x 100 son 10.000
 * casillas: con un Label por casilla la construccion tarda segundos y el arbol de
 * escena se vuelve inmanejable, asi que se pinta sobre un Canvas del tamano
 * total y es el ScrollPane el que se mueve por encima. Dibujar es instantaneo y
 * el desplazamiento sigue siendo el nativo.
 *
 * Convenciones del enunciado: "-" para los pares sin ruta e "inf" para aquellos
 * cuyo maximo no esta acotado.
 */
public final class MatrixPane {

    private static final double CELL_W = 56;
    private static final double CELL_H = 22;
    private static final double HEADER_W = 44;

    private final Canvas canvas = new Canvas(10, 10);
    private final ScrollPane scroll = new ScrollPane(new Pane(canvas));

    public MatrixPane() {
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setPannable(true);
        scroll.getStyleClass().add("panel-sunken");
        scroll.setMinHeight(140);
        // Sin tope de alto: al ir en su propia columna, la matriz aprovecha todo
        // el alto disponible y hay que desplazarla mucho menos.
    }

    public ScrollPane node() { return scroll; }

    /** Repinta la matriz y resalta la casilla (start, destination), que es la respuesta. */
    public void render(long[][] best, boolean[][] unbounded, int start, int destination) {
        int n = best.length;
        double width = HEADER_W + n * CELL_W;
        double height = CELL_H + n * CELL_H;
        canvas.setWidth(width);
        canvas.setHeight(height);

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Theme.PAPER);
        g.fillRect(0, 0, width, height);
        g.setFont(Font.font(11));
        g.setTextAlign(TextAlignment.CENTER);

        // Encabezados de columna y de fila.
        g.setFill(Theme.INK_SOFT);
        for (int j = 0; j < n; j++) {
            g.fillText(String.valueOf(j), HEADER_W + j * CELL_W + CELL_W / 2, CELL_H * 0.72);
        }
        for (int i = 0; i < n; i++) {
            g.fillText(String.valueOf(i), HEADER_W / 2, CELL_H + i * CELL_H + CELL_H * 0.72);
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double x = HEADER_W + j * CELL_W;
                double y = CELL_H + i * CELL_H;

                boolean answer = (i == start && j == destination);
                if (answer) {
                    g.setFill(Theme.fade(Theme.CHURUN, 0.22));
                    g.fillRect(x + 1, y + 1, CELL_W - 2, CELL_H - 2);
                } else if ((i + j) % 2 == 0) {
                    g.setFill(Theme.fade(Theme.PAPER_WARM, 0.6));
                    g.fillRect(x + 1, y + 1, CELL_W - 2, CELL_H - 2);
                }

                String text;
                if (unbounded[i][j]) {
                    text = "inf";
                    g.setFill(Theme.LIMON);
                } else if (best[i][j] == FloydWarshall.NONE) {
                    text = "-";
                    g.setFill(Theme.fade(Theme.INK_SOFT, 0.7));
                } else {
                    text = String.valueOf(best[i][j]);
                    g.setFill(answer ? Theme.CHURUN
                            : (best[i][j] < 0 ? Theme.fade(Theme.LIMON, 0.9) : Theme.INK));
                }
                g.fillText(text, x + CELL_W / 2, y + CELL_H * 0.72);
            }
        }
        g.setTextAlign(TextAlignment.LEFT);
    }
}
