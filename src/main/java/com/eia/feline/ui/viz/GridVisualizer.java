package com.eia.feline.ui.viz;

import com.eia.feline.algo.grid.GridGraph;
import com.eia.feline.algo.search.SearchResult;
import com.eia.feline.missions.MissionOneSolver;
import com.eia.feline.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Dibujo de la Mision 1: la cuadricula, las bombas, y los dos recorridos.
 *
 * La animacion es lo que vuelve visible la unica diferencia real entre BFS y
 * DFS. Los dos recorren exactamente el mismo grafo; lo unico que cambia es el
 * orden en que expanden la frontera. Reproduciendo visitOrder se ve al BFS
 * abrirse en anillos concentricos desde el origen y al DFS hundirse por una sola
 * rama hasta chocar. Esa es la razon de que 18 y 32 sean los dos numeros
 * correctos para el mismo mapa.
 *
 * Se pinta sobre un Canvas y no con un nodo por celda: 50x50 son 2500 celdas y
 * 2500 nodos de escena con sus propios efectos serian muchisimo mas lentos que un
 * solo repintado.
 */
public final class GridVisualizer implements Visualizer<MissionOneSolver.Case> {

    /** Seccion 2.3: por encima de 50x50 no se dibuja. */
    private static final int MAX_SIDE = 50;

    private final Canvas canvas = new Canvas(600, 600);
    private final Pane canvasHolder = new Pane(canvas);
    private final VBox root = new VBox(10);
    private final CheckBox showBfs = new CheckBox("BFS (optimo)");
    private final CheckBox showDfs = new CheckBox("DFS (valido, no optimo)");
    private final Label scoreboard = new Label();

    private MissionOneSolver.Case current;
    private int step;
    private int totalSteps;

    public GridVisualizer() {
        showBfs.setSelected(true);
        showDfs.setSelected(true);
        showBfs.setTextFill(Theme.MINERVA);
        showDfs.setTextFill(Theme.POLA);
        showBfs.setOnAction(e -> repaint());
        showDfs.setOnAction(e -> repaint());

        scoreboard.getStyleClass().add("scoreboard");

        HBox legend = new HBox(18, showBfs, showDfs);
        legend.setAlignment(Pos.CENTER_LEFT);

        canvasHolder.setMinSize(0, 0);
        VBox.setVgrow(canvasHolder, Priority.ALWAYS);

        // El Canvas se redimensiona con el panel y se vuelve a pintar.
        canvasHolder.widthProperty().addListener((o, a, b) -> resize());
        canvasHolder.heightProperty().addListener((o, a, b) -> resize());

        root.setPadding(new Insets(12));
        root.getChildren().addAll(scoreboard, legend, canvasHolder);
    }

    private void resize() {
        canvas.setWidth(Math.max(1, canvasHolder.getWidth()));
        canvas.setHeight(Math.max(1, canvasHolder.getHeight()));
        repaint();
    }

    @Override
    public Region node() { return root; }

    @Override
    public boolean withinDrawBudget(MissionOneSolver.Case c) {
        return c.grid().rows() <= MAX_SIDE && c.grid().cols() <= MAX_SIDE;
    }

    @Override
    public String overBudgetMessage(MissionOneSolver.Case c) {
        return "El mapa de Limon es de " + c.grid().rows() + " x " + c.grid().cols()
                + " celdas y el limite para dibujar es de " + MAX_SIDE + " x " + MAX_SIDE
                + ". Se omitio el dibujo por el tamano de la instancia; la respuesta"
                + " se calculo completa de todas formas.";
    }

    @Override
    public void render(MissionOneSolver.Case c) {
        this.current = c;
        this.step = 0;
        scoreboard.setText(c.reachable()
                ? "BFS " + c.bfsMoves() + "   DFS " + c.dfsMoves()
                : "Nina is unreachable");
        scoreboard.setTextFill(c.reachable() ? Theme.INK : Theme.LIMON);
        resize();
    }

    @Override
    public int prepareSteps(MissionOneSolver.Case c) {
        // Un paso por celda expandida del recorrido mas largo, y al final unos
        // cuantos pasos extra para que Pola camine el resultado.
        int longest = Math.max(c.bfs().visitCount(), c.dfs().visitCount());
        totalSteps = longest + (c.reachable() ? c.bfsMoves() + 1 : 0);
        return totalSteps;
    }

    @Override
    public void showStep(int step) {
        this.step = step;
        repaint();
    }

    // ------------------------------------------------------------------ pintar

