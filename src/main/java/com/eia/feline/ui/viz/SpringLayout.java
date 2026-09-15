package com.eia.feline.ui.viz;

import com.eia.feline.algo.graph.EdgeList;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Colocacion de los nodos de un grafo por simulacion de fuerzas (Fruchterman-
 * Reingold simplificado), escrita a mano.
 *
 * El enunciado prohibe las librerias de grafos para el nucleo algoritmico y
 * permite una libreria solo para dibujar; en vez de agregar una dependencia por
 * esto, son unas cien lineas: cada par de nodos se repele, cada PAR CONECTADO
 * tira de sus extremos, y una "temperatura" que baja evita que el sistema oscile
 * para siempre.
 *
 * Es O(iteraciones * N^2) por la repulsion de todos contra todos, lo cual solo es
 * aceptable porque la seccion 2.3 limita el dibujo a 60 nodos (Misiones 2 y 3) y
 * 100 (Mision 4). Por encima de ese tope no se dibuja, asi que esto nunca corre
 * sobre los 10.000 nodos que si admite el algoritmo.
 */
public final class SpringLayout {

    /** Cuanto puede estirarse un eje respecto al otro al encajar en el recuadro. */
    private static final double MAX_STRETCH = 2.2;

    private SpringLayout() {}

    /**
     * Devuelve {xs, ys} con coordenadas ya escaladas al rectangulo pedido y con
     * una separacion minima garantizada entre nodos.
     *
     * @param minSeparation distancia minima entre centros, en pixeles. Se pasa el
     *                      diametro del nodo mas un margen, para que las etiquetas
     *                      quepan y los circulos no se toquen.
     */
    public static double[][] compute(int n, EdgeList edges, double width, double height,
                                     double margin, double minSeparation, long seed) {
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
        if (n == 1) {
            return new double[][]{ { width / 2 }, { height / 2 } };
        }

        // UN SOLO RESORTE POR PAR CONECTADO.
        // Si se recorriera la lista de aristas tal cual, un par con varias aristas
        // entre el (frecuente: el enunciado admite repetidas, y en la Mision 3 son
        // dirigidas, asi que a->b y b->a son dos entradas) recibiria el tiron
        // multiplicado y los dos nodos quedarian pegados uno encima del otro. Eso
        // es exactamente lo que pasaba con el ejemplo de la Mision 3, donde 1 y 2
        // tienen dos pasadizos y salian encimados.
        Set<Long> pairs = new HashSet<>();
        for (int e = 0; e < edges.size(); e++) {
            int a = edges.from(e), b = edges.to(e);
            if (a == b) continue;                       // un lazo no mueve nada
            pairs.add(key(a, b));
        }

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

            // Atraccion, una vez por par conectado.
            for (long pair : pairs) {
                int a = (int) (pair >> 32), b = (int) (pair & 0xFFFFFFFFL);
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

        double[][] placed = scale(x, y, width, height, margin);

        // La separacion pedida se recorta a lo que el lienzo puede dar de si.
        // Si se exige mas de lo que cabe, la pasada de separacion empuja a todos
        // contra los bordes, se pelea con el recorte al rectangulo y el resultado
        // es peor que no separar nada: un amasijo pegado al marco.
        double usableW = Math.max(1, width - 2 * margin);
        double usableH = Math.max(1, height - 2 * margin);
        double affordable = Math.sqrt((usableW * usableH) / n) * 0.85;
        separate(placed[0], placed[1], Math.min(minSeparation, affordable), width, height, margin);
        return placed;
    }

    /** Clave canonica de un par no ordenado, empaquetada en un long. */
    private static long key(int a, int b) {
        int lo = Math.min(a, b), hi = Math.max(a, b);
        return ((long) lo << 32) | (hi & 0xFFFFFFFFL);
    }

    /**
     * Empuja los nodos que queden mas cerca que minSeparation.
     *
     * La simulacion de fuerzas equilibra el grafo COMPLETO, pero no garantiza nada
     * sobre un par concreto: en grafos alargados o muy conectados siguen saliendo
     * nodos casi encimados, y entonces no se leen ni su numero ni el peso de sus
     * aristas. Estas pasadas son la garantia dura que la simulacion no da.
     */
    private static void separate(double[] x, double[] y, double minSeparation,
                                 double width, double height, double margin) {
        int n = x.length;
        if (n < 2 || minSeparation <= 0) return;

        for (int pass = 0; pass < 60; pass++) {
            boolean moved = false;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double ux = x[j] - x[i], uy = y[j] - y[i];
                    double dist = Math.hypot(ux, uy);

                    if (dist >= minSeparation) continue;

                    // Dos nodos exactamente encima: se separan en una direccion
                    // arbitraria pero estable, o la division de abajo seria por cero.
                    if (dist < 1e-6) { ux = 1; uy = 0; dist = 1e-6; }

                    double push = (minSeparation - dist) / 2 + 0.5;
                    double nx = ux / dist * push, ny = uy / dist * push;
                    x[i] -= nx; y[i] -= ny;
                    x[j] += nx; y[j] += ny;
                    moved = true;
                }
            }
            // Se devuelven al rectangulo los que se hayan salido al empujar.
            for (int i = 0; i < n; i++) {
                x[i] = Math.max(margin, Math.min(width - margin, x[i]));
                y[i] = Math.max(margin, Math.min(height - margin, y[i]));
            }
            if (!moved) break;
        }
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

        // Cada eje se escala por su cuenta, pero con un tope de deformacion.
        //
        // Conservar la proporcion exacta parecia lo correcto y dejaba el dibujo
        // apinado: en un recuadro ancho y bajo, un grafo de forma cuadrada se
        // escala segun la ALTURA -- el lado que aprieta -- y todo el ancho
        // sobrante se queda vacio, con los nodos amontonados en el centro.
        //
        // Estirando el eje holgado hasta MAX_STRETCH veces el otro, el grafo se
        // reparte por el recuadro. Como los nodos se siguen dibujando redondos,
        // lo unico que se deforma son las distancias, y a 2,2x no se nota.
        double fx = usableW / spanX;
        double fy = usableH / spanY;
        double base = Math.min(fx, fy);
        double cap = base * MAX_STRETCH;
        fx = Math.min(fx, cap);
        fy = Math.min(fy, cap);

        // Centrado: lo que sobre en cada eje se reparte a los dos lados.
        double offsetX = margin + (usableW - spanX * fx) / 2.0;
        double offsetY = margin + (usableH - spanY * fy) / 2.0;

        double[] outX = new double[n], outY = new double[n];
        for (int i = 0; i < n; i++) {
            outX[i] = offsetX + (x[i] - minX) * fx;
            outY[i] = offsetY + (y[i] - minY) * fy;
        }
        return new double[][]{ outX, outY };
    }
}
