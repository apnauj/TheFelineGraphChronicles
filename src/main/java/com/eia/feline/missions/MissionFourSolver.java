package com.eia.feline.missions;

import com.eia.feline.algo.graph.EdgeList;
import com.eia.feline.algo.mst.Kruskal;
import com.eia.feline.algo.mst.MstResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Mision 4 -- reconectar la red (Kruskal con union-find).
 *
 * El algoritmo vive en su lugar definitivo, com.eia.feline.algo.mst.Kruskal.
 *
 * Formato de entrada:
 *   T
 *   (por cada caso)  N
 *                    C
 *                    a b costo   (C veces; intersecciones numeradas de 1 a N)
 *
 * OJO con la numeracion: en esta mision las intersecciones van de 1 a N, no de 0
 * a N-1 como en las Misiones 2 y 3. Se pasan a base 0 al leer, en un solo sitio.
 */
public final class MissionFourSolver implements MissionSolver<MissionFourSolver.Case> {

    private static final int MAX_NODES = 10_000;
    private static final int MAX_CABLES = 100_000;
    private static final int MAX_COST = 1_000_000;

    /**
     * @param cables   todos los cables disponibles, en el orden en que se leyeron
     * @param order    indices de los cables en el orden en que Kruskal los examina
     * @param accepted accepted[i] indica si el cable order[i] entro en el arbol
     */
    public record Case(int nodes,
                       EdgeList cables,
                       int[] order,
                       boolean[] accepted,
                       long total,
                       boolean connected,
                       int components) {}

    @Override
    public String title() { return "Mision 4 - Reconectando la red"; }

    @Override
    public String sampleInput() {
        return """
               1
               4
               5
               1 2 10
               2 3 20
               3 4 30
               4 1 40
               1 3 15
               """;
    }

    @Override
    public List<CaseResult<Case>> solve(String raw) throws InputFormatException {
        Tokenizer in = new Tokenizer(raw);
        List<CaseResult<Case>> results = new ArrayList<>();

        int cases = in.nextInt("T (numero de casos de prueba)", 0, Integer.MAX_VALUE);

        for (int k = 1; k <= cases; k++) {
            int nodes = in.nextInt("N (numero de intersecciones) del caso " + k, 1, MAX_NODES);
            int cableCount = in.nextInt("C (numero de cables) del caso " + k, 0, MAX_CABLES);

            EdgeList cables = new EdgeList(cableCount);
            for (int i = 0; i < cableCount; i++) {
                // Unica conversion de 1..N a 0..N-1 de toda la mision.
                int a = in.nextInt("la interseccion inicial de un cable del caso " + k, 1, nodes) - 1;
                int b = in.nextInt("la interseccion final de un cable del caso " + k, 1, nodes) - 1;
                int cost = in.nextInt("el costo de un cable del caso " + k, 0, MAX_COST);
                // Los cables duplicados y los que van de una interseccion a si misma
                // se guardan tal cual: Kruskal los descarta solo al cerrar ciclo.
                cables.add(a, b, cost);
            }

            MstResult mst = Kruskal.run(nodes, cables);

            String line = mst.connected()
                    ? "Case #" + k + ": " + mst.total()
                    : "Case #" + k + ": Limon cut too many cables";

            results.add(new CaseResult<>(k, line, new Case(
                    nodes, cables, mst.order(), mst.accepted(),
                    mst.total(), mst.connected(), mst.components())));
        }
        return results;
    }

    public static void main(String[] args) throws IOException {
        String text;
        try (InputStream in = System.in) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try {
            System.out.print(MissionSolver.render(new MissionFourSolver().solve(text)));
        } catch (InputFormatException e) {
            System.err.println("Entrada invalida: " + e.getMessage());
        }
    }
}
