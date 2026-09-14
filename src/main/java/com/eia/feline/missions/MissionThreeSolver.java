package com.eia.feline.missions;

import com.eia.feline.algo.graph.EdgeList;
import com.eia.feline.missions.stub.ReferenceMaxWalk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Mision 3 -- el botin de churun (Floyd-Warshall y Bellman-Ford).
 *
 * ATENCION: los dos algoritmos vienen por ahora de
 * com.eia.feline.missions.stub.ReferenceMaxWalk, que es andamiaje temporal para
 * poder construir la interfaz. Cuando lleguen las implementaciones definitivas
 * en com.eia.feline.algo.maxwalk, lo unico que cambia en todo el proyecto son
 * las dos llamadas de solve(). El payload y la pantalla no se tocan.
 *
 * Formato de entrada:
 *   T
 *   (por cada caso)  N M S D
 *                    A B W   (M veces; pasadizo DIRIGIDO de A a B, -1000 <= W <= 1000)
 */
public final class MissionThreeSolver implements MissionSolver<MissionThreeSolver.Case> {

    private static final int MAX_NODES = 100;
    private static final int MAX_EDGES = 5000;
    private static final int MAX_WEIGHT = 1000;

    /** Cual de los tres desenlaces del enunciado aplica, en su orden de precedencia. */
    public enum Outcome { BLOCKED, INFINITE, VALUE }

    /**
     * @param matrix    churun maximo entre todos los pares (Floyd-Warshall)
     * @param unbounded casillas de la matriz cuyo maximo no esta acotado
     * @param route     ruta que alcanza el maximo, cuando es finito
     * @param cycle     ciclo positivo responsable, cuando el resultado es infinito
     * @param mismatch  null si los dos algoritmos coinciden; si no, en que discrepan
     */
    public record Case(int nodes,
                       EdgeList edges,
                       int start,
                       int destination,
                       Outcome outcome,
                       long churun,
                       long[][] matrix,
                       boolean[][] unbounded,
                       int[] route,
                       int[] cycle,
                       String mismatch) {

        public boolean agrees() { return mismatch == null; }
    }

    @Override
    public String title() { return "Mision 3 - El botin de churun"; }

    @Override
    public String sampleInput() {
        return """
               3
               5 7 0 4
               0 1 50
               0 2 10
               1 2 -30
               1 3 40
               2 1 -5
               2 3 60
               3 4 20
               4 4 0 3
               0 1 20
               1 2 30
               2 1 -10
               2 3 15
               3 3 0 2
               0 1 -40
               1 2 -25
               0 2 -80
               """;
    }

    @Override
    public List<CaseResult<Case>> solve(String raw) throws InputFormatException {
        Tokenizer in = new Tokenizer(raw);
        List<CaseResult<Case>> results = new ArrayList<>();

        int cases = in.nextInt("T (numero de casos de prueba)", 0, Integer.MAX_VALUE);

        for (int k = 1; k <= cases; k++) {
            int nodes = in.nextInt("N (numero de nodos) del caso " + k, 1, MAX_NODES);
            int passages = in.nextInt("M (numero de pasadizos) del caso " + k, 0, MAX_EDGES);
            int start = in.nextInt("S (nodo de inicio) del caso " + k, 0, nodes - 1);
            int destination = in.nextInt("D (nodo destino) del caso " + k, 0, nodes - 1);

            EdgeList edges = new EdgeList(passages);
            for (int i = 0; i < passages; i++) {
                int a = in.nextInt("el origen A de un pasadizo del caso " + k, 0, nodes - 1);
                int b = in.nextInt("el destino B de un pasadizo del caso " + k, 0, nodes - 1);
                int w = in.nextInt("el churun W de un pasadizo del caso " + k, -MAX_WEIGHT, MAX_WEIGHT);
                // Los pasadizos son de una sola direccion y los repetidos cuentan aparte.
                edges.add(a, b, w);
            }

            // Los DOS algoritmos se ejecutan en TODOS los casos, como pide el enunciado.
            ReferenceMaxWalk.AllPairs fw = ReferenceMaxWalk.floydWarshall(nodes, edges);
            ReferenceMaxWalk.SingleSource bf = ReferenceMaxWalk.bellmanFord(nodes, edges, start);

            boolean reachable = bf.dist()[destination] != ReferenceMaxWalk.NONE;
            boolean infinite = bf.unbounded()[destination];

            Outcome outcome;
            String line;
            long churun = 0;
            if (!reachable) {
                outcome = Outcome.BLOCKED;
                line = "Case #" + k + ": Limon blocked the way";
            } else if (infinite) {
                outcome = Outcome.INFINITE;
                line = "Case #" + k + ": Infinite churun!";
            } else {
                outcome = Outcome.VALUE;
                churun = bf.dist()[destination];
                line = "Case #" + k + ": " + churun;
            }

            String mismatch = crossCheck(fw, bf, start, destination, outcome, churun);

            int[] route = (outcome == Outcome.VALUE)
                    ? reconstruct(bf.parent(), start, destination, nodes)
                    : new int[0];

            results.add(new CaseResult<>(k, line, new Case(
                    nodes, edges, start, destination, outcome, churun,
                    fw.best(), fw.unbounded(), route, bf.cycle(), mismatch)));
        }
        return results;
    }

