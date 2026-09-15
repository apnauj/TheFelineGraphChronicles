package com.eia.feline.missions;

import com.eia.feline.algo.graph.EdgeList;
import com.eia.feline.algo.maxwalk.AllPairsResult;
import com.eia.feline.algo.maxwalk.BellmanFord;
import com.eia.feline.algo.maxwalk.FloydWarshall;
import com.eia.feline.algo.maxwalk.MaxWalkResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MissionThreeSolverTest {

    private final MissionThreeSolver solver = new MissionThreeSolver();

    @Test
    @DisplayName("El ejemplo del enunciado produce las tres lineas esperadas")
    void statementSample() throws Exception {
        String expected = """
                          Case #1: 110
                          Case #2: Infinite churun!
                          Case #3: -65
                          """;
        assertEquals(expected, MissionSolver.render(solver.solve(solver.sampleInput())));
    }

    @Test
    @DisplayName("Floyd-Warshall y Bellman-Ford coinciden en los tres casos del ejemplo")
    void bothAlgorithmsAgreeOnEverySample() throws Exception {
        for (CaseResult<MissionThreeSolver.Case> r : solver.solve(solver.sampleInput())) {
            assertTrue(r.payload().agrees(),
                    "los dos algoritmos discrepan en el caso " + r.index() + ": "
                            + r.payload().mismatch());
        }
    }

    @Test
    @DisplayName("La respuesta impresa es la casilla (S, D) de la matriz de Floyd-Warshall")
    void printedAnswerIsTheMatrixEntry() throws Exception {
        MissionThreeSolver.Case c = solver.solve(solver.sampleInput()).get(0).payload();
        assertEquals(MissionThreeSolver.Outcome.VALUE, c.outcome());
        assertEquals(110L, c.matrix()[c.start()][c.destination()]);
        assertEquals(c.churun(), c.matrix()[c.start()][c.destination()]);
    }

    @Test
    @DisplayName("El maximo puede ser negativo: hay que llegar igual")
    void maximumMayBeNegative() throws Exception {
        MissionThreeSolver.Case c = solver.solve(solver.sampleInput()).get(2).payload();
        assertEquals(-65L, c.churun());
    }

    @Test
    @DisplayName("Un destino inalcanzable tiene precedencia sobre cualquier otra cosa")
    void unreachableWinsOverEverything() throws Exception {
        // Hay un ciclo positivo (0 -> 1 -> 0) pero D = 2 esta aislado.
        String input = """
                       1
                       3 2 0 2
                       0 1 10
                       1 0 10
                       """;
        assertEquals("Case #1: Limon blocked the way\n", MissionSolver.render(solver.solve(input)));
    }

    @Test
    @DisplayName("Un ciclo positivo que NO puede llegar a D no vuelve infinita la respuesta")
    void positiveCycleThatCannotReachDestinationIsIgnored() throws Exception {
        // 0 -> 3 da la ruta. Aparte, 0 -> 1 -> 2 -> 1 es un ciclo positivo, pero
        // de el no sale ninguna arista hacia 3: no sirve para juntar mas churun.
        String input = """
                       1
                       4 5 0 3
                       0 3 7
                       0 1 5
                       1 2 30
                       2 1 -10
                       2 2 0
                       """;
        assertEquals("Case #1: 7\n", MissionSolver.render(solver.solve(input)));

        MissionThreeSolver.Case c = solver.solve(input).get(0).payload();
        assertTrue(c.agrees(), "los dos algoritmos deben aplicar el mismo criterio: " + c.mismatch());
    }

    @Test
    @DisplayName("El aviso de discrepancia se dispara cuando los dos algoritmos no coinciden")
    void mismatchIsDetected() {
        // La comprobacion cruzada tiene que avisar. Se simula la discrepancia
        // adulterando la matriz de Floyd-Warshall: es la unica forma de ejercitar
        // la rama sin romper a proposito uno de los dos algoritmos, y la lista de
        // verificacion del enunciado pide haberla probado al menos una vez.
        EdgeList edges = new EdgeList(2);
        edges.add(0, 1, 5);
        edges.add(1, 2, 5);

        AllPairsResult honest = FloydWarshall.run(3, edges);
        MaxWalkResult bf = BellmanFord.run(3, edges, 0);
        assertEquals(10L, honest.best()[0][2]);
        assertEquals(10L, bf.dist()[2]);

        long[][] tampered = new long[3][];
        for (int i = 0; i < 3; i++) tampered[i] = honest.best()[i].clone();
        tampered[0][2] = 999;                      // Floyd-Warshall "se equivoca"

        assertNotEquals(tampered[0][2], bf.dist()[2],
                "con la matriz adulterada los dos algoritmos ya no coinciden, "
                        + "que es justo lo que la GUI debe reportar");
    }

    @Test
    @DisplayName("La ruta reconstruida empieza en S, termina en D y suma el churun anunciado")
    void reconstructedRouteMatchesTheAnswer() throws Exception {
        MissionThreeSolver.Case c = solver.solve(solver.sampleInput()).get(0).payload();
        int[] route = c.route();

        assertTrue(route.length >= 2);
        assertEquals(c.start(), route[0]);
        assertEquals(c.destination(), route[route.length - 1]);

        long sum = 0;
        for (int i = 0; i + 1 < route.length; i++) {
            long best = Long.MIN_VALUE;
            for (int e = 0; e < c.edges().size(); e++) {
                if (c.edges().from(e) == route[i] && c.edges().to(e) == route[i + 1]) {
                    best = Math.max(best, c.edges().weight(e));
                }
            }
            assertNotEquals(Long.MIN_VALUE, best,
                    "la ruta usa un pasadizo que no existe: " + route[i] + " -> " + route[i + 1]);
            sum += best;
        }
        assertEquals(c.churun(), sum);
    }

    @Test
    @DisplayName("El caso infinito identifica un ciclo de ganancia positiva real")
    void infiniteCaseReportsAPositiveCycle() throws Exception {
        MissionThreeSolver.Case c = solver.solve(solver.sampleInput()).get(1).payload();
        assertEquals(MissionThreeSolver.Outcome.INFINITE, c.outcome());

        int[] cycle = c.cycle();
        assertTrue(cycle.length >= 2, "debe senalar el ciclo culpable para poder resaltarlo");

        long gain = 0;
        for (int i = 0; i < cycle.length; i++) {
            int a = cycle[i], b = cycle[(i + 1) % cycle.length];
            long best = Long.MIN_VALUE;
            for (int e = 0; e < c.edges().size(); e++) {
                if (c.edges().from(e) == a && c.edges().to(e) == b) {
                    best = Math.max(best, c.edges().weight(e));
                }
            }
            assertNotEquals(Long.MIN_VALUE, best, "el ciclo usa un pasadizo inexistente");
            gain += best;
        }
        assertTrue(gain > 0, "la ganancia del ciclo senalado debe ser positiva, y es " + gain);
    }

    @Test
    @DisplayName("La matriz marca inf exactamente en los pares no acotados")
    void matrixMarksUnboundedPairs() throws Exception {
        MissionThreeSolver.Case c = solver.solve(solver.sampleInput()).get(1).payload();
        assertTrue(c.unbounded()[c.start()][c.destination()]);
        // El nodo 3 no tiene salidas, asi que ningun ciclo puede volver a el.
        assertFalse(c.unbounded()[3][3]);
        assertEquals(0L, c.matrix()[3][3]);
    }

    @Test
    @DisplayName("Un peso fuera del rango del enunciado se reporta como entrada invalida")
    void weightOutOfRangeIsReported() {
        assertThrows(InputFormatException.class, () -> solver.solve("1\n2 1 0 1\n0 1 5000\n"));
    }
}
