package com.eia.feline.algo.maxwalk;

import com.eia.feline.algo.graph.EdgeList;

import java.util.Arrays;

/**
 * Floyd-Warshall de MAXIMIZACION para la Mision 3: el mayor churun acumulable en
 * un PASEO (puede repetir nodos y aristas) entre TODOS los pares de nodos.
 *
 * POR QUE FLOYD-WARSHALL EN LA MISION 3: junto con Bellman-Ford (ver
 * BellmanFord.java) es el segundo algoritmo que exige el enunciado. Resuelve
 * todos los pares a la vez -- que es lo que la GUI necesita para dibujar la
 * matriz N x N -- mientras que Bellman-Ford solo resuelve un origen pero es el
 * que identifica el ciclo positivo concreto para resaltarlo. El enunciado exige
 * que las respuestas de ambos coincidan para (S, D); esa comprobacion cruzada
 * vive en MissionThreeSolver, no aqui.
 *
 * Es el mismo triple bucle k-i-j que la version clasica de minimizacion de
 * distancias, con dos cambios: se MAXIMIZA en vez de minimizar (se compara con
 * > en vez de <), y una pasada final marca que pares quedan sin cota en vez de
 * limitarse a mirar la diagonal.
 *
 * "Sin ruta" es el centinela NONE (Long.MIN_VALUE), sobre el que nunca se hace
 * aritmetica: toda actualizacion comprueba antes que las dos mitades del camino
 * (i-k y k-j) sean finitas. Los valores se acotan con CAP: sin ella, un ciclo de
 * ganancia positiva hace que el triple bucle DUPLIQUE el valor en cada vuelta de
 * k (el mismo d[i][k] ya inflado se vuelve a sumar como parte de d[i][k'] en la
 * siguiente iteracion), y desbordaria el long mucho antes de los 100 nodos que
 * permite el enunciado. Cualquier valor legitimo es a lo sumo (N-1) * 1000 =
 * 99.000, muy por debajo de la cota, asi que recortar nunca se confunde con una
 * respuesta real.
 *
 * Complejidad: O(N^3) tiempo, O(N^2) espacio. Con N <= 100 son 10^6 operaciones,
 * de sobra para el limite del enunciado, y a cambio da la matriz completa que la
 * GUI tiene que mostrar.
 */
public final class FloydWarshall {

    /** Centinela de "no hay ruta". Nunca entra en una suma. */
    public static final long NONE = Long.MIN_VALUE;

    /** Techo artificial: por encima de esto el valor solo puede venir de un ciclo positivo. */
    private static final long CAP = 1_000_000_000L;

    private FloydWarshall() {}

    public static AllPairsResult run(int nodes, EdgeList edges) {
        long[][] d = new long[nodes][nodes];
        for (long[] row : d) Arrays.fill(row, NONE);
        for (int i = 0; i < nodes; i++) d[i][i] = 0;

        // Si un par ordenado aparece varias veces, se queda el mejor.
        for (int e = 0; e < edges.size(); e++) {
            int a = edges.from(e), b = edges.to(e);
            long w = edges.weight(e);
            if (d[a][b] == NONE || w > d[a][b]) d[a][b] = w;
        }

        for (int k = 0; k < nodes; k++) {
            for (int i = 0; i < nodes; i++) {
                if (d[i][k] == NONE) continue;         // nunca se suma el centinela
                for (int j = 0; j < nodes; j++) {
                    if (d[k][j] == NONE) continue;
                    long candidate = Math.min(CAP, d[i][k] + d[k][j]);
                    if (d[i][j] == NONE || candidate > d[i][j]) d[i][j] = candidate;
                }
            }
        }

        return new AllPairsResult(d, markUnbounded(nodes, d));
    }

    /**
     * (i, j) es no acotado si y solo si existe k con d[i][k] finito, d[k][k] > 0
     * y d[k][j] finito: se puede llegar de i a un ciclo de ganancia positiva y
     * despues seguir de ahi hacia j. Sin esta pasada la matriz guarda numeros
     * grandes sin sentido en vez de infinitos.
     */
    private static boolean[][] markUnbounded(int nodes, long[][] d) {
        boolean[][] unbounded = new boolean[nodes][nodes];
        for (int k = 0; k < nodes; k++) {
            if (d[k][k] <= 0) continue;                // k no esta en un ciclo positivo
            for (int i = 0; i < nodes; i++) {
                if (d[i][k] == NONE) continue;
                for (int j = 0; j < nodes; j++) {
                    if (d[k][j] == NONE) continue;
                    unbounded[i][j] = true;
                }
            }
        }
        return unbounded;
    }
}
