package com.eia.feline.missions.stub;

import com.eia.feline.algo.graph.EdgeList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * ANDAMIAJE TEMPORAL -- ver el README de este paquete.
 *
 * Floyd-Warshall y Bellman-Ford de MAXIMIZACION para la Mision 3, escritos solo
 * para poder construir y demostrar la interfaz. La version definitiva la escribe
 * otro integrante en com.eia.feline.algo.maxwalk y esta clase se borra.
 *
 * Notas del problema que la implementacion definitiva tambien tiene que respetar:
 *
 *  - Las aristas son DIRIGIDAS y una ruta es un PASEO: puede repetir nodos y
 *    aristas. Por eso el problema es tratable; el camino simple mas largo seria
 *    NP-dificil.
 *  - "Sin ruta" es un centinela sobre el que nunca se hace aritmetica.
 *  - Los valores se acotan (CAP). Sin esa cota, con un ciclo positivo el triple
 *    bucle duplica el valor en cada k y desborda el long mucho antes de los 100
 *    nodos que permite el enunciado. Cualquier valor legitimo es a lo sumo
 *    (N-1) * 1000 = 99.000, muy por debajo de la cota, asi que recortar no puede
 *    confundirse con una respuesta real.
 */
public final class ReferenceMaxWalk {

    /** Centinela de "no hay ruta". Nunca entra en una suma. */
    public static final long NONE = Long.MIN_VALUE;

    /** Techo artificial: por encima de esto el valor solo puede venir de un ciclo positivo. */
    public static final long CAP = 1_000_000_000L;

    private ReferenceMaxWalk() {}

    // ------------------------------------------------------------ Floyd-Warshall

    /** Matriz de churun maximo entre todos los pares, con las casillas no acotadas marcadas. */
    public record AllPairs(long[][] best, boolean[][] unbounded) {}

    /**
     * Complejidad: O(N^3) tiempo, O(N^2) espacio. Con N <= 100 son 10^6
     * operaciones, de sobra para el limite del enunciado, y a cambio da la matriz
     * completa que la GUI tiene que mostrar.
     */
    public static AllPairs floydWarshall(int n, EdgeList edges) {
        long[][] d = new long[n][n];
        for (long[] row : d) java.util.Arrays.fill(row, NONE);
        for (int i = 0; i < n; i++) d[i][i] = 0;

        // Si un par ordenado aparece varias veces, se queda el mejor.
        for (int e = 0; e < edges.size(); e++) {
            int a = edges.from(e), b = edges.to(e);
            long w = edges.weight(e);
            if (d[a][b] == NONE || w > d[a][b]) d[a][b] = w;
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (d[i][k] == NONE) continue;              // nunca se suma el centinela
                for (int j = 0; j < n; j++) {
                    if (d[k][j] == NONE) continue;
                    long candidate = Math.min(CAP, d[i][k] + d[k][j]);
                    if (d[i][j] == NONE || candidate > d[i][j]) d[i][j] = candidate;
                }
            }
        }

        // Pasada extra obligatoria: (i, j) es no acotado si y solo si existe un k
        // con d[i][k] finito, d[k][k] > 0 y d[k][j] finito. Sin esto la matriz
        // guarda numeros grandes sin sentido en vez de infinitos.
        boolean[][] unbounded = new boolean[n][n];
        for (int k = 0; k < n; k++) {
            if (d[k][k] <= 0) continue;                     // k no esta en un ciclo positivo
            for (int i = 0; i < n; i++) {
                if (d[i][k] == NONE) continue;
                for (int j = 0; j < n; j++) {
                    if (d[k][j] == NONE) continue;
                    unbounded[i][j] = true;
                }
            }
        }
        return new AllPairs(d, unbounded);
    }

    // ------------------------------------------------------------- Bellman-Ford

    /**
     * @param dist      mejor churun desde el origen, o NONE
     * @param parent    de donde se llego a cada nodo; reconstruye la ruta
     * @param unbounded nodos alimentados por un ciclo positivo alcanzable
     * @param cycle     un ciclo positivo responsable, para resaltarlo; puede ir vacio
     */
    public record SingleSource(long[] dist, int[] parent, boolean[] unbounded, int[] cycle) {}

    /**
     * Complejidad: O(N * M) tiempo, O(N) espacio. Es el unico de los dos que
     * detecta ciclos de ganancia positiva, que es la razon de que la mision pida
     * los dos algoritmos y no solo Floyd-Warshall.
     */
    public static SingleSource bellmanFord(int n, EdgeList edges, int source) {
        long[] dist = new long[n];
        java.util.Arrays.fill(dist, NONE);
        int[] parent = new int[n];
        java.util.Arrays.fill(parent, -1);
        dist[source] = 0;

        // Relajar las M aristas N-1 veces, maximizando.
        for (int round = 0; round < n - 1; round++) {
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
        boolean[] seed = new boolean[n];
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

        // La marca de "no acotado" se propaga a todo lo alcanzable desde esos nodos.
        // D es no acotado SOLO si queda marcado: un ciclo positivo que no puede
        // llegar a D no le sirve de nada a las heroinas. Floyd-Warshall aplica el
        // mismo criterio, y por eso las dos respuestas tienen que coincidir.
        boolean[] unbounded = new boolean[n];
        Deque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (seed[v]) { unbounded[v] = true; queue.add(v); }
        }
        List<List<Integer>> out = adjacency(n, edges);
        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int w : out.get(v)) {
                if (!unbounded[w]) { unbounded[w] = true; queue.add(w); }
            }
        }

        return new SingleSource(dist, parent, unbounded, extractCycle(n, parent, witness));
    }

    private static List<List<Integer>> adjacency(int n, EdgeList edges) {
        List<List<Integer>> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) out.add(new ArrayList<>());
        for (int e = 0; e < edges.size(); e++) out.get(edges.from(e)).add(edges.to(e));
        return out;
    }

    /**
     * Sube N veces por parent[] desde un nodo que seguia mejorando: eso garantiza
     * caer DENTRO del ciclo, y desde ahi se recoge hasta volver a pasar por el
     * mismo nodo.
     */
    private static int[] extractCycle(int n, int[] parent, int witness) {
        if (witness < 0) return new int[0];

        int x = witness;
        for (int i = 0; i < n; i++) {
            if (parent[x] == -1) return new int[0];
            x = parent[x];
        }

        List<Integer> cycle = new ArrayList<>();
        boolean[] seen = new boolean[n];
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
