package com.eia.feline.algo;

import com.eia.feline.algo.grid.GridGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La estructura CSR reemplazo a una List<List<Integer>> por razones de memoria.
 * Estas pruebas fijan que el reemplazo sea equivalente: mismos vecinos y, sobre
 * todo, EN EL MISMO ORDEN, porque de ese orden depende que el DFS sea determinista.
 */
class GridGraphTest {

    /** La construccion original, conservada aqui como referencia contra la cual comparar. */
    private static List<List<Integer>> referenceAdjacency(int R, int C, boolean[] bomb) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < R * C; i++) adj.add(new ArrayList<>());

        for (int i = 0; i < R * C; i++) {
            if (bomb[i]) continue;
            int r = i / C, c = i % C;
            if (c < C - 1 && !bomb[i + 1]) adj.get(i).add(i + 1); // right
            if (c > 0     && !bomb[i - 1]) adj.get(i).add(i - 1); // left
            if (r < R - 1 && !bomb[i + C]) adj.get(i).add(i + C); // down
            if (r > 0     && !bomb[i - C]) adj.get(i).add(i - C); // up
        }
        return adj;
    }

    private static List<Integer> neighboursOf(GridGraph g, int v) {
        List<Integer> out = new ArrayList<>();
        for (int e = g.adjStart(v); e < g.adjEnd(v); e++) out.add(g.adjTarget(e));
        return out;
    }

    @Test
    @DisplayName("CSR coincide con la lista de listas original, vecino por vecino y en orden")
    void csrMatchesTheOriginalAdjacencyList() {
        Random rnd = new Random(20260913L);
        for (int trial = 0; trial < 40; trial++) {
            int R = 1 + rnd.nextInt(9);
            int C = 1 + rnd.nextInt(9);
            boolean[] bomb = new boolean[R * C];
            for (int i = 0; i < bomb.length; i++) bomb[i] = rnd.nextInt(4) == 0;

            GridGraph g = GridGraph.of(R, C, bomb);
            List<List<Integer>> reference = referenceAdjacency(R, C, bomb);

            for (int v = 0; v < R * C; v++) {
                assertEquals(reference.get(v), neighboursOf(g, v),
                        "difieren los vecinos de la celda " + v + " en una cuadricula " + R + "x" + C);
            }
            int totalArcs = reference.stream().mapToInt(List::size).sum();
            assertEquals(totalArcs, g.edgeCount());
        }
    }

    @Test
    @DisplayName("El bloque de vecinos se guarda como right, left, down, up")
    void neighbourBlockIsStoredInReverseOfTheVisitOrder() {
        // Centro de una 3x3 limpia: tiene los cuatro vecinos.
        GridGraph g = GridGraph.of(3, 3, new boolean[9]);
        assertEquals(List.of(5, 3, 7, 1), neighboursOf(g, 4),
                "guardar right,left,down,up es lo que hace que la pila LIFO del DFS "
                        + "salga en orden up,down,left,right");
    }

    @Test
    @DisplayName("Una bomba no tiene salidas y nadie tiene una arista hacia ella")
    void bombsAreIsolated() {
        boolean[] bomb = new boolean[9];
        bomb[4] = true;                                  // el centro
        GridGraph g = GridGraph.of(3, 3, bomb);

        assertTrue(neighboursOf(g, 4).isEmpty(), "una bomba no tiene salidas");
        for (int v = 0; v < 9; v++) {
            assertFalse(neighboursOf(g, v).contains(4), "nadie debe apuntar a la bomba");
        }
        assertTrue(g.isBomb(4));
        assertTrue(g.isBomb(1, 1));
    }

    @Test
    @DisplayName("Una cuadricula de una sola celda es valida y no tiene vecinos")
    void singleCellGrid() {
        GridGraph g = GridGraph.of(1, 1, new boolean[1]);
        assertEquals(1, g.size());
        assertEquals(0, g.edgeCount());
    }

    @Test
    @DisplayName("Las conversiones entre (fila, columna) e indice lineal son mutuamente inversas")
    void indexRoundTrip() {
        GridGraph g = GridGraph.of(7, 5, new boolean[35]);
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 5; c++) {
                int i = g.index(r, c);
                assertEquals(r, g.rowOf(i));
                assertEquals(c, g.colOf(i));
            }
        }
    }
}
