package com.eia.feline.algo.maxwalk;

import com.eia.feline.algo.graph.EdgeList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Bellman-Ford de MAXIMIZACION para la Mision 3: el mayor churun acumulable en un
 * PASEO (puede repetir nodos y aristas) desde un origen fijo hasta cada nodo.
 *
 * POR QUE BELLMAN-FORD EN LA MISION 3: los pasadizos pueden tener peso negativo
 * (estan envenenados), lo que descarta a Dijkstra; y el enunciado exige detectar
 * ciclos de ganancia positiva alcanzables desde el origen, que solo un algoritmo
 * que relaja mas alla de las N-1 rondas necesarias puede hacer.
 *
 * Relaja las M aristas N-1 veces MAXIMIZANDO (en vez de minimizando, que es lo
 * usual). Una ronda extra detecta que nodos SIGUEN mejorando: esos pertenecen a,
 * o son alimentados por, un ciclo de ganancia positiva. Esa marca "unbounded" se
 * propaga desde ahi a todo lo alcanzable, porque un ciclo positivo solo le sirve
 * a las heroinas si pueden entrar en el y despues seguir camino hacia el destino;
 * uno que no puede llegar a D no cambia la respuesta.
 *
 * "Sin ruta" es el centinela NONE (Long.MIN_VALUE), sobre el que nunca se hace
 * aritmetica: toda relajacion comprueba antes que el origen de la arista ya tenga
 * distancia finita. Los valores se acotan con CAP para que un ciclo positivo no
 * empuje al long mas alla de un rango razonable durante las rondas de relajacion;
 * cualquier valor legitimo con N <= 100 y |W| <= 1000 es a lo sumo 99 * 1000 =
 * 99.000, muy por debajo de la cota, asi que recortar nunca se confunde con una
 * respuesta real.
 *
 * Complejidad: O(N * M) tiempo (las N-1 rondas de relajacion, mas la propagacion
 * final que recorre cada arista a lo sumo una vez), O(N + M) espacio.
 */
public final class BellmanFord {

    /** Centinela de "no hay ruta". Nunca entra en una suma. */
    public static final long NONE = Long.MIN_VALUE;

    /** Techo artificial: por encima de esto el valor solo puede venir de un ciclo positivo. */
    private static final long CAP = 1_000_000_000L;

    private BellmanFord() {}

    public static MaxWalkResult run(int nodes, EdgeList edges, int source) {
        long[] dist = new long[nodes];
        Arrays.fill(dist, NONE);
        int[] parent = new int[nodes];
        Arrays.fill(parent, -1);
        dist[source] = 0;

        for (int round = 0; round < nodes - 1; round++) {
            boolean changed = false;
            for (int e = 0; e < edges.size(); e++) {
                int a = edges.from(e), b = edges.to(e);
                if (dist[a] == NONE) continue;
                long candidate = Math.min(CAP, dist[a] + edges.weight(e));
                if (dist[b] == NONE || candidate > dist[b]) {
                    dist[b] = candidate;
                    parent[b] = a;
                    changed = true;
                }
            }
            if (!changed) break;    // ya converge: sin ciclo positivo no puede mejorar mas
        }

        // Ronda extra: lo que todavia mejora pertenece a un ciclo positivo o lo alimenta.
        boolean[] seed = new boolean[nodes];
        int witness = -1;
        for (int e = 0; e < edges.size(); e++) {
            int a = edges.from(e), b = edges.to(e);
            if (dist[a] == NONE) continue;
            long candidate = Math.min(CAP, dist[a] + edges.weight(e));
            if (dist[b] == NONE || candidate > dist[b]) {
                seed[b] = true;
                parent[b] = a;
                if (witness < 0) witness = b;
            }
        }

        boolean[] unbounded = propagate(nodes, edges, seed);

        return new MaxWalkResult(dist, parent, unbounded, extractCycle(nodes, parent, witness));
    }

    /** Propaga "unbounded" desde los nodos semilla a todo lo que alcanzan. */
    private static boolean[] propagate(int nodes, EdgeList edges, boolean[] seed) {
        boolean[] unbounded = new boolean[nodes];
        Deque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < nodes; v++) {
            if (seed[v]) { unbounded[v] = true; queue.add(v); }
        }

        List<List<Integer>> out = new ArrayList<>(nodes);
        for (int i = 0; i < nodes; i++) out.add(new ArrayList<>());
        for (int e = 0; e < edges.size(); e++) out.get(edges.from(e)).add(edges.to(e));

        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int w : out.get(v)) {
                if (!unbounded[w]) { unbounded[w] = true; queue.add(w); }
            }
        }
        return unbounded;
    }

    /**
     * Sube N veces por parent[] desde un nodo que seguia mejorando: eso garantiza
     * caer DENTRO del ciclo, y desde ahi se recoge hasta volver a pasar por el
     * mismo nodo.
     */
    private static int[] extractCycle(int nodes, int[] parent, int witness) {
        if (witness < 0) return new int[0];

        int x = witness;
        for (int i = 0; i < nodes; i++) {
            if (parent[x] == -1) return new int[0];
            x = parent[x];
        }

        List<Integer> cycle = new ArrayList<>();
        boolean[] seen = new boolean[nodes];
        int at = x;
        while (!seen[at]) {
            seen[at] = true;
            cycle.add(at);
            if (parent[at] == -1) return new int[0];
            at = parent[at];
        }

        // Recortar lo que va por delante del ciclo y devolverlo en sentido del paseo.
        int start = cycle.indexOf(at);
        if (start < 0) return new int[0];
        List<Integer> ring = cycle.subList(start, cycle.size());
        int[] result = new int[ring.size()];
        for (int i = 0; i < ring.size(); i++) result[i] = ring.get(ring.size() - 1 - i);
        return result;
    }
}
