package com.eia.feline.ui.fx;

import com.eia.feline.ui.theme.Theme;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;

/**
 * Gatos dibujados con figuras primitivas, entintados al estilo del comic.
 *
 * Son marcadores de posicion: el arte definitivo entrara como SVG y como
 * secuencias de PNG generadas a partir de fotos reales, y para cambiarlo basta
 * reemplazar el contenido de estos metodos. Todo lo demas de la interfaz pide "un
 * gato de tal color de tal tamano" y no sabe como esta hecho por dentro.
 *
 * Lo que los hace parecer de comic no es la forma sino el ENTINTADO: cada pieza
 * lleva una linea negra gruesa por fuera (StrokeType.OUTSIDE, para que el trazo
 * no se coma el relleno) y el color va plano, sin degradados.
 */
public final class CatArt {

    private CatArt() {}

    /** Cabeza de gato de frente. size es el ancho total aproximado, en pixeles. */
    public static Group head(Color fur, double size) {
        double r = size / 2.0;
        double ink = Math.max(2, size * 0.035);
        Group g = new Group();

        // Orejas: dos triangulos que asoman por encima del craneo.
        Polygon leftEar = new Polygon(-r * 0.78, -r * 0.35, -r * 0.34, -r * 1.18, -r * 0.08, -r * 0.42);
        Polygon rightEar = new Polygon(r * 0.78, -r * 0.35, r * 0.34, -r * 1.18, r * 0.08, -r * 0.42);
        for (Polygon ear : new Polygon[]{ leftEar, rightEar }) {
            ear.setFill(fur);
            outline(ear, ink);
        }

        Polygon leftInner = new Polygon(-r * 0.60, -r * 0.44, -r * 0.36, -r * 0.94, -r * 0.20, -r * 0.48);
        Polygon rightInner = new Polygon(r * 0.60, -r * 0.44, r * 0.36, -r * 0.94, r * 0.20, -r * 0.48);
        leftInner.setFill(Theme.NINA);
        rightInner.setFill(Theme.NINA);

        Ellipse skull = new Ellipse(0, 0, r, r * 0.88);
        skull.setFill(fur);
        outline(skull, ink);

        // Ojos con brillo: circulo de tinta, pupila y un punto de luz.
        Circle leftEye = new Circle(-r * 0.36, -r * 0.08, r * 0.19, Theme.PAPER);
        Circle rightEye = new Circle(r * 0.36, -r * 0.08, r * 0.19, Theme.PAPER);
        outline(leftEye, ink * 0.8);
        outline(rightEye, ink * 0.8);

        Circle leftPupil = new Circle(-r * 0.33, -r * 0.06, r * 0.10, Theme.INK);
        Circle rightPupil = new Circle(r * 0.39, -r * 0.06, r * 0.10, Theme.INK);
        Circle leftSpark = new Circle(-r * 0.37, -r * 0.11, r * 0.04, Theme.PAPER);
        Circle rightSpark = new Circle(r * 0.35, -r * 0.11, r * 0.04, Theme.PAPER);

        Polygon nose = new Polygon(-r * 0.11, r * 0.22, r * 0.11, r * 0.22, 0, r * 0.38);
        nose.setFill(Theme.NINA);
        outline(nose, ink * 0.7);

        Group whiskers = new Group();
        for (int side = -1; side <= 1; side += 2) {
            for (int i = -1; i <= 1; i++) {
                Line w = new Line(side * r * 0.24, r * 0.26 + i * r * 0.10,
                                  side * r * 1.08, r * 0.16 + i * r * 0.22);
                w.setStroke(Theme.INK);
                w.setStrokeWidth(Math.max(1.4, r * 0.05));
                w.setStrokeLineCap(StrokeLineCap.ROUND);
                whiskers.getChildren().add(w);
            }
        }

        g.getChildren().addAll(leftEar, rightEar, leftInner, rightInner, skull,
                leftEye, rightEye, leftPupil, rightPupil, leftSpark, rightSpark, nose, whiskers);
        return g;
    }

    /** Silueta de villano: cabeza con la mirada entrecerrada. */
    public static Group villain(Color fur, double size) {
        Group g = head(fur, size);
        double r = size / 2.0;
        double ink = Math.max(2, size * 0.035);

        // Dos parpados caidos convierten la mirada en una mueca.
        Polygon leftLid = new Polygon(-r * 0.60, -r * 0.34, -r * 0.12, -r * 0.34, -r * 0.12, -r * 0.04);
        Polygon rightLid = new Polygon(r * 0.60, -r * 0.34, r * 0.12, -r * 0.34, r * 0.12, -r * 0.04);
        for (Polygon lid : new Polygon[]{ leftLid, rightLid }) {
            lid.setFill(fur.darker());
            outline(lid, ink * 0.7);
        }

        g.getChildren().addAll(leftLid, rightLid);
        return g;
    }

    /** Linea de tinta gruesa por fuera, que es lo que da el aspecto de comic. */
    private static void outline(javafx.scene.shape.Shape shape, double width) {
        shape.setStroke(Theme.INK);
        shape.setStrokeWidth(width);
        shape.setStrokeType(StrokeType.OUTSIDE);
    }
}
