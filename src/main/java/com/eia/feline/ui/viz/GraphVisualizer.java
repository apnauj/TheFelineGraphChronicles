package com.eia.feline.ui.viz;

import com.eia.feline.algo.graph.EdgeList;
import com.eia.feline.missions.MissionTwoSolver;
import com.eia.feline.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;

/**
 * Dibujo de la Mision 2: la red, los nodos que Dijkstra va resolviendo y la ruta
 * mas barata.
 *
 * La animacion recorre settleOrder, que es el orden en que los nodos salen del
 * heap. Se ve la propiedad que hace correcto a Dijkstra: el frente avanza
 * SIEMPRE por el nodo pendiente mas barato, nunca por el mas cercano en saltos.
 * Cuando un nodo se resuelve, su costo definitivo aparece encima.
 */
public final class GraphVisualizer implements Visualizer<MissionTwoSolver.Case> {

    /** Seccion 2.3: por encima de 60 nodos no se dibuja. */
    private static final int MAX_NODES = 60;

    private final Canvas canvas = new Canvas(600, 600);
    private final Pane canvasHolder = new Pane(canvas);
    private final VBox root = new VBox(10);
    private final Label scoreboard = new Label();
    private final Label legend = new Label();
    private final Button resetViewButton = new Button("Restablecer vista");
    private final GraphCanvasInteraction interaction;

    private MissionTwoSolver.Case current;
    private double[] nodeX = new double[0];
    private double[] nodeY = new double[0];
    private int step;

    /** Para cada arista: en que carril de su par (a,b) le toca, y cuantas comparten ese par. */
    private int[] edgeSlot = new int[0];
    private int[] edgeGroupSize = new int[0];

    /** true en cuanto el usuario arrastra un nodo: el proximo resize ya no debe pisarle el layout. */
    private boolean manualLayout;

    public GraphVisualizer() {
        scoreboard.getStyleClass().add("scoreboard");
        legend.getStyleClass().add("caption");
        legend.setText("Nodo resuelto = cian    Ruta mas barata = dorada    S = inicio, D = destino"
                + "    ·    rueda = zoom, arrastra el fondo = mover, arrastra un nodo = reacomodar");

        resetViewButton.getStyleClass().addAll("button-ghost", "icon-button");
        resetViewButton.setOnAction(e -> resetView());

        HBox legendRow = new HBox(12, legend, resetViewButton);
        legendRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(legend, Priority.ALWAYS);

        canvasHolder.setMinSize(0, 0);
        VBox.setVgrow(canvasHolder, Priority.ALWAYS);
        canvasHolder.widthProperty().addListener((o, a, b) -> relayout());
        canvasHolder.heightProperty().addListener((o, a, b) -> relayout());

        interaction = new GraphCanvasInteraction(canvas, canvasHolder,
                () -> nodeX, () -> nodeY,
                () -> current == null ? 0 : current.nodes(),
                this::currentRadius,
                this::repaint,
                () -> manualLayout = true);

        root.setPadding(new Insets(12));
        root.getChildren().addAll(scoreboard, legendRow, canvasHolder);
    }

    /** Vuelve al layout automatico y al zoom/pan originales. */
    private void resetView() {
        manualLayout = false;
        interaction.resetView();
        relayout();
    }

    /** Radio del nodo; un solo sitio porque lo necesitan el dibujo, el layout y el hit-test del arrastre. */
    private double currentRadius() {
        return Math.max(11, Math.min(24, 380.0 / Math.max(5, current == null ? 8 : current.nodes())));
    }

    @Override
    public Region node() { return root; }

    @Override
    public boolean withinDrawBudget(MissionTwoSolver.Case c) {
        return c.nodes() <= MAX_NODES;
    }

    @Override
    public String overBudgetMessage(MissionTwoSolver.Case c) {
        return "La red tiene " + c.nodes() + " nodos y el limite para dibujar es de "
                + MAX_NODES + ". Se omitio el dibujo por el tamano de la instancia;"
                + " la respuesta se calculo completa de todas formas.";
    }

    @Override
    public void render(MissionTwoSolver.Case c) {
        this.current = c;
        this.step = 0;
        this.manualLayout = false;
        interaction.resetView();
        computeEdgeSlots();
        scoreboard.setText(c.reachable()
                ? "Costo minimo: " + c.cost()
                : "Nina is very sad");
        scoreboard.setTextFill(c.reachable() ? Theme.CHURUN : Theme.LIMON);
        relayout();
    }

