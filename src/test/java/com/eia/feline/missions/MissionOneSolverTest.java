package com.eia.feline.missions;

import com.eia.feline.algo.grid.GridGraph;
import com.eia.feline.algo.search.BFS;
import com.eia.feline.algo.search.DFS;
import com.eia.feline.algo.search.SearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MissionOneSolverTest {

    private final MissionOneSolver solver = new MissionOneSolver();

    @Test
    @DisplayName("El ejemplo del enunciado produce la salida esperada, caracter por caracter")
    void statementSample() throws Exception {
        String output = MissionSolver.render(solver.solve(solver.sampleInput()));
        assertEquals("Case #1: BFS 18 DFS 32\n", output);
    }

    @Test
    @DisplayName("La salida no lleva los signos < > del marcador de posicion del enunciado")
    void outputHasNoAngleBrackets() throws Exception {
        String output = MissionSolver.render(solver.solve(solver.sampleInput()));
        assertFalse(output.contains("<"), "la linea no debe traer '<': " + output);
        assertFalse(output.contains(">"), "la linea no debe traer '>': " + output);
    }

    @Test
    @DisplayName("El DFS expande los vecinos en el orden up, down, left, right")
    void dfsNeighbourOrderIsDeterministic() {
        // Cuadricula 3x3 sin bombas, arrancando en el centro (1,1) = indice 4.
        // Con el orden up, down, left, right el DFS baja primero por 'up' (indice 1),
        // y desde ahi vuelve a preferir 'up'... hasta agotar la rama.
        GridGraph grid = GridGraph.of(3, 3, new boolean[9]);
        SearchResult dfs = DFS.search(grid.adjacency(), 4, 8);

        int[] visited = dfs.visited();
        assertEquals(4, visited[0], "debe empezar en el centro");
        assertEquals(1, visited[1], "el primer vecino expandido debe ser el de arriba");
        assertEquals(0, visited[2], "desde (0,1) el orden up/down/left/right lleva a (0,0)");
    }

    @Test
    @DisplayName("Una bomba en el origen deja a Nina inalcanzable aunque coincida con el destino")
    void bombOnStartIsUnreachableEvenWhenStartEqualsEnd() throws Exception {
        String input = """
                       3 3
                       1
                       1 1 1
                       1 1
                       1 1
                       0 0
                       """;
        assertEquals("Case #1: Nina is unreachable\n",
                MissionSolver.render(solver.solve(input)));
    }

    @Test
    @DisplayName("Origen igual a destino en una celda libre cuesta 0 movimientos")
    void sameCellCostsZero() throws Exception {
        String input = """
                       3 3
                       0
                       1 1
                       1 1
                       0 0
                       """;
        assertEquals("Case #1: BFS 0 DFS 0\n", MissionSolver.render(solver.solve(input)));
    }

    @Test
    @DisplayName("Un muro de bombas hace inalcanzable el destino")
    void wallOfBombsBlocksTheWay() throws Exception {
        String input = """
                       3 3
                       1
                       1 3 0 1 2
                       0 0
                       2 2
                       0 0
                       """;
        assertEquals("Case #1: Nina is unreachable\n", MissionSolver.render(solver.solve(input)));
    }

    @Test
    @DisplayName("Varios casos se numeran de forma corrida dentro de la mision")
    void casesAreNumberedPerMission() throws Exception {
        String input = """
                       2 2
                       0
                       0 0
                       1 1
                       2 2
                       0
                       0 0
                       0 1
                       0 0
                       """;
        List<CaseResult<MissionOneSolver.Case>> results = solver.solve(input);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).index());
        assertEquals(2, results.get(1).index());
        assertTrue(results.get(1).outputLine().startsWith("Case #2: "));
    }

    @Test
    @DisplayName("BFS nunca es peor que DFS y ambos coinciden en la alcanzabilidad")
    void bfsIsOptimalAndAgreesWithDfsOnReachability() throws Exception {
        MissionOneSolver.Case c = solver.solve(solver.sampleInput()).get(0).payload();
        assertTrue(c.bfsMoves() <= c.dfsMoves());
        assertEquals(c.bfs().reached(c.end()), c.dfs().reached(c.end()));
    }

    @Test
    @DisplayName("El camino reconstruido del BFS es contiguo, sin bombas y de la longitud anunciada")
    void reconstructedPathIsValid() throws Exception {
        MissionOneSolver.Case c = solver.solve(solver.sampleInput()).get(0).payload();
        GridGraph grid = c.grid();
        int[] path = c.bfs().pathTo(c.end());

        assertEquals(c.bfsMoves() + 1, path.length, "un camino de b movimientos tiene b+1 celdas");
        assertEquals(c.start(), path[0]);
        assertEquals(c.end(), path[path.length - 1]);

        for (int i = 0; i < path.length; i++) {
            assertFalse(grid.isBomb(path[i]), "el camino pisa una bomba en la celda " + path[i]);
            if (i > 0) {
                int dr = Math.abs(grid.rowOf(path[i]) - grid.rowOf(path[i - 1]));
                int dc = Math.abs(grid.colOf(path[i]) - grid.colOf(path[i - 1]));
                assertEquals(1, dr + dc, "los pasos deben ser de a una celda y sin diagonales");
            }
        }
    }

    @Test
    @DisplayName("La cuadricula del limite (1000x1000) se resuelve sin agotar memoria")
    void maximumGridDoesNotExhaustMemory() throws Exception {
        String input = "1000 1000\n0\n0 0\n999 999\n0 0\n";
        MissionOneSolver.Case c = solver.solve(input).get(0).payload();

        // 999 pasos hacia abajo mas 999 hacia la derecha: la cota inferior es exacta.
        assertEquals(1998, c.bfsMoves());
        // El DFS encuentra un camino valido pero no optimo; solo se le exige eso.
        assertTrue(c.dfsMoves() >= c.bfsMoves(),
                "el DFS no puede ser mas corto que el optimo: " + c.dfsMoves());
    }

    @Test
    @DisplayName("La entrada mal formada produce un mensaje legible, no una excepcion cruda")
    void malformedInputIsReported() {
        InputFormatException e = assertThrows(InputFormatException.class,
                () -> solver.solve("10 10\nchurun\n"));
        assertTrue(e.getMessage().contains("churun"), e.getMessage());
        assertTrue(e.getMessage().contains("token #"), e.getMessage());
    }

    @Test
    @DisplayName("Espacios y lineas en blanco de sobra no afectan la lectura")
    void toleratesExtraWhitespace() throws Exception {
        String messy = "\n\n  3   3 \n\n 0 \n\n  0 0  \n\n 2 2 \n\n\n 0    0 \n\n";
        assertEquals("Case #1: BFS 4 DFS 8\n", MissionSolver.render(solver.solve(messy)));
    }

    @Test
    @DisplayName("El BFS sobre la cuadricula coincide con el BFS sobre la estructura desnuda")
    void bfsMatchesRawGraphTraversal() {
        GridGraph grid = GridGraph.of(4, 4, new boolean[16]);
        assertEquals(6, BFS.bfs(grid.adjacency(), 0, 15));
    }
}
