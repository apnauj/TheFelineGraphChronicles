package com.eia.feline.ui.viz;

import com.eia.feline.algo.graph.EdgeList;
import com.eia.feline.missions.MissionFourSolver;
import com.eia.feline.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Dibujo de la Mision 4: la red de intersecciones y la cola de cables ordenada.
 *
 * La cola de la derecha es la mitad interesante. Kruskal no es mas que "mira los
 * cables de mas barato a mas caro y quedate con el que una dos partes todavia
 * separadas", y eso se ve literalmente: cada cable se acepta (se vuelve dorado en
 * el mapa) o se descarta por cerrar un ciclo (se tacha). El contador del total va
 * subiendo solo con los aceptados.
 */
public final class MstVisualizer implements Visualizer<MissionFourSolver.Case> {

    /** Seccion 2.3: hasta 100 intersecciones y 300 cables. */
    private static final int MAX_NODES = 100;
    private static final int MAX_CABLES = 300;

    private static final double ROW_H = 20;

    private final Canvas canvas = new Canvas(600, 480);
    private final Pane canvasHolder = new Pane(canvas);
    private final Canvas queueCanvas = new Canvas(230, 100);
    private final ScrollPane queueScroll = new ScrollPane(new Pane(queueCanvas));
    private final VBox root = new VBox(8);
    private final Label scoreboard = new Label();
    private final Label queueLabel = new Label("CABLES, DE MAS BARATO A MAS CARO");
    private final Label mapLegend = new Label(
            "Rueda = zoom, arrastra el fondo = mover, arrastra un nodo = reacomodar");
    private final Button resetViewButton = new Button("Restablecer vista");
    private final GraphCanvasInteraction interaction;

    private MissionFourSolver.Case current;
    private double[] nodeX = new double[0];
    private double[] nodeY = new double[0];
    private int step;

    /** true en cuanto el usuario arrastra un nodo: el proximo resize ya no debe pisarle el layout. */
    private boolean manualLayout;

    public MstVisualizer() {
        scoreboard.getStyleClass().add("scoreboard");
        queueLabel.getStyleClass().add("section-label");
        mapLegend.getStyleClass().add("caption");

        queueScroll.getStyleClass().add("panel-sunken");
        queueScroll.setPannable(true);
        queueScroll.setMinWidth(250);
        queueScroll.setPrefWidth(250);

        resetViewButton.getStyleClass().addAll("button-ghost", "icon-button");
        resetViewButton.setOnAction(e -> resetView());

        canvasHolder.setMinSize(0, 0);
        canvasHolder.widthProperty().addListener((o, a, b) -> relayout());
        canvasHolder.heightProperty().addListener((o, a, b) -> relayout());

        interaction = new GraphCanvasInteraction(canvas, canvasHolder,
                () -> nodeX, () -> nodeY,
                () -> current == null ? 0 : current.nodes(),
                this::currentRadius,
                this::repaint,
                () -> manualLayout = true);

        HBox mapHeader = new HBox(12, mapLegend, resetViewButton);
        mapHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(mapLegend, Priority.ALWAYS);
        VBox mapColumn = new VBox(6, mapHeader, canvasHolder);
        VBox.setVgrow(canvasHolder, Priority.ALWAYS);
        HBox.setHgrow(mapColumn, Priority.ALWAYS);

        VBox queueColumn = new VBox(6, queueLabel, queueScroll);
        VBox.setVgrow(queueScroll, Priority.ALWAYS);

        HBox split = new HBox(12, mapColumn, queueColumn);
        VBox.setVgrow(split, Priority.ALWAYS);

        root.setPadding(new Insets(12));
        root.getChildren().addAll(scoreboard, split);
    }

    /** Vuelve al layout automatico y al zoom/pan originales. */
    private void resetView() {
        manualLayout = false;
        interaction.resetView();
        relayout();
    }

    /** Radio del nodo; un solo sitio porque lo necesitan el dibujo, el layout y el hit-test del arrastre. */
    private double currentRadius() {
        return Math.max(10, Math.min(22, 340.0 / Math.max(5, current == null ? 8 : current.nodes())));
    }

    @Override
    public Region node() { return root; }

    @Override
    public boolean withinDrawBudget(MissionFourSolver.Case c) {
        return c.nodes() <= MAX_NODES && c.cables().size() <= MAX_CABLES;
    }

    @Override
    public String overBudgetMessage(MissionFourSolver.Case c) {
        return "La red tiene " + c.nodes() + " intersecciones y " + c.cables().size()
                + " cables; el limite para dibujar es de " + MAX_NODES + " y " + MAX_CABLES
                + ". Se omitio el dibujo por el tamano de la instancia; la respuesta"
                + " se calculo completa de todas formas.";
    }

    @Override
    public void render(MissionFourSolver.Case c) {
        this.current = c;
        this.step = c.order().length;        // al pintar sin animar se muestra el arbol final
        this.manualLayout = false;
        interaction.resetView();
        scoreboard.setText(c.connected()
                ? "Cable total: " + c.total()
                : "Limon cut too many cables  (" + c.components() + " partes sueltas)");
        scoreboard.setTextFill(c.connected() ? Theme.CHURUN : Theme.LIMON);
        relayout();
    }

    @Override
    public int prepareSteps(MissionFourSolver.Case c) {
        return c.order().length;             // un paso por cable examinado
    }

    @Override
    public void showStep(int step) {
        this.step = step;
        repaint();
        repaintQueue();
    }