    /**
     * Floyd-Warshall y Bellman-Ford tienen que dar lo mismo para (S, D). El
     * enunciado exige que la GUI avise si alguna vez discrepan, asi que la
     * comparacion se hace aqui y el desacuerdo viaja en el payload.
     */
    private String crossCheck(ReferenceMaxWalk.AllPairs fw, ReferenceMaxWalk.SingleSource bf,
                              int s, int d, Outcome outcome, long churun) {
        boolean fwReachable = fw.best()[s][d] != ReferenceMaxWalk.NONE;
        boolean fwInfinite = fw.unbounded()[s][d];

        switch (outcome) {
            case BLOCKED -> {
                if (fwReachable) {
                    return "Bellman-Ford no encuentra ruta de " + s + " a " + d
                            + " pero Floyd-Warshall si (" + fw.best()[s][d] + ")";
                }
            }
            case INFINITE -> {
                if (!fwInfinite) {
                    return "Bellman-Ford marca churun infinito entre " + s + " y " + d
                            + " pero Floyd-Warshall no";
                }
            }
            case VALUE -> {
                if (fwInfinite) {
                    return "Floyd-Warshall marca churun infinito entre " + s + " y " + d
                            + " pero Bellman-Ford da " + churun;
                }
                if (!fwReachable) {
                    return "Floyd-Warshall no encuentra ruta de " + s + " a " + d
                            + " pero Bellman-Ford da " + churun;
                }
                if (fw.best()[s][d] != churun) {
                    return "Floyd-Warshall da " + fw.best()[s][d]
                            + " y Bellman-Ford da " + churun + " para el par (" + s + ", " + d + ")";
                }
            }
        }
        return null;
    }

    /** Sube por parent[] desde el destino. Solo se usa cuando el maximo es finito. */
    private int[] reconstruct(int[] parent, int start, int destination, int nodes) {
        int[] buffer = new int[nodes + 1];
        int length = 0;
        int at = destination;
        while (at != -1 && length <= nodes) {
            buffer[length++] = at;
            if (at == start) break;
            at = parent[at];
        }
        if (length == 0 || buffer[length - 1] != start) return new int[0];

        int[] route = new int[length];
        for (int i = 0; i < length; i++) route[i] = buffer[length - 1 - i];
        return route;
    }

    public static void main(String[] args) throws IOException {
        String text;
        try (InputStream in = System.in) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try {
            System.out.print(MissionSolver.render(new MissionThreeSolver().solve(text)));
        } catch (InputFormatException e) {
            System.err.println("Entrada invalida: " + e.getMessage());
        }
    }
}
