package com.eia.feline.ui.viz;

import com.eia.feline.algo.graph.EdgeList;
import com.eia.feline.missions.MissionThreeSolver;
import com.eia.feline.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Dibujo de la Mision 3: el grafo dirigido, la matriz de Floyd-Warshall y el
 * elemento resaltado, que segun el caso es una ruta o un ciclo.
 *
 * Los pasadizos envenenados (peso negativo) van en magenta y a trazos, para que
 * se vea de un golpe por que este problema no lo puede resolver Dijkstra.
 *
 * Cuando la respuesta es "Infinite churun!" lo resaltado NO es una ruta sino el
 * ciclo de ganancia positiva responsable, que es lo que pide el enunciado: no
 * existe una ruta maxima que dibujar, existe una vuelta que se puede repetir
 * para siempre.
 */
public final class MaxWalkVisualizer implements Visualizer<MissionThreeSolver.Case> {

    /** Seccion 2.3: por encima de 60 nodos no se dibuja el grafo. */
    private static final int MAX_NODES = 60;

    private final Canvas canvas = new Canvas(600, 380);
    private final Pane canvasHolder = new Pane(canvas);
    private final MatrixPane matrix = new MatrixPane();
    private final VBox root = new VBox(8);
    private final Label scoreboard = new Label();
    private final Label mismatchBanner = new Label();
    private final Label matrixLabel = new Label("MATRIZ DE FLOYD-WARSHALL  (- sin ruta, inf no acotado)");

    private MissionThreeSolver.Case current;
    private double[] nodeX = new double[0];
    private double[] nodeY = new double[0];
    private int step;

    public MaxWalkVisualizer() {
        scoreboard.getStyleClass().addAll("mono", "title");
        matrixLabel.getStyleClass().add("section-label");

        mismatchBanner.getStyleClass().addAll("banner-error", "text-error");
        mismatchBanner.setWrapText(true);
        mismatchBanner.setVisible(false);
        mismatchBanner.setManaged(false);

        canvasHolder.setMinSize(0, 0);
        VBox.setVgrow(canvasHolder, Priority.ALWAYS);
        canvasHolder.widthProperty().addListener((o, a, b) -> relayout());
        canvasHolder.heightProperty().addListener((o, a, b) -> relayout());

        root.setPadding(new Insets(12));
        root.getChildren().addAll(scoreboard, mismatchBanner, canvasHolder, matrixLabel, matrix.node());
    }

    @Override
    public Region node() { return root; }

    @Override
    public boolean withinDrawBudget(MissionThreeSolver.Case c) {
        return c.nodes() <= MAX_NODES;
    }

    @Override
    public String overBudgetMessage(MissionThreeSolver.Case c) {
        return "El mapa tiene " + c.nodes() + " nodos y el limite para dibujar el grafo es de "
                + MAX_NODES + ". Se omitio el dibujo por el tamano de la instancia;"
                + " la matriz y la respuesta se calcularon completas de todas formas.";
    }

    @Override
    public void render(MissionThreeSolver.Case c) {
        this.current = c;
        this.step = 0;

        switch (c.outcome()) {
            case BLOCKED -> {
                scoreboard.setText("Limon blocked the way");
                scoreboard.setTextFill(Theme.LIMON);
            }
            case INFINITE -> {
                scoreboard.setText("Infinite churun!");
                scoreboard.setTextFill(Theme.CHURUN);
            }
            case VALUE -> {
                scoreboard.setText("Churun maximo: " + c.churun());
                scoreboard.setTextFill(c.churun() < 0 ? Theme.LIMON : Theme.CHURUN);
            }
        }

        // El enunciado exige avisar si los dos algoritmos alguna vez discrepan.
        boolean disagree = !c.agrees();
        mismatchBanner.setText(disagree
                ? "Floyd-Warshall y Bellman-Ford NO coinciden: " + c.mismatch()
                : "");
        mismatchBanner.setVisible(disagree);
        mismatchBanner.setManaged(disagree);

        // La matriz se muestra siempre para N <= 100, incluso si el grafo no se dibuja.
        matrix.render(c.matrix(), c.unbounded(), c.start(), c.destination());
        relayout();
    }

    @Override
    public int prepareSteps(MissionThreeSolver.Case c) {
        return switch (c.outcome()) {
            case VALUE -> Math.max(0, c.route().length - 1);
            // En el caso infinito se dan tres vueltas al ciclo, para que se lea como un bucle.
            case INFINITE -> c.cycle().length == 0 ? 0 : c.cycle().length * 3;
            case BLOCKED -> 0;
        };
    }

    @Override
    public void showStep(int step) {
        this.step = step;
        repaint();
    }

    private void relayout() {
        canvas.setWidth(Math.max(1, canvasHolder.getWidth()));
        canvas.setHeight(Math.max(1, canvasHolder.getHeight()));
        if (current != null && canvas.getWidth() > 1 && canvas.getHeight() > 1) {
            double[][] positions = SpringLayout.compute(current.nodes(), current.edges(),
                    canvas.getWidth(), canvas.getHeight(), 44, 1234L);
            nodeX = positions[0];
            nodeY = positions[1];
        }
        repaint();
    }

    private void repaint() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.setFill(Theme.BG);
        g.fillRect(0, 0, w, h);
        if (current == null || nodeX.length == 0) return;

