package com.eia.feline.ui.viz;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Arrastre de nodos, zoom con la rueda y pan de fondo para un Canvas que ya se
 * pinta solo a partir de dos arreglos nodeX[]/nodeY[] en coordenadas de pixel del
 * propio canvas (GraphVisualizer, MaxWalkVisualizer, MstVisualizer).
 *
 * No toca como se dibuja ni sabe nada de grafos: solo traduce eventos de mouse en
 * (a) mover una posicion dentro de esos arreglos, o (b) una transformada (escala
 * + traslacion) sobre el Node del propio Canvas. El truco de fondo es que un
 * Canvas es un Node como cualquier otro: se puede escalar y trasladar sin tocar
 * el GraphicsContext, y JavaFX entrega MouseEvent.getX()/getY() ya en el sistema
 * de coordenadas LOCAL del nodo (el de antes de esa transformada), que es
 * exactamente el mismo sistema en el que viven nodeX[]/nodeY[]. Por eso el
 * hit-test contra los nodos no necesita saber nada de zoom ni de pan.
 *
 * Los arreglos se piden por Supplier y no por referencia fija porque las tres
 * pantallas REEMPLAZAN el arreglo entero cada vez que recalculan el layout
 * (SpringLayout.compute devuelve arreglos nuevos); una referencia capturada una
 * sola vez quedaria apuntando al arreglo viejo despues del primer recalculo.
 */
public final class GraphCanvasInteraction {

    private static final double MIN_SCALE = 0.35;
    private static final double MAX_SCALE = 3.5;

    /** Margen extra alrededor del radio del nodo para que agarrarlo con el mouse no sea milimetrico. */
    private static final double HIT_SLOP = 6;

    private final Canvas canvas;
    private final Supplier<double[]> xs;
    private final Supplier<double[]> ys;
    private final IntSupplier nodeCount;
    private final DoubleSupplier radius;
    private final Runnable repaint;
    private final Runnable onNodeDragged;

    private int draggedNode = -1;
    private boolean panning;
    private double lastScreenX;
    private double lastScreenY;

    /**
     * @param clipTarget    el contenedor que no debe dejar ver el canvas fuera de sus
     *                      bordes una vez que el pan/zoom lo mueva; normalmente el Pane
     *                      que envuelve al canvas.
     * @param onNodeDragged se llama cada vez que un arrastre mueve un nodo, para que la
     *                      pantalla sepa que el layout ya no es el automatico y no lo
     *                      pise en el proximo resize.
     */
    public GraphCanvasInteraction(Canvas canvas, Region clipTarget,
                                   Supplier<double[]> xs, Supplier<double[]> ys,
                                   IntSupplier nodeCount, DoubleSupplier radius,
                                   Runnable repaint, Runnable onNodeDragged) {
        this.canvas = canvas;
        this.xs = xs;
        this.ys = ys;
        this.nodeCount = nodeCount;
        this.radius = radius;
        this.repaint = repaint;
        this.onNodeDragged = onNodeDragged;

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(clipTarget.widthProperty());
        clip.heightProperty().bind(clipTarget.heightProperty());
        clipTarget.setClip(clip);

        canvas.setOnMousePressed(this::onPressed);
        canvas.setOnMouseDragged(this::onDragged);
        canvas.setOnMouseReleased(e -> { draggedNode = -1; panning = false; });
        canvas.setOnScroll(this::onScroll);
    }

    private void onPressed(MouseEvent e) {
        double[] x = xs.get(), y = ys.get();
        int n = Math.min(nodeCount.getAsInt(), x.length);
        double hit = radius.getAsDouble() + HIT_SLOP;

        draggedNode = -1;
        for (int i = 0; i < n; i++) {
            if (Math.hypot(e.getX() - x[i], e.getY() - y[i]) <= hit) { draggedNode = i; break; }
        }
        if (draggedNode < 0) {
            panning = true;
            lastScreenX = e.getScreenX();
            lastScreenY = e.getScreenY();
        }
    }

    private void onDragged(MouseEvent e) {
        if (draggedNode >= 0) {
            xs.get()[draggedNode] = e.getX();
            ys.get()[draggedNode] = e.getY();
            onNodeDragged.run();
            repaint.run();
        } else if (panning) {
            // El pan se mide en coordenadas de PANTALLA, no locales: las locales
            // cambian de significado a mitad del arrastre en cuanto se toca la
            // traslacion, y el gesto se sentiria a saltos.
            canvas.setTranslateX(canvas.getTranslateX() + (e.getScreenX() - lastScreenX));
            canvas.setTranslateY(canvas.getTranslateY() + (e.getScreenY() - lastScreenY));
            lastScreenX = e.getScreenX();
            lastScreenY = e.getScreenY();
        }
    }

    /** Zoom centrado en el cursor: el punto bajo el mouse no se mueve al escalar. */
    private void onScroll(ScrollEvent e) {
        double oldScale = canvas.getScaleX();
        double factor = Math.pow(1.0015, e.getDeltaY());
        double newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, oldScale * factor));
        if (newScale == oldScale) return;

        // Pivote por defecto de scaleX/scaleY: el centro del canvas. Para que el
        // punto bajo el cursor no salte hay que compensar esa diferencia en la
        // traslacion antes de aplicar la nueva escala.
        double cx = canvas.getWidth() / 2, cy = canvas.getHeight() / 2;
        canvas.setTranslateX(canvas.getTranslateX() + (e.getX() - cx) * (oldScale - newScale));
        canvas.setTranslateY(canvas.getTranslateY() + (e.getY() - cy) * (oldScale - newScale));
        canvas.setScaleX(newScale);
        canvas.setScaleY(newScale);
        e.consume();
    }

    /** Vuelve al zoom y pan originales. No toca las posiciones de los nodos. */
    public void resetView() {
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        canvas.setTranslateX(0);
        canvas.setTranslateY(0);
    }
}
