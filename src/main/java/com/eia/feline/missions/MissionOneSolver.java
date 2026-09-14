package com.eia.feline.missions;

import com.eia.feline.algo.grid.GridGraph;
import com.eia.feline.algo.search.BFS;
import com.eia.feline.algo.search.DFS;
import com.eia.feline.algo.search.SearchResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Mision 1 -- rescatar a Nina del campo minado (BFS y DFS).
 *
 * Formato de entrada (flujo de tokens):
 *   R C
 *   filasConBombas
 *   (por cada una)  fila  cuantasBombas  columna...
 *   filaInicio colInicio
 *   filaDestino colDestino
 *   ... y al final un caso con R = 0 y C = 0 que NO se procesa.
 */
public final class MissionOneSolver implements MissionSolver<MissionOneSolver.Case> {

    /** Todo lo que la visualizacion necesita para dibujar y animar un caso. */
    public record Case(GridGraph grid,
                       int start,
                       int end,
                       SearchResult bfs,
                       SearchResult dfs,
                       boolean reachable) {

        public int bfsMoves() { return bfs.distanceTo(end); }
        public int dfsMoves() { return dfs.distanceTo(end); }
    }

    @Override
    public String title() { return "Mision 1 - Rescatando a Nina del campo minado"; }

    @Override
    public String sampleInput() {
        return """
               10 10
               9
               0 1 2
               1 1 2
               2 2 2 9
               3 2 1 7
               5 3 3 6 9
               6 4 0 1 2 7
               7 3 0 3 8
               8 2 7 9
               9 3 2 3 4
               0 0
               9 9
               0 0
               """;
    }

    @Override
    public List<CaseResult<Case>> solve(String raw) throws InputFormatException {
        Tokenizer in = new Tokenizer(raw);
        List<CaseResult<Case>> results = new ArrayList<>();
        int k = 1;

        while (true) {
            int rows = in.nextInt("R (numero de filas)", 0, 1000);
            int cols = in.nextInt("C (numero de columnas)", 0, 1000);

            // El caso terminal 0 0 no se procesa.
            if (rows == 0 && cols == 0) break;
            if (rows == 0 || cols == 0) {
                throw new InputFormatException(
                        "Caso #" + k + ": R y C deben ser ambos mayores que cero, o ambos cero"
                                + " para terminar la entrada (se leyo R=" + rows + ", C=" + cols + ")");
            }

            boolean[] bomb = new boolean[rows * cols];
            int rowsWithBombs = in.nextInt("el numero de filas con bombas", 0, rows);
            for (int i = 0; i < rowsWithBombs; i++) {
                int row = in.nextInt("el numero de fila de una fila con bombas", 0, rows - 1);
                int count = in.nextInt("la cantidad de bombas de la fila " + row, 0, cols);
                for (int j = 0; j < count; j++) {
                    int col = in.nextInt("la columna de una bomba de la fila " + row, 0, cols - 1);
                    // Unica conversion (fila, columna) -> indice lineal de toda la mision.
                    bomb[row * cols + col] = true;
                }
            }

            int startRow = in.nextInt("la fila de inicio", 0, rows - 1);
            int startCol = in.nextInt("la columna de inicio", 0, cols - 1);
            int endRow = in.nextInt("la fila de destino", 0, rows - 1);
            int endCol = in.nextInt("la columna de destino", 0, cols - 1);

            int start = startRow * cols + startCol;
            int end = endRow * cols + endCol;

            GridGraph grid = GridGraph.of(rows, cols, bomb);
            SearchResult bfs = BFS.search(grid, start, end);
            SearchResult dfs = DFS.search(grid, start, end);

            // Una celda con bomba aisla al nodo, asi que BFS ya devuelve -1 en casi
            // todos los casos. La excepcion es start == end sobre una bomba, donde la
            // distancia seria 0: el enunciado dice que eso cuenta como inalcanzable.
            boolean reachable = !bomb[start] && !bomb[end] && bfs.reached(end);

            String line = reachable
                    ? "Case #" + k + ": BFS " + bfs.distanceTo(end) + " DFS " + dfs.distanceTo(end)
                    : "Case #" + k + ": Nina is unreachable";

            results.add(new CaseResult<>(k, line, new Case(grid, start, end, bfs, dfs, reachable)));
            k++;
        }
        return results;
    }

    /** Ejecucion por consola, util para comparar contra el ejemplo sin abrir la GUI. */
    public static void main(String[] args) throws IOException {
        String text;
        try (InputStream in = System.in) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try {
            System.out.print(MissionSolver.render(new MissionOneSolver().solve(text)));
        } catch (InputFormatException e) {
            System.err.println("Entrada invalida: " + e.getMessage());
        }
    }
}