        EdgeList edges = current.edges();
        double radius = Math.max(9, Math.min(19, 300.0 / Math.max(4, current.nodes())));

        // 1. Los pasadizos, con punta de flecha porque son dirigidos.
        for (int e = 0; e < edges.size(); e++) {
            int a = edges.from(e), b = edges.to(e);
            if (a == b) continue;
            boolean poisoned = edges.weight(e) < 0;
            g.setStroke(poisoned ? Theme.fade(Theme.LIMON, 0.8) : Theme.fade(Theme.STROKE, 0.95));
            g.setLineWidth(poisoned ? 1.8 : 1.4);
            g.setLineDashes(poisoned ? new double[]{ 6, 5 } : null);
            drawArrow(g, nodeX[a], nodeY[a], nodeX[b], nodeY[b], radius);
        }
        g.setLineDashes((double[]) null);

        // 2. Lo resaltado: la ruta maxima, o el ciclo culpable si el churun es infinito.
        if (current.outcome() == MissionThreeSolver.Outcome.VALUE) {
            highlightWalk(g, current.route(), Math.min(step, Math.max(0, current.route().length - 1)),
                    Theme.CHURUN, radius);
        } else if (current.outcome() == MissionThreeSolver.Outcome.INFINITE
                && current.cycle().length > 0) {
            int[] cycle = current.cycle();
            // El ciclo se recorre en bucle, dando vueltas indefinidamente.
            int[] loop = new int[cycle.length + 1];
            System.arraycopy(cycle, 0, loop, 0, cycle.length);
            loop[cycle.length] = cycle[0];
            highlightWalk(g, loop, step == 0 ? loop.length - 1 : (step % loop.length),
                    Theme.CHURUN, radius);
        }

        // 3. Los pesos, DESPUES del resaltado: pintados antes, la linea dorada
        //    del paseo los tapaba justo en los tramos que mas interesa leer.
        if (edges.size() <= 40) {
            g.setFont(Font.font(11));
            g.setTextAlign(TextAlignment.CENTER);
            for (int e = 0; e < edges.size(); e++) {
                int a = edges.from(e), b = edges.to(e);
                if (a == b) continue;
                double mx = (nodeX[a] + nodeX[b]) / 2, my = (nodeY[a] + nodeY[b]) / 2;
                String weight = String.valueOf(edges.weight(e));
                double chipW = 7 + weight.length() * 6.2;
                g.setFill(Theme.fade(Theme.BG, 0.88));
                g.fillRoundRect(mx - chipW / 2, my - 12, chipW, 14, 6, 6);
                g.setFill(edges.weight(e) < 0 ? Theme.LIMON : Theme.fade(Theme.MUTED, 0.95));
                g.fillText(weight, mx, my - 1);
            }
            g.setTextAlign(TextAlignment.LEFT);
        }

        // 4. Los nodos.
        g.setFont(Font.font(Math.max(10, radius)));
        g.setTextAlign(TextAlignment.CENTER);
        for (int v = 0; v < current.nodes(); v++) {
            Color fill = Theme.PANEL_ALT;
            if (v == current.start()) fill = Theme.POLA;
            else if (v == current.destination()) fill = Theme.NINA;

            g.setFill(fill);
            g.fillOval(nodeX[v] - radius, nodeY[v] - radius, radius * 2, radius * 2);
            g.setStroke(Theme.fade(Theme.TEXT, 0.35));
            g.setLineWidth(1.2);
            g.strokeOval(nodeX[v] - radius, nodeY[v] - radius, radius * 2, radius * 2);

            if (radius >= 10) {
                g.setFill(fill == Theme.PANEL_ALT ? Theme.MUTED : Color.web("#14121F"));
                g.fillText(String.valueOf(v), nodeX[v], nodeY[v] + radius * 0.38);
            }
        }
        g.setTextAlign(TextAlignment.LEFT);
    }

    /** Traza los primeros upTo tramos de un paseo. */
    private void highlightWalk(GraphicsContext g, int[] walk, int upTo, Color tint, double radius) {
        if (walk.length < 2) return;
        g.setStroke(tint);
        g.setLineWidth(4.5);
        for (int i = 0; i < Math.min(upTo, walk.length - 1); i++) {
            drawArrow(g, nodeX[walk[i]], nodeY[walk[i]], nodeX[walk[i + 1]], nodeY[walk[i + 1]], radius);
        }
    }

    /** Linea con punta de flecha, recortada para no quedar tapada por el nodo destino. */
    private void drawArrow(GraphicsContext g, double x1, double y1, double x2, double y2, double radius) {
        double dx = x2 - x1, dy = y2 - y1;
        double length = Math.max(1e-6, Math.hypot(dx, dy));
        double ux = dx / length, uy = dy / length;

        double endX = x2 - ux * (radius + 2);
        double endY = y2 - uy * (radius + 2);
        g.strokeLine(x1 + ux * radius, y1 + uy * radius, endX, endY);

        double head = 8;
        double angle = Math.atan2(uy, ux);
        g.strokeLine(endX, endY,
                endX - head * Math.cos(angle - Math.PI / 7), endY - head * Math.sin(angle - Math.PI / 7));
        g.strokeLine(endX, endY,
                endX - head * Math.cos(angle + Math.PI / 7), endY - head * Math.sin(angle + Math.PI / 7));
    }
}
