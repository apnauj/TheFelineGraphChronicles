package com.eia.feline.algo;

import com.eia.feline.algo.grid.GridGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La lista de adyacencia de la cuadricula tiene que cumplir tres cosas, y de la
 * tercera depende que el DFS sea determinista y por tanto calificable.
 */
class GridGraphTest {

    @Test
    @DisplayName("El bloque de vecinos se guarda como right, left, down, up")
    void neighbourOrderIsTheReverseOfTheVisitOrder() {
        // Centro de una 3x3 limpia: tiene los cuatro vecinos.
        GridGraph g = GridGraph.of(3, 3, new boolean[9]);
        assertEquals(List.of(5, 3, 7, 1), g.adjacency().get(4),
                "guardar right,left,down,up es lo que hace que la pila LIFO del DFS "
                        + "salga en orden up,down,left,right");
    }

    @Test
    @DisplayName("En los bordes solo aparecen los vecinos que existen, en el mismo orden relativo")
    void bordersKeepTheRelativeOrder() {
        GridGraph g = GridGraph.of(3, 3, new boolean[9]);
        assertEquals(List.of(1, 3), g.adjacency().get(0), "esquina superior izquierda: right, down");
        // La celda 1 es (0,1): right = 2, left = 0, down = 1 + cols = 4.
        assertEquals(List.of(2, 0, 4), g.adjacency().get(1), "borde superior: right, left, down");
        assertEquals(List.of(7, 5), g.adjacency().get(8), "esquina inferior derecha: left, up");
    }

    @Test
    @DisplayName("Una bomba no tiene salidas y nadie tiene una arista hacia ella")
    void bombsAreIsolated() {
        boolean[] bomb = new boolean[9];
        bomb[4] = true;                                  // el centro
        GridGraph g = GridGraph.of(3, 3, bomb);

        assertTrue(g.adjacency().get(4).isEmpty(), "una bomba no tiene salidas");
        for (int v = 0; v < 9; v++) {
            assertFalse(g.adjacency().get(v).contains(4), "nadie debe apuntar a la bomba");
        }
        assertTrue(g.isBomb(4));
        assertTrue(g.isBomb(1, 1));
    }

    @Test
    @DisplayName("Sobre cuadriculas al azar la adyacencia es simetrica y nunca toca una bomba")
    void randomGridsStaySymmetricAndBombFree() {
        Random rnd = new Random(20260913L);
        for (int trial = 0; trial < 40; trial++) {
            int R = 1 + rnd.nextInt(9);
            int C = 1 + rnd.nextInt(9);
            boolean[] bomb = new boolean[R * C];
            for (int i = 0; i < bomb.length; i++) bomb[i] = rnd.nextInt(4) == 0;

            GridGraph g = GridGraph.of(R, C, bomb);
            List<List<Integer>> adj = g.adjacency();

            for (int v = 0; v < R * C; v++) {
                if (bomb[v]) {
                    assertTrue(adj.get(v).isEmpty(), "la bomba " + v + " no debe tener salidas");
                    continue;
                }
                for (int nb : adj.get(v)) {
                    assertFalse(bomb[nb], "hay una arista hacia la bomba " + nb);
                    assertTrue(adj.get(nb).contains(v),
                            "el paso es reversible: falta " + nb + " -> " + v);
                    // Vecindad real: exactamente un paso, sin diagonales.
                    int dr = Math.abs(g.rowOf(v) - g.rowOf(nb));
                    int dc = Math.abs(g.colOf(v) - g.colOf(nb));
                    assertEquals(1, dr + dc, "los vecinos estan a un paso y sin diagonales");
                }
                assertEquals(adj.get(v).size(), new ArrayList<>(new java.util.HashSet<>(adj.get(v))).size(),
                        "no debe haber vecinos repetidos");
            }
        }
    }

    @Test
    @DisplayName("Una cuadricula de una sola celda es valida y no tiene vecinos")
    void singleCellGrid() {
        GridGraph g = GridGraph.of(1, 1, new boolean[1]);
        assertEquals(1, g.size());
        assertTrue(g.adjacency().get(0).isEmpty());
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
