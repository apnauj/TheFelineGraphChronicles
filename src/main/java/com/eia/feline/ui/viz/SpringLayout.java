package com.eia.feline.ui.viz;

import com.eia.feline.algo.graph.EdgeList;

import java.util.Random;

/**
 * Colocacion de los nodos de un grafo por simulacion de fuerzas (Fruchterman-
 * Reingold simplificado), escrita a mano.
 *
 * El enunciado prohibe las librerias de grafos para el nucleo algoritmico y
 * permite una libreria solo para dibujar; en vez de agregar una dependencia por
 * esto, son cincuenta lineas: cada par de nodos se repele, cada arista tira de
 * sus extremos, y una "temperatura" que baja evita que el sistema oscile para
 * siempre.
 *
 * Es O(iteraciones * N^2) por la repulsion de todos contra todos, lo cual solo es
 * aceptable porque la seccion 2.3 limita el dibujo a 60 nodos (Misiones 2 y 3) y
 * 100 (Mision 4). Por encima de ese tope no se dibuja, asi que esto nunca corre
 * sobre los 10.000 nodos que si admite el algoritmo.
 */
public final class SpringLayout {

    private SpringLayout() {}

    /** Devuelve {xs, ys} con coordenadas ya escaladas al rectangulo pedido. */
    public static double[][] compute(int n, EdgeList edges, double width, double height,
                                     double margin, long seed) {
        double[] x = new double[n];
        double[] y = new double[n];

        // Arranque en circulo: es determinista y evita el caso degenerado de que
        // dos nodos nazcan exactamente en el mismo punto (repulsion infinita).
        Random rnd = new Random(seed);
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / Math.max(1, n);
            x[i] = Math.cos(angle) * 100 + rnd.nextDouble() * 2 - 1;
            y[i] = Math.sin(angle) * 100 + rnd.nextDouble() * 2 - 1;
        }
        if (n == 1) return scale(x, y, width, height, margin);

        double area = 200 * 200;
        double k = Math.sqrt(area / n);            // distancia de equilibrio
        double temperature = 100;
        int iterations = 320;

        double[] dx = new double[n];
        double[] dy = new double[n];

        for (int iter = 0; iter < iterations; iter++) {
            java.util.Arrays.fill(dx, 0);
            java.util.Arrays.fill(dy, 0);

            // Repulsion entre todos los pares.
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double ux = x[i] - x[j], uy = y[i] - y[j];
                    double dist = Math.max(0.01, Math.hypot(ux, uy));
                    double force = (k * k) / dist;
                    double fx = ux / dist * force, fy = uy / dist * force;
                    dx[i] += fx; dy[i] += fy;
                    dx[j] -= fx; dy[j] -= fy;
                }
            }

            // Atraccion a lo largo de las aristas.
            for (int e = 0; e < edges.size(); e++) {
                int a = edges.from(e), b = edges.to(e);
                if (a == b) continue;                       // un lazo no mueve nada
                double ux = x[a] - x[b], uy = y[a] - y[b];
                double dist = Math.max(0.01, Math.hypot(ux, uy));
                double force = (dist * dist) / k;
                double fx = ux / dist * force, fy = uy / dist * force;
                dx[a] -= fx; dy[a] -= fy;
                dx[b] += fx; dy[b] += fy;
            }

            // Desplazamiento limitado por la temperatura, que se va enfriando.
            for (int i = 0; i < n; i++) {
                double d = Math.max(0.01, Math.hypot(dx[i], dy[i]));
                double capped = Math.min(d, temperature);
                x[i] += dx[i] / d * capped;
                y[i] += dy[i] / d * capped;
            }
            temperature = Math.max(0.6, temperature * 0.965);
        }

        return scale(x, y, width, height, margin);
    }

    /** Lleva la nube de puntos al rectangulo de dibujo conservando la proporcion. */
    private static double[][] scale(double[] x, double[] y, double width, double height, double margin) {
        int n = x.length;
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            minX = Math.min(minX, x[i]); maxX = Math.max(maxX, x[i]);
            minY = Math.min(minY, y[i]); maxY = Math.max(maxY, y[i]);
        }

        double spanX = Math.max(1e-6, maxX - minX);
        double spanY = Math.max(1e-6, maxY - minY);
        double usableW = Math.max(1, width - 2 * margin);
        double usableH = Math.max(1, height - 2 * margin);
        double factor = Math.min(usableW / spanX, usableH / spanY);

        // Centrado: lo que sobre del eje mas holgado se reparte a los dos lados.
        double offsetX = margin + (usableW - spanX * factor) / 2.0;
        double offsetY = margin + (usableH - spanY * factor) / 2.0;

        double[] outX = new double[n], outY = new double[n];
        for (int i = 0; i < n; i++) {
            outX[i] = offsetX + (x[i] - minX) * factor;
            outY[i] = offsetY + (y[i] - minY) * factor;
        }
        // Un solo nodo queda en una esquina tras el escalado: se centra a mano.
        if (n == 1) { outX[0] = width / 2; outY[0] = height / 2; }
        return new double[][]{ outX, outY };
    }
}
