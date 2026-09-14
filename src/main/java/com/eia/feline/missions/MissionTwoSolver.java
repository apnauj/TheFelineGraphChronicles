package com.eia.feline.missions;

import com.eia.feline.algo.graph.EdgeList;
import com.eia.feline.algo.graph.WeightedGraph;
import com.eia.feline.algo.sp.Dijkstra;
import com.eia.feline.algo.sp.ShortestPathResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Mision 2 -- recuperar las cuentas de Claude (Dijkstra).
 *
 * Formato de entrada:
 *   T
 *   (por cada caso)  N C S D
 *                    A B W   (C veces; conexion bidireccional, W >= 0)
 */
public final class MissionTwoSolver implements MissionSolver<MissionTwoSolver.Case> {

    private static final int MAX_NODES = 10_000;
    private static final int MAX_EDGES = 100_000;
    private static final int MAX_WEIGHT = 1_000_000;

    /** Estado estructurado para dibujar la red y resaltar la ruta mas barata. */
    public record Case(int nodes,
                       EdgeList edges,
                       int start,
                       int destination,
                       ShortestPathResult shortestPath,
                       boolean reachable) {

        /** Costo total de la ruta, valido solo si reachable() es true. */
        public long cost() { return shortestPath.costTo(destination); }

        /** Nodos de la ruta mas barata, en orden; vacio si no hay ruta. */
        public int[] route() { return shortestPath.pathTo(destination); }
    }

    @Override
    public String title() { return "Mision 2 - Recuperando las cuentas de Claude"; }

    @Override
    public String sampleInput() {
        return """
               3
               2 1 0 1
               0 1 100
               3 3 2 0
               0 1 100
               0 2 200
               1 2 50
               2 0 0 1
               """;
    }

    @Override
    public List<CaseResult<Case>> solve(String raw) throws InputFormatException {
        Tokenizer in = new Tokenizer(raw);
        List<CaseResult<Case>> results = new ArrayList<>();

        int cases = in.nextInt("T (numero de casos de prueba)", 0, Integer.MAX_VALUE);

        for (int k = 1; k <= cases; k++) {
            int nodes = in.nextInt("N (numero de nodos) del caso " + k, 1, MAX_NODES);
            int edgeCount = in.nextInt("C (numero de conexiones) del caso " + k, 0, MAX_EDGES);
            int start = in.nextInt("S (nodo de inicio) del caso " + k, 0, nodes - 1);
            int destination = in.nextInt("D (nodo destino) del caso " + k, 0, nodes - 1);

            EdgeList edges = new EdgeList(edgeCount);
            for (int i = 0; i < edgeCount; i++) {
                int a = in.nextInt("el extremo A de una conexion del caso " + k, 0, nodes - 1);
                int b = in.nextInt("el extremo B de una conexion del caso " + k, 0, nodes - 1);
                int w = in.nextInt("el peso W de una conexion del caso " + k, 0, MAX_WEIGHT);
                edges.add(a, b, w);
            }

            // Las conexiones son bidireccionales: cada una se guarda en los dos sentidos.
            // Los duplicados y los lazos se guardan tal cual; Dijkstra se queda solo con
            // el mejor sin necesidad de deduplicar.
            WeightedGraph graph = edges.toUndirectedGraph(nodes);
            ShortestPathResult sp = Dijkstra.run(graph, start);
            boolean reachable = sp.reached(destination);

            String line = reachable
                    ? "Case #" + k + ": " + sp.costTo(destination)
                    : "Case #" + k + ": Nina is very sad";

            results.add(new CaseResult<>(k,
                    line,
                    new Case(nodes, edges, start, destination, sp, reachable)));
        }
        return results;
    }

    public static void main(String[] args) throws IOException {
        String text;
        try (InputStream in = System.in) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try {
            System.out.print(MissionSolver.render(new MissionTwoSolver().solve(text)));
        } catch (InputFormatException e) {
            System.err.println("Entrada invalida: " + e.getMessage());
        }
    }
}