    /**
     * El enunciado permite conexiones repetidas entre el mismo par de nodos (se
     * guardan todas, sin deduplicar). Dibujadas como rectas quedarian exactamente
     * una encima de otra y solo se veria la ultima. Aqui se numera cada arista
     * dentro de su par -- "carril" 0, 1, 2... -- para que quien dibuja sepa
     * cuantas comparten el par y en que carril le toca curvarse a cada una.
     */
    private void computeEdgeSlots() {
        EdgeList edges = current.edges();
        int m = edges.size();
        edgeSlot = new int[m];
        edgeGroupSize = new int[m];

        Map<Long, Integer> countSoFar = new HashMap<>();
        for (int e = 0; e < m; e++) {
            long key = pairKey(edges.from(e), edges.to(e));
            edgeSlot[e] = countSoFar.merge(key, 1, Integer::sum) - 1;
        }
        for (int e = 0; e < m; e++) {
            edgeGroupSize[e] = countSoFar.get(pairKey(edges.from(e), edges.to(e)));
        }
    }

    /** Clave canonica de un par no ordenado, empaquetada en un long (igual que en SpringLayout). */
    private static long pairKey(int a, int b) {
        int lo = Math.min(a, b), hi = Math.max(a, b);
        return ((long) lo << 32) | (hi & 0xFFFFFFFFL);
    }

    /**
     * Punto de control de la curva entre a y b para el carril que le toco. Si es
     * la unica arista de su par, el "control" es el propio punto medio -- eso es
     * lo que hace que se dibuje recta.
     *
     * La perpendicular se toma SIEMPRE en el sentido canonico (del indice menor
     * al mayor) y solo despues se elige el lado segun el carril: si se tomara en
     * el sentido de la arista, una arista a->b y su gemela b->a invertirian el
     * vector Y el lado a la vez, los dos signos se cancelarian, y las dos
     * curvas caerian una encima de la otra otra vez.
     */
    private double[] controlPoint(int a, int b, int slot, int groupSize, double radius) {
        double mx = (nodeX[a] + nodeX[b]) / 2, my = (nodeY[a] + nodeY[b]) / 2;
        if (groupSize <= 1) return new double[]{ mx, my };

        int lo = Math.min(a, b), hi = Math.max(a, b);
        double dx = nodeX[hi] - nodeX[lo], dy = nodeY[hi] - nodeY[lo];
        double len = Math.max(1e-6, Math.hypot(dx, dy));
        double px = -dy / len, py = dx / len;

        // Carriles alternados y crecientes: 0 -> +1, 1 -> -1, 2 -> +2, 3 -> -2...
        // Se acota la magnitud para que un par con muchas aristas repetidas no
        // termine con una curva absurdamente ancha.
        int magnitude = Math.min(6, slot / 2 + 1);
        double sign = (slot % 2 == 0) ? 1 : -1;
        double offset = sign * magnitude * (radius * 1.6 + 18);

        return new double[]{ mx + px * offset, my + py * offset };
    }

    @Override
    public int prepareSteps(MissionTwoSolver.Case c) {
        // Un paso por nodo resuelto, mas el recorrido final de la ruta.
        int route = c.reachable() ? c.route().length : 0;
        return c.shortestPath().settledCount() + route;
    }

    @Override
    public void showStep(int step) {
        this.step = step;
        repaint();
    }

    /**
     * Recalcula posiciones (solo cuando cambia el caso o el tamano, y solo si el
     * usuario no las arrastro a mano) y repinta.
     */
    private void relayout() {
        canvas.setWidth(Math.max(1, canvasHolder.getWidth()));
        canvas.setHeight(Math.max(1, canvasHolder.getHeight()));

        if (current != null && canvas.getWidth() > 1 && canvas.getHeight() > 1 && !manualLayout) {
            double radius = currentRadius();
            double[][] positions = SpringLayout.compute(
                    current.nodes(), current.edges(),
                    canvas.getWidth(), canvas.getHeight(), radius + 18, radius * 2 + 30,
                    // Semilla fija: el mismo grafo se dibuja siempre igual, lo que
                    // hace que la demostracion sea reproducible.
                    1234L);
            nodeX = positions[0];
            nodeY = positions[1];
        }
        repaint();
    }

    private void repaint() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.setFill(Theme.PAPER);
        g.fillRect(0, 0, w, h);
        if (current == null || nodeX.length == 0) return;

        EdgeList edges = current.edges();
        int settledShown = Math.min(step, current.shortestPath().settledCount());
        boolean[] settled = new boolean[current.nodes()];
        int[] order = current.shortestPath().settleOrder();
        for (int i = 0; i < settledShown; i++) settled[order[i]] = true;

        double radius = currentRadius();

        // 1. Todas las conexiones, apagadas. Si dos o mas comparten el mismo par
        //    de nodos se curvan en carriles distintos; si no, van rectas.
        g.setLineWidth(1.4);
        g.setStroke(Theme.fade(Theme.INK, 0.9));
        for (int e = 0; e < edges.size(); e++) {
            int a = edges.from(e), b = edges.to(e);
            if (a == b) continue;                       // los lazos no se dibujan
            int groupSize = edgeGroupSize[e];
            if (groupSize <= 1) {
                g.strokeLine(nodeX[a], nodeY[a], nodeX[b], nodeY[b]);
            } else {
                double[] ctrl = controlPoint(a, b, edgeSlot[e], groupSize, radius);
                g.beginPath();
                g.moveTo(nodeX[a], nodeY[a]);
                g.quadraticCurveTo(ctrl[0], ctrl[1], nodeX[b], nodeY[b]);
                g.stroke();
            }
        }

