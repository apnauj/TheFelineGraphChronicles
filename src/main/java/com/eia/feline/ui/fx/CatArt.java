package com.eia.feline.ui.fx;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;

import static com.eia.feline.ui.theme.Theme.*;

/**
 * Gatos dibujados con figuras primitivas.
 *
 * Son marcadores de posicion: el arte vectorial definitivo entrara como SVG y
 * como secuencias de PNG, y para cambiarlo basta reemplazar el contenido de
 * estos metodos. Todo lo demas de la interfaz pide "un gato de tal color de tal
 * tamano" y no le importa como este hecho por dentro.
 *
 * Se construyen alrededor del origen y escalados a un tamano pedido, para que el
 * llamador pueda colocarlos sin hacer cuentas.
 */
public final class CatArt {

    private CatArt() {}

    /** Cabeza de gato de frente. size es el ancho total aproximado, en pixeles. */
    public static Group head(Color fur, double size) {
        double r = size / 2.0;
        Group g = new Group();

        // Orejas: dos triangulos que asoman por encima del craneo.
        Polygon leftEar = new Polygon(-r * 0.78, -r * 0.35, -r * 0.34, -r * 1.16, -r * 0.10, -r * 0.42);
        Polygon rightEar = new Polygon(r * 0.78, -r * 0.35, r * 0.34, -r * 1.16, r * 0.10, -r * 0.42);
        leftEar.setFill(fur);
        rightEar.setFill(fur);

        Polygon leftInner = new Polygon(-r * 0.60, -r * 0.42, -r * 0.37, -r * 0.92, -r * 0.22, -r * 0.46);
        Polygon rightInner = new Polygon(r * 0.60, -r * 0.42, r * 0.37, -r * 0.92, r * 0.22, -r * 0.46);
        leftInner.setFill(fade(NINA, 0.75));
        rightInner.setFill(fade(NINA, 0.75));

        Ellipse skull = new Ellipse(0, 0, r, r * 0.88);
        skull.setFill(fur);

        Circle leftEye = new Circle(-r * 0.36, -r * 0.08, r * 0.15, Color.web("#1B1526"));
        Circle rightEye = new Circle(r * 0.36, -r * 0.08, r * 0.15, Color.web("#1B1526"));
        Circle leftSpark = new Circle(-r * 0.31, -r * 0.14, r * 0.05, TEXT);
        Circle rightSpark = new Circle(r * 0.41, -r * 0.14, r * 0.05, TEXT);

        Polygon nose = new Polygon(-r * 0.10, r * 0.22, r * 0.10, r * 0.22, 0, r * 0.36);
        nose.setFill(NINA);

        Group whiskers = new Group();
        for (int side = -1; side <= 1; side += 2) {
            for (int i = -1; i <= 1; i++) {
                Line w = new Line(side * r * 0.22, r * 0.26 + i * r * 0.10,
                                  side * r * 1.05, r * 0.16 + i * r * 0.20);
                w.setStroke(fade(TEXT, 0.55));
                w.setStrokeWidth(Math.max(1, r * 0.045));
                w.setStrokeLineCap(StrokeLineCap.ROUND);
                whiskers.getChildren().add(w);
            }
        }

        g.getChildren().addAll(leftEar, rightEar, leftInner, rightInner, skull,
                leftEye, rightEye, leftSpark, rightSpark, nose, whiskers);
        return g;
    }

    /**
     * Gato de perfil en cuatro patas. Devuelve el grupo y deja las patas
     * accesibles para animarlas, porque el ciclo de carrera las rota.
     */
    public static Runner runner(Color fur, double size) {
        double u = size / 100.0;             // unidad: el cuerpo mide 100 unidades de largo
        Group g = new Group();

        Ellipse body = new Ellipse(0, 0, 42 * u, 22 * u);
        body.setFill(fur);

        // Cola, en su propio grupo para poder ondearla.
        Group tail = new Group();
        Line tailLine = new Line(-40 * u, -6 * u, -74 * u, -30 * u);
        tailLine.setStroke(fur);
        tailLine.setStrokeWidth(7 * u);
        tailLine.setStrokeLineCap(StrokeLineCap.ROUND);
        tail.getChildren().add(tailLine);
        tail.setTranslateX(0);

        Group headGroup = head(fur, 46 * u);
        headGroup.setTranslateX(44 * u);
        headGroup.setTranslateY(-16 * u);

        Line[] legs = new Line[4];
        double[] hipX = { -26 * u, -14 * u, 16 * u, 28 * u };
        for (int i = 0; i < 4; i++) {
            Line leg = new Line(hipX[i], 14 * u, hipX[i], 40 * u);
            leg.setStroke(fur.darker());
            leg.setStrokeWidth(6 * u);
            leg.setStrokeLineCap(StrokeLineCap.ROUND);
            legs[i] = leg;
        }

        g.getChildren().add(tail);
        g.getChildren().addAll(legs);
        g.getChildren().addAll(body, headGroup);

        return new Runner(g, legs, tail, u);
    }

    /** Un gato corredor con sus partes animables expuestas. */
    public record Runner(Group node, Line[] legs, Group tail, double unit) {}

    /** Silueta de villano: cabeza con ojos entrecerrados y un aura de maldad. */
    public static Group villain(Color fur, double size) {
        Group g = head(fur, size);
        double r = size / 2.0;

        // Dos parpados caidos convierten la mirada en una mueca.
        Polygon leftLid = new Polygon(-r * 0.58, -r * 0.30, -r * 0.14, -r * 0.30, -r * 0.14, -r * 0.06);
        Polygon rightLid = new Polygon(r * 0.58, -r * 0.30, r * 0.14, -r * 0.30, r * 0.14, -r * 0.06);
        leftLid.setFill(fur.darker());
        rightLid.setFill(fur.darker());

        g.getChildren().addAll(leftLid, rightLid);
        return g;
    }
}