    /**
     * Cada recorrido se dibuja en SU PROPIO tablero, lado a lado.
     *
     * Superponer los dos sobre la misma cuadricula parecia la opcion obvia y es
     * la peor: dos capas traslucidas encima de las mismas celdas se mezclan en un
     * color intermedio y deja de verse cual algoritmo visito que. Separados, la
     * comparacion es inmediata -- y es tambien como los presenta el enunciado.
     */
    private void repaint() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();

        g.setFill(Theme.PAPER);
        g.fillRect(0, 0, w, h);
        if (current == null) return;

        boolean bfs = showBfs.isSelected();
        boolean dfs = showDfs.isSelected();
        if (!bfs && !dfs) return;

        if (bfs && dfs) {
            // Se parte por el lado largo para que cada tablero quede lo mas cuadrado posible.
            if (w >= h) {
                drawBoard(g, 0, 0, w / 2, h, current.bfs(), Theme.MINERVA, "BFS", true);
                drawBoard(g, w / 2, 0, w / 2, h, current.dfs(), Theme.POLA, "DFS", false);
            } else {
                drawBoard(g, 0, 0, w, h / 2, current.bfs(), Theme.MINERVA, "BFS", true);
                drawBoard(g, 0, h / 2, w, h / 2, current.dfs(), Theme.POLA, "DFS", false);
            }
        } else if (bfs) {
            drawBoard(g, 0, 0, w, h, current.bfs(), Theme.MINERVA, "BFS", true);
        } else {
            drawBoard(g, 0, 0, w, h, current.dfs(), Theme.POLA, "DFS", false);
        }
    }

    /**
     * Un tablero completo dentro del rectangulo dado.
     *
     * @param withWalker si Pola recorre el camino al final (solo en el tablero optimo).
     */
    private void drawBoard(GraphicsContext g, double bx, double by, double bw, double bh,
                           SearchResult result, Color tint, String caption, boolean withWalker) {
        GridGraph grid = current.grid();
        int rows = grid.rows(), cols = grid.cols();

        double titleBand = 24;
        double inset = 10;
        double availableW = bw - 2 * inset;
        double availableH = bh - 2 * inset - titleBand;

        double cell = Math.max(3, Math.min(availableW / cols, availableH / rows));
        double originX = bx + inset + (availableW - cell * cols) / 2.0;
        double originY = by + inset + titleBand + (availableH - cell * rows) / 2.0;
        double pad = Math.max(0.5, cell * 0.06);
        double arc = Math.min(6, cell * 0.28);

        // Titulo del tablero con el resultado de ese algoritmo.
        int moves = result.distanceTo(current.end());
        String heading = current.reachable() ? caption + " - " + moves + " movimientos"
                                             : caption + " - sin ruta";
        g.setFill(tint);
        g.setFont(Font.font(14));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText(heading, bx + bw / 2, by + inset + 14);
        g.setTextAlign(TextAlignment.LEFT);

        // 1. El tablero: celdas libres y bombas.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int cellIndex = grid.index(r, c);
                double x = originX + c * cell, y = originY + r * cell;

                g.setFill(grid.isBomb(cellIndex) ? Theme.LIMON : Theme.PAPER_DEEP);
                g.fillRoundRect(x + pad, y + pad, cell - 2 * pad, cell - 2 * pad, arc, arc);

                // La linea de tinta alrededor de cada casilla es lo que convierte
                // la cuadricula en un dibujo de comic y no en una hoja de calculo.
                if (cell >= 7) {
                    g.setStroke(Theme.INK);
                    g.setLineWidth(Math.max(1, cell * 0.055));
                    g.strokeRoundRect(x + pad, y + pad, cell - 2 * pad, cell - 2 * pad, arc, arc);
                }
                if (grid.isBomb(cellIndex) && cell >= 12) {
                    drawBomb(g, x + cell / 2, y + cell / 2, cell * 0.22);
                }
            }
        }

        // 2. La frontera expandida hasta el paso actual.
        int shown = Math.min(step, result.visitCount());
        int[] order = result.visitOrder();
        for (int i = 0; i < shown; i++) {
            int cellIndex = order[i];
            double x = originX + grid.colOf(cellIndex) * cell;
            double y = originY + grid.rowOf(cellIndex) * cell;

            // Las celdas mas recientes brillan mas: eso es lo que dibuja la forma
            // del avance (anillos en BFS, una rama larga en DFS).
            double age = shown <= 1 ? 1 : (double) i / (shown - 1);
            g.setFill(Theme.fade(tint, 0.35 + 0.60 * age));
            g.fillRoundRect(x + pad, y + pad, cell - 2 * pad, cell - 2 * pad, arc, arc);

            if (cell >= 7) {
                boolean newest = (i == shown - 1);
                g.setStroke(Theme.INK);
                g.setLineWidth(newest ? Math.max(2, cell * 0.13) : Math.max(1, cell * 0.055));
                g.strokeRoundRect(x + pad, y + pad, cell - 2 * pad, cell - 2 * pad, arc, arc);
            }
        }

        // 3. El camino encontrado, cuando la exploracion ya termino.
        int explorationLength = Math.max(current.bfs().visitCount(), current.dfs().visitCount());
        if (current.reachable() && step >= explorationLength) {
            int[] path = result.pathTo(current.end());
            if (path.length > 0) {
                // Dos pasadas: primero la tinta mas gruesa, luego el color encima.
                // Es literalmente como se entinta y se colorea una vineta.
                for (int pass = 0; pass < 2; pass++) {
                    g.setStroke(pass == 0 ? Theme.INK : Theme.CHURUN);
                    g.setLineWidth(Math.max(2, cell * (pass == 0 ? 0.38 : 0.24)));
                    g.beginPath();
                    for (int i = 0; i < path.length; i++) {
                        double x = originX + grid.colOf(path[i]) * cell + cell / 2;
                        double y = originY + grid.rowOf(path[i]) * cell + cell / 2;
                        if (i == 0) g.moveTo(x, y); else g.lineTo(x, y);
                    }
                    g.stroke();
                }

                if (withWalker) {
                    paintWalker(g, path, step - explorationLength, originX, originY, cell);
                }
            }
        }

        // 4. Origen y destino, siempre encima de todo.
        drawMarker(g, grid, current.start(), Theme.POLA, "S", originX, originY, cell);
        drawMarker(g, grid, current.end(), Theme.NINA, "N", originX, originY, cell);
    }

    /** Pola avanzando por el camino, dejando huellas detras. */
    private void paintWalker(GraphicsContext g, int[] path, int walked,
                             double ox, double oy, double cell) {
        GridGraph grid = current.grid();
        int at = Math.max(0, Math.min(walked, path.length - 1));

        for (int i = 0; i <= at; i++) {
            double x = ox + grid.colOf(path[i]) * cell + cell / 2;
            double y = oy + grid.rowOf(path[i]) * cell + cell / 2;
            g.setFill(Theme.fade(Theme.INK, 0.45));
            g.fillOval(x - cell * 0.08, y - cell * 0.08, cell * 0.16, cell * 0.16);
        }

        double x = ox + grid.colOf(path[at]) * cell + cell / 2;
        double y = oy + grid.rowOf(path[at]) * cell + cell / 2;
        g.setFill(Theme.POLA);
        g.fillOval(x - cell * 0.34, y - cell * 0.34, cell * 0.68, cell * 0.68);
        // Dos orejas, para que se lea como un gato y no como un punto.
        g.fillPolygon(new double[]{ x - cell * 0.30, x - cell * 0.08, x - cell * 0.30 },
                      new double[]{ y - cell * 0.26, y - cell * 0.30, y - cell * 0.54 }, 3);
        g.fillPolygon(new double[]{ x + cell * 0.30, x + cell * 0.08, x + cell * 0.30 },
                      new double[]{ y - cell * 0.26, y - cell * 0.30, y - cell * 0.54 }, 3);
    }

    private void drawBomb(GraphicsContext g, double cx, double cy, double radius) {
        g.setFill(Color.web("#2A0A18"));
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setStroke(Theme.CHURUN);
        g.setLineWidth(Math.max(1, radius * 0.28));
        g.strokeLine(cx + radius * 0.5, cy - radius * 0.7, cx + radius * 1.2, cy - radius * 1.4);
    }

    private void drawMarker(GraphicsContext g, GridGraph grid, int cellIndex, Color tint,
                            String letter, double ox, double oy, double cell) {
        double x = ox + grid.colOf(cellIndex) * cell;
        double y = oy + grid.rowOf(cellIndex) * cell;

        g.setStroke(tint);
        g.setLineWidth(Math.max(1.5, cell * 0.12));
        g.strokeRoundRect(x + 1, y + 1, cell - 2, cell - 2, 6, 6);

        if (cell >= 14) {
            g.setFill(tint);
            g.setFont(Font.font(Math.min(16, cell * 0.5)));
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(letter, x + cell / 2, y + cell * 0.70);
            g.setTextAlign(TextAlignment.LEFT);
        }
    }
}
