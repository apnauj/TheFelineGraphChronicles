package com.eia.feline.algo.sp;

import com.eia.feline.algo.graph.WeightedGraph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Dijkstra con cola de prioridad (min-heap) sobre un grafo en formato CSR.
 *
 * POR QUE DIJKSTRA EN LA MISION 2: los pesos son no negativos, y esa es
 * exactamente la condicion que lo hace correcto. Con pesos no negativos, cuando
 * un nodo sale del heap ya no existe ningun camino mas barato hacia el: cualquier
 * ruta alternativa tendria que pasar por un nodo pendiente, que por definicion ya
 * cuesta al menos lo mismo, y agregarle aristas solo puede aumentar el costo.
 * Con un peso NEGATIVO esa garantia se cae: un nodo puede salir del heap como
 * "resuelto" y despues una arista negativa abaratarlo, y esta implementacion ya
 * no volveria a expandirlo. Para pesos negativos hay que usar Bellman-Ford,
 * que es justamente lo que hace la Mision 3.
 *
 * COSTOS EN long: con N = 10.000 nodos y pesos de hasta 1.000.000, un camino
 * puede costar del orden de 10^10, que no cabe en int. En int el resultado se
 * desbordaria en silencio y saldria un numero equivocado pero verosimil.
 *
 * Complejidad: O((N + M) log N) tiempo con el heap binario de java.util.
 * Espacio: O(N + M).
 */
public final class Dijkstra {

    public static final long UNREACHABLE = ShortestPathResult.UNREACHABLE;

    private Dijkstra() {}

    public static ShortestPathResult run(WeightedGraph g, int start) {
        int n = g.size();

        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, UNREACHABLE);
        Arrays.fill(parent, -1);

        boolean[] settled = new boolean[n];
        int[] order = new int[n];
        int resolved = 0;

        dist[start] = 0;

        // Min-heap ordenado por distancia. Entradas obsoletas se descartan al salir
        // (la variante "lazy"), que evita tener que soportar decrease-key.
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));
        pq.add(new long[]{ start, 0L });

        while (!pq.isEmpty()) {
            long[] current = pq.poll();
            int u = (int) current[0];

            if (settled[u]) continue;      // entrada obsoleta: ya hay mejor camino
            settled[u] = true;
            order[resolved++] = u;

            // RELAJACION de aristas. dist[u] es finito aqui: u salio del heap, y al
            // heap solo entran nodos que ya recibieron un costo real. Por eso la
            // suma de abajo nunca toca el centinela.
            for (int e = g.adjStart(u); e < g.adjEnd(u); e++) {
                int v = g.adjTarget(e);
                long candidate = dist[u] + g.weight(e);
                if (candidate < dist[v]) {
                    dist[v] = candidate;
                    parent[v] = u;
                    pq.add(new long[]{ v, candidate });
                }
            }
        }

        return new ShortestPathResult(dist, parent, order, resolved);
    }
}
