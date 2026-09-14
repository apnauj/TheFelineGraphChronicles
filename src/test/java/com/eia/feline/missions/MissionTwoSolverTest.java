package com.eia.feline.missions;

import com.eia.feline.algo.graph.WeightedGraph;
import com.eia.feline.algo.sp.Dijkstra;
import com.eia.feline.algo.sp.ShortestPathResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MissionTwoSolverTest {

    private final MissionTwoSolver solver = new MissionTwoSolver();

    @Test
    @DisplayName("El ejemplo del enunciado produce las tres lineas esperadas")
    void statementSample() throws Exception {
        String expected = """
                          Case #1: 100
                          Case #2: 150
                          Case #3: Nina is very sad
                          """;
        assertEquals(expected, MissionSolver.render(solver.solve(solver.sampleInput())));
    }

    @Test
    @DisplayName("Los costos acumulados usan long y no se desbordan como lo harian en int")
    void accumulatedCostDoesNotOverflow() throws Exception {
        // Primero un caso chico que solo comprueba que la suma es correcta.
        String input = """
                       1
                       4 3 0 3
                       0 1 1000000
                       1 2 1000000
                       2 3 1000000
                       """;
        assertEquals("Case #1: 3000000\n", MissionSolver.render(solver.solve(input)));

        // Y el caso real del limite del enunciado: una cadena larga de pesos maximos.
        StringBuilder sb = new StringBuilder("1\n5000 4999 0 4999\n");
        for (int i = 0; i < 4999; i++) sb.append(i).append(' ').append(i + 1).append(" 1000000\n");
        long expected = 4999L * 1_000_000L;          // 4.999.000.000 > Integer.MAX_VALUE
        assertTrue(expected > Integer.MAX_VALUE, "el caso debe pasarse de int para ser util");
        assertEquals("Case #1: " + expected + "\n", MissionSolver.render(solver.solve(sb.toString())));
    }

    @Test
    @DisplayName("Origen igual a destino cuesta 0")
    void sameNodeCostsZero() throws Exception {
        assertEquals("Case #1: 0\n", MissionSolver.render(solver.solve("1\n3 0 2 2\n")));
    }

    @Test
    @DisplayName("Aristas repetidas y lazos no rompen nada; gana la mas barata")
    void duplicatesAndSelfLoopsAreHandled() throws Exception {
        String input = """
                       1
                       2 4 0 1
                       0 0 5
                       0 1 900
                       0 1 40
                       1 1 7
                       """;
        assertEquals("Case #1: 40\n", MissionSolver.render(solver.solve(input)));
    }

    @Test
    @DisplayName("Un peso de 0 es valido y no confunde la relajacion")
    void zeroWeightIsAllowed() throws Exception {
        assertEquals("Case #1: 0\n",
                MissionSolver.render(solver.solve("1\n2 1 0 1\n0 1 0\n")));
    }

    @Test
    @DisplayName("La ruta reconstruida empieza en S, termina en D y suma el costo anunciado")
    void reconstructedRouteMatchesTheCost() throws Exception {
        MissionTwoSolver.Case c = solver.solve(solver.sampleInput()).get(1).payload();
        int[] route = c.route();

        assertEquals(c.start(), route[0]);
        assertEquals(c.destination(), route[route.length - 1]);
        assertEquals(150L, c.cost());
        assertArrayEquals(new int[]{ 2, 1, 0 }, route, "la ruta barata es 2 -> 1 -> 0");
    }

    @Test
    @DisplayName("Un grafo sin aristas deja a Nina triste")
    void noEdgesMeansUnreachable() throws Exception {
        assertEquals("Case #1: Nina is very sad\n",
                MissionSolver.render(solver.solve("1\n2 0 0 1\n")));
    }

    @Test
    @DisplayName("Un nodo fuera de rango se reporta como entrada invalida, no como crash")
    void nodeOutOfRangeIsReported() {
        InputFormatException e = assertThrows(InputFormatException.class,
                () -> solver.solve("1\n2 1 0 1\n0 5 10\n"));
        assertTrue(e.getMessage().contains("entre 0 y 1"), e.getMessage());
    }

    @Test
    @DisplayName("Un peso negativo se rechaza en la lectura: Dijkstra no lo admite")
    void negativeWeightIsRejected() {
        assertThrows(InputFormatException.class, () -> solver.solve("1\n2 1 0 1\n0 1 -5\n"));
    }

    @Test
    @DisplayName("El centinela de inalcanzable nunca entra en una suma")
    void unreachableSentinelIsNeverUsedInArithmetic() {
        // Dos componentes separadas: 0-1 y 2-3. Desde 0 no se llega a 2 ni a 3, y sus
        // distancias deben quedar EXACTAMENTE en el centinela, sin desbordar.
        WeightedGraph g = new WeightedGraph(4)
                .addUndirected(0, 1, 10)
                .addUndirected(2, 3, 10);
        ShortestPathResult sp = Dijkstra.run(g, 0);

        assertEquals(Dijkstra.UNREACHABLE, sp.costTo(2));
        assertEquals(Dijkstra.UNREACHABLE, sp.costTo(3));
        assertFalse(sp.reached(2));
        assertEquals(10L, sp.costTo(1));
    }

    @Test
    @DisplayName("Los nodos se resuelven en orden de costo creciente")
    void settleOrderIsByIncreasingCost() {
        WeightedGraph g = new WeightedGraph(4)
                .addUndirected(0, 1, 5)
                .addUndirected(0, 2, 1)
                .addUndirected(2, 3, 1);
        ShortestPathResult sp = Dijkstra.run(g, 0);

        assertArrayEquals(new int[]{ 0, 2, 3, 1 }, sp.settled(),
                "0 (0), luego 2 (1), luego 3 (2), y por ultimo 1 (5)");
    }
}
