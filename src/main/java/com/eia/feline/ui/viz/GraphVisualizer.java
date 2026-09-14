package com.eia.feline.ui.viz;

import com.eia.feline.algo.graph.EdgeList;
import com.eia.feline.missions.MissionTwoSolver;
import com.eia.feline.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

    private MissionTwoSolver.Case current;
    private double[] nodeX = new double[0];
    private double[] nodeY = new double[0];
    private int step;

    public GraphVisualizer() {
        scoreboard.getStyleClass().addAll("mono", "title");
        legend.getStyleClass().add("caption");
        legend.setText("Nodo resuelto = cian    Ruta mas barata = dorada    S = inicio, D = destino");

        HBox legendRow = new HBox(legend);
        legendRow.setAlignment(Pos.CENTER_LEFT);

        canvasHolder.setMinSize(0, 0);
        VBox.setVgrow(canvasHolder, Priority.ALWAYS);
        canvasHolder.widthProperty().addListener((o, a, b) -> relayout());
        canvasHolder.heightProperty().addListener((o, a, b) -> relayout());

        root.setPadding(new Insets(12));
        root.getChildren().addAll(scoreboard, legendRow, canvasHolder);
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
        scoreboard.setText(c.reachable()
                ? "Costo minimo: " + c.cost()
                : "Nina is very sad");
        scoreboard.setTextFill(c.reachable() ? Theme.CHURUN : Theme.LIMON);
        relayout();
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

    /** Recalcula posiciones (solo cuando cambia el caso o el tamano) y repinta. */
    private void relayout() {
        canvas.setWidth(Math.max(1, canvasHolder.getWidth()));
        canvas.setHeight(Math.max(1, canvasHolder.getHeight()));

        if (current != null && canvas.getWidth() > 1 && canvas.getHeight() > 1) {
            double margin = 46;
            double[][] positions = SpringLayout.compute(
                    current.nodes(), current.edges(),
                    canvas.getWidth(), canvas.getHeight(), margin,
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
        g.setFill(Theme.BG);
        g.fillRect(0, 0, w, h);
        if (current == null || nodeX.length == 0) return;

        EdgeList edges = current.edges();
        int settledShown = Math.min(step, current.shortestPath().settledCount());
        boolean[] settled = new boolean[current.nodes()];
        int[] order = current.shortestPath().settleOrder();
        for (int i = 0; i < settledShown; i++) settled[order[i]] = true;

        double radius = Math.max(9, Math.min(20, 320.0 / Math.max(4, current.nodes())));

        // 1. Todas las conexiones, apagadas.
        g.setLineWidth(1.4);
        for (int e = 0; e < edges.size(); e++) {
            int a = edges.from(e), b = edges.to(e);
            if (a == b) continue;                       // los lazos no se dibujan
            g.setStroke(Theme.fade(Theme.STROKE, 0.9));
            g.strokeLine(nodeX[a], nodeY[a], nodeX[b], nodeY[b]);
        }

        // 2. Pesos, solo si hay sitio para leerlos.
        if (edges.size() <= 40) {
            g.setFont(Font.font(11));
            g.setTextAlign(TextAlignment.CENTER);
            for (int e = 0; e < edges.size(); e++) {
                int a = edges.from(e), b = edges.to(e);
                if (a == b) continue;
                double mx = (nodeX[a] + nodeX[b]) / 2, my = (nodeY[a] + nodeY[b]) / 2;
                String weight = String.valueOf(edges.weight(e));
                double chipW = 7 + weight.length() * 6.2;
                g.setFill(Theme.fade(Theme.BG, 0.85));
                g.fillRoundRect(mx - chipW / 2, my - 12, chipW, 14, 6, 6);
                g.setFill(Theme.fade(Theme.MUTED, 0.95));
                g.fillText(weight, mx, my - 1);
            }
            g.setTextAlign(TextAlignment.LEFT);
        }

        // 3. La ruta mas barata, una vez terminada la exploracion.
        int[] route = current.reachable() ? current.route() : new int[0];
        int settledTotal = current.shortestPath().settledCount();
        if (route.length > 0 && step >= settledTotal) {
            int walked = Math.min(route.length, step - settledTotal + 1);
            g.setStroke(Theme.CHURUN);
            g.setLineWidth(4.5);
            g.beginPath();
            for (int i = 0; i < walked; i++) {
                if (i == 0) g.moveTo(nodeX[route[i]], nodeY[route[i]]);
                else g.lineTo(nodeX[route[i]], nodeY[route[i]]);
            }
            g.stroke();
        }

        // 4. Los nodos.
        g.setFont(Font.font(Math.max(10, radius)));
        g.setTextAlign(TextAlignment.CENTER);
        for (int v = 0; v < current.nodes(); v++) {
            boolean isStart = v == current.start();
            boolean isDest = v == current.destination();

            Color fill = settled[v] ? Theme.MINERVA : Theme.PANEL_ALT;
            if (isStart) fill = Theme.POLA;
            if (isDest) fill = Theme.NINA;

            g.setFill(fill);
            g.fillOval(nodeX[v] - radius, nodeY[v] - radius, radius * 2, radius * 2);
            g.setStroke(Theme.fade(Theme.TEXT, 0.35));
            g.setLineWidth(1.2);
            g.strokeOval(nodeX[v] - radius, nodeY[v] - radius, radius * 2, radius * 2);

            // El ultimo nodo resuelto se resalta: es el que Dijkstra acaba de sacar del heap.
            if (settledShown > 0 && order[settledShown - 1] == v) {
                g.setStroke(Theme.CHURUN);
                g.setLineWidth(3);
                g.strokeOval(nodeX[v] - radius - 4, nodeY[v] - radius - 4,
                        (radius + 4) * 2, (radius + 4) * 2);
            }

            if (radius >= 11) {
                g.setFill(settled[v] || isStart || isDest ? Color.web("#14121F") : Theme.MUTED);
                g.fillText(String.valueOf(v), nodeX[v], nodeY[v] + radius * 0.38);
            }

            // Costo definitivo encima del nodo, en cuanto se resuelve. Va sobre una
            // pastilla oscura porque si no se pierde encima de las aristas.
            if (settled[v]) {
                String cost = String.valueOf(current.shortestPath().costTo(v));
                g.setFont(Font.font(11));
                double chipW = 8 + cost.length() * 6.4;
                double chipY = nodeY[v] - radius - 16;
                g.setFill(Theme.fade(Theme.BG, 0.92));
                g.fillRoundRect(nodeX[v] - chipW / 2, chipY, chipW, 15, 7, 7);
                g.setFill(Theme.CHURUN);
                g.fillText(cost, nodeX[v], chipY + 11);
                g.setFont(Font.font(Math.max(10, radius)));
            }
        }
        g.setTextAlign(TextAlignment.LEFT);
    }
}