        // 2. La ruta mas barata, una vez terminada la exploracion.
        int[] route = current.reachable() ? current.route() : new int[0];
        int settledTotal = current.shortestPath().settledCount();
        if (route.length > 0 && step >= settledTotal) {
            int walked = Math.min(route.length, step - settledTotal + 1);
            // Dos pasadas: la tinta gruesa debajo, el dorado encima.
            for (int pass = 0; pass < 2; pass++) {
                g.setStroke(pass == 0 ? Theme.INK : Theme.CHURUN);
                g.setLineWidth(pass == 0 ? 9 : 5);
                g.beginPath();
                for (int i = 0; i < walked; i++) {
                    if (i == 0) g.moveTo(nodeX[route[i]], nodeY[route[i]]);
                    else g.lineTo(nodeX[route[i]], nodeY[route[i]]);
                }
                g.stroke();
            }
        }

        // 3. Pesos, solo si hay sitio para leerlos, y DESPUES de la ruta: pintados
        //    antes, la linea dorada los tapaba en los tramos que mas interesa leer.
        if (edges.size() <= 40) {
            g.setFont(Font.font(11));
            g.setTextAlign(TextAlignment.CENTER);
            for (int e = 0; e < edges.size(); e++) {
                int a = edges.from(e), b = edges.to(e);
                if (a == b) continue;
                // Sobre la curva real y no sobre el punto medio de la recta: con
                // una arista curva, B(0.5) = mid + 0.5*(control - mid). Con una
                // sola arista en el par, control == mid y da lo mismo de siempre.
                double[] ctrl = controlPoint(a, b, edgeSlot[e], edgeGroupSize[e], radius);
                double mx = (nodeX[a] + nodeX[b]) / 2 + 0.5 * (ctrl[0] - (nodeX[a] + nodeX[b]) / 2);
                double my = (nodeY[a] + nodeY[b]) / 2 + 0.5 * (ctrl[1] - (nodeY[a] + nodeY[b]) / 2);
                String weight = String.valueOf(edges.weight(e));
                double chipW = 7 + weight.length() * 6.2;
                g.setFill(Theme.fade(Theme.PAPER, 0.85));
                g.fillRoundRect(mx - chipW / 2, my - 12, chipW, 14, 6, 6);
                g.setFill(Theme.fade(Theme.INK_SOFT, 0.95));
                g.fillText(weight, mx, my - 1);
            }
            g.setTextAlign(TextAlignment.LEFT);
        }

        // 4. Los nodos.
        g.setFont(Font.font(Math.max(10, radius)));
        g.setTextAlign(TextAlignment.CENTER);
        for (int v = 0; v < current.nodes(); v++) {
            boolean isStart = v == current.start();
            boolean isDest = v == current.destination();

            Color fill = settled[v] ? Theme.MINERVA : Theme.PAPER_DEEP;
            if (isStart) fill = Theme.POLA;
            if (isDest) fill = Theme.NINA;

            g.setFill(fill);
            g.fillOval(nodeX[v] - radius, nodeY[v] - radius, radius * 2, radius * 2);
            g.setStroke(Theme.INK);
            g.setLineWidth(2.6);
            g.strokeOval(nodeX[v] - radius, nodeY[v] - radius, radius * 2, radius * 2);

            // El ultimo nodo resuelto se resalta: es el que Dijkstra acaba de sacar del heap.
            if (settledShown > 0 && order[settledShown - 1] == v) {
                g.setStroke(Theme.CHURUN);
                g.setLineWidth(3);
                g.strokeOval(nodeX[v] - radius - 4, nodeY[v] - radius - 4,
                        (radius + 4) * 2, (radius + 4) * 2);
            }

            if (radius >= 11) {
                g.setFill(settled[v] || isStart || isDest ? Theme.PAPER : Theme.INK_SOFT);
                g.fillText(String.valueOf(v), nodeX[v], nodeY[v] + radius * 0.38);
            }

            // Costo definitivo encima del nodo, en cuanto se resuelve. Va sobre una
            // pastilla oscura porque si no se pierde encima de las aristas.
            if (settled[v]) {
                String cost = String.valueOf(current.shortestPath().costTo(v));
                g.setFont(Font.font(11));
                double chipW = 8 + cost.length() * 6.4;
                double chipY = nodeY[v] - radius - 16;
                g.setFill(Theme.fade(Theme.PAPER, 0.92));
                g.fillRoundRect(nodeX[v] - chipW / 2, chipY, chipW, 15, 7, 7);
                g.setFill(Theme.CHURUN);
                g.fillText(cost, nodeX[v], chipY + 11);
                g.setFont(Font.font(Math.max(10, radius)));
            }
        }
        g.setTextAlign(TextAlignment.LEFT);
    }
}