    private void relayout() {
        canvas.setWidth(Math.max(1, canvasHolder.getWidth()));
        canvas.setHeight(Math.max(1, canvasHolder.getHeight()));
        if (current != null && canvas.getWidth() > 1 && canvas.getHeight() > 1 && !manualLayout) {
            double radius = currentRadius();
            double[][] positions = SpringLayout.compute(current.nodes(), current.cables(),
                    canvas.getWidth(), canvas.getHeight(), radius + 16, radius * 2 + 22, 1234L);
            nodeX = positions[0];
            nodeY = positions[1];
        }
        repaint();
        repaintQueue();
    }

    private void repaint() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.setFill(Theme.PAPER);
        g.fillRect(0, 0, w, h);
        if (current == null || nodeX.length == 0) return;

        EdgeList cables = current.cables();
        int[] order = current.order();
        boolean[] accepted = current.accepted();
        int examined = Math.min(step, order.length);
        double radius = currentRadius();

        // 1. Todos los cables disponibles, apagados.
        g.setLineWidth(1.3);
        g.setStroke(Theme.fade(Theme.INK, 0.85));
        for (int e = 0; e < cables.size(); e++) {
            int a = cables.from(e), b = cables.to(e);
            if (a == b) continue;
            g.strokeLine(nodeX[a], nodeY[a], nodeX[b], nodeY[b]);
        }

        // 2. Los cables ya examinados: dorados si entraron, tachados si cerraban ciclo.
        for (int i = 0; i < examined; i++) {
            int e = order[i];
            int a = cables.from(e), b = cables.to(e);
            if (a == b) continue;

            if (accepted[i]) {
                // Tinta primero, color encima: entintado y coloreado, como una vineta.
                g.setStroke(Theme.INK);
                g.setLineWidth(8);
                g.strokeLine(nodeX[a], nodeY[a], nodeX[b], nodeY[b]);
                g.setStroke(Theme.CHURUN);
                g.setLineWidth(4.5);
                g.strokeLine(nodeX[a], nodeY[a], nodeX[b], nodeY[b]);
            } else if (i == examined - 1) {
                // Solo el rechazo mas reciente se marca, para no llenar el mapa de tachones.
                g.setStroke(Theme.fade(Theme.LIMON, 0.85));
                g.setLineWidth(2.2);
                g.setLineDashes(5, 4);
                g.strokeLine(nodeX[a], nodeY[a], nodeX[b], nodeY[b]);
                g.setLineDashes((double[]) null);
            }
        }

        // 3. Las intersecciones.
        g.setFont(Font.font(Math.max(9, radius)));
        g.setTextAlign(TextAlignment.CENTER);
        for (int v = 0; v < current.nodes(); v++) {
            g.setFill(Theme.MINERVA);
            g.fillOval(nodeX[v] - radius, nodeY[v] - radius, radius * 2, radius * 2);
            g.setStroke(Theme.INK);
            g.setLineWidth(2.6);
            g.strokeOval(nodeX[v] - radius, nodeY[v] - radius, radius * 2, radius * 2);
            if (radius >= 10) {
                g.setFill(Theme.PAPER);
                // Las intersecciones se numeran de 1 a N en el enunciado de esta mision.
                g.fillText(String.valueOf(v + 1), nodeX[v], nodeY[v] + radius * 0.38);
            }
        }
        g.setTextAlign(TextAlignment.LEFT);
    }

    /** La cola ordenada de cables, con el veredicto de cada uno. */
    private void repaintQueue() {
        if (current == null) return;

        int[] order = current.order();
        boolean[] accepted = current.accepted();
        int examined = Math.min(step, order.length);

        double width = Math.max(230, queueScroll.getWidth() - 18);
        queueCanvas.setWidth(width);
        queueCanvas.setHeight(Math.max(10, order.length * ROW_H + 8));

        GraphicsContext g = queueCanvas.getGraphicsContext2D();
        g.setFill(Theme.PAPER);
        g.fillRect(0, 0, queueCanvas.getWidth(), queueCanvas.getHeight());
        g.setFont(Font.font(11.5));

        EdgeList cables = current.cables();
        long running = 0;

        for (int i = 0; i < order.length; i++) {
            int e = order[i];
            double y = 4 + i * ROW_H;
            boolean done = i < examined;
            boolean isCurrent = i == examined - 1;

            if (isCurrent) {
                g.setFill(Theme.fade(Theme.INK, 0.10));
                g.fillRect(2, y, width - 4, ROW_H - 2);
            }

            String label = (cables.from(e) + 1) + " - " + (cables.to(e) + 1);
            String cost = String.valueOf(cables.weight(e));

            if (!done) {
                g.setFill(Theme.fade(Theme.INK_SOFT, 0.55));
            } else if (accepted[i]) {
                running += cables.weight(e);
                g.setFill(Theme.CHURUN);
            } else {
                g.setFill(Theme.fade(Theme.LIMON, 0.85));
            }

            g.fillText(label, 10, y + 14);
            g.fillText(cost, 96, y + 14);

            if (done) {
                g.fillText(accepted[i] ? "aceptado" : "ciclo", 150, y + 14);
                if (accepted[i]) {
                    g.setFill(Theme.fade(Theme.CHURUN, 0.75));
                    g.fillText(String.valueOf(running), 210, y + 14);
                } else {
                    // Tachado sobre el cable descartado.
                    g.setStroke(Theme.fade(Theme.LIMON, 0.7));
                    g.setLineWidth(1);
                    g.strokeLine(8, y + 10, 140, y + 10);
                }
            }
        }
    }
}
