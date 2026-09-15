package com.eia.feline.missions;

import com.eia.feline.algo.mst.Kruskal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MissionFourSolverTest {

    private final MissionFourSolver solver = new MissionFourSolver();

    @Test
    @DisplayName("El ejemplo del enunciado da 55")
    void statementSample() throws Exception {
        assertEquals("Case #1: 55\n", MissionSolver.render(solver.solve(solver.sampleInput())));
    }

    @Test
    @DisplayName("Se escogen 1-2, 1-3 y 3-4; 2-3 y 4-1 se descartan por cerrar ciclo")
    void theRightCablesAreChosen() throws Exception {
        MissionFourSolver.Case c = solver.solve(solver.sampleInput()).get(0).payload();

        assertTrue(c.connected());
        assertEquals(55L, c.total());

        long sum = 0;
        int chosen = 0;
        for (int i = 0; i < c.order().length; i++) {
            if (c.accepted()[i]) { sum += c.cables().weight(c.order()[i]); chosen++; }
        }
        assertEquals(c.nodes() - 1, chosen, "un arbol de expansion tiene N-1 aristas");
        assertEquals(55L, sum);
    }

    @Test
    @DisplayName("Kruskal examina los cables de mas barato a mas caro")
    void cablesAreExaminedInIncreasingCost() throws Exception {
        MissionFourSolver.Case c = solver.solve(solver.sampleInput()).get(0).payload();
        long previous = Long.MIN_VALUE;
        for (int i : c.order()) {
            long cost = c.cables().weight(i);
            assertTrue(cost >= previous, "la cola no esta ordenada");
            previous = cost;
        }
    }

    @Test
    @DisplayName("Si los cables no alcanzan para conectar todo, Limon corto demasiados")
    void disconnectedNetworkIsReported() throws Exception {
        String input = """
                       1
                       4
                       1
                       1 2 10
                       """;
        assertEquals("Case #1: Limon cut too many cables\n",
                MissionSolver.render(solver.solve(input)));
    }

    @Test
    @DisplayName("Una sola interseccion ya esta conectada y no cuesta nada")
    void singleIntersectionCostsZero() throws Exception {
        assertEquals("Case #1: 0\n", MissionSolver.render(solver.solve("1\n1\n0\n")));
    }

    @Test
    @DisplayName("Cables duplicados y lazos no rompen nada")
    void duplicatesAndSelfLoopsAreHandled() throws Exception {
        String input = """
                       1
                       3
                       5
                       1 1 7
                       1 2 100
                       1 2 4
                       2 3 6
                       2 3 60
                       """;
        assertEquals("Case #1: 10\n", MissionSolver.render(solver.solve(input)));
    }

    @Test
    @DisplayName("Las intersecciones se numeran de 1 a N; un 0 es entrada invalida")
    void intersectionsAreOneBased() {
        InputFormatException e = assertThrows(InputFormatException.class,
                () -> solver.solve("1\n3\n1\n0 2 5\n"));
        assertTrue(e.getMessage().contains("entre 1 y 3"), e.getMessage());
    }

    @Test
    @DisplayName("El union-find con compresion de caminos aplana el arbol al buscar")
    void pathCompressionFlattensTheTree() {
        Kruskal.UnionFind uf = new Kruskal.UnionFind(6);
        assertTrue(uf.union(0, 1));
        assertTrue(uf.union(1, 2));
        assertTrue(uf.union(2, 3));

        // Ya conectados: unir otra vez debe fallar, y eso es lo que Kruskal usa
        // para saber que un cable cerraria un ciclo.
        assertFalse(uf.union(0, 3));

        int root = uf.find(3);
        assertEquals(root, uf.find(0));
        assertEquals(root, uf.find(1));
        assertEquals(root, uf.find(2));
        assertNotEquals(root, uf.find(4));
    }

    @Test
    @DisplayName("El costo total usa long y no se desborda")
    void totalUsesLong() throws Exception {
        // 5000 cables al maximo: 5000 * 1.000.000 = 5.000.000.000 > Integer.MAX_VALUE
        StringBuilder sb = new StringBuilder("1\n5001\n5000\n");
        for (int i = 1; i <= 5000; i++) sb.append(i).append(' ').append(i + 1).append(" 1000000\n");
        long expected = 5000L * 1_000_000L;
        assertTrue(expected > Integer.MAX_VALUE);
        assertEquals("Case #1: " + expected + "\n", MissionSolver.render(solver.solve(sb.toString())));
    }
}
