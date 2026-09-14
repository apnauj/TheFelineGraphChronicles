package com.eia.feline.algo.graph;

/**
 * Grafo con pesos en formato CSR. Internamente todo es dirigido: una arista no
 * dirigida simplemente se guarda dos veces.
 *
 * Se conservan los duplicados y los lazos. El enunciado de la Mision 2 permite
 * "quedarse con la mas barata o guardarlas todas"; guardarlas todas es lo
 * correcto aqui porque deduplicar exige buscar en la lista de vecinos, y con
 * 100.000 aristas incidentes a un mismo nodo esa busqueda es cuadratica. Dijkstra
 * relaja las aristas repetidas sin inmutarse: la mas barata gana sola.
 *
 * Complejidad de la construccion: O(N + M) tiempo y espacio (conteo + suma prefija).
 */
public final class WeightedGraph implements Adjacency {

    private final int n;
    private final int[] off;
    private final int[] to;
    private final long[] w;

    private WeightedGraph(int n, int[] off, int[] to, long[] w) {
        this.n = n;
        this.off = off;
        this.to = to;
        this.w = w;
    }

    /** Peso del arco almacenado en la posicion e. */
    public long weight(int e) { return w[e]; }

    @Override public int size()           { return n; }
    @Override public int edgeCount()      { return to.length; }
    @Override public int adjStart(int v)  { return off[v]; }
    @Override public int adjEnd(int v)    { return off[v + 1]; }
    @Override public int adjTarget(int e) { return to[e]; }

    /**
     * Acumula arcos y al final los ordena por nodo origen con un conteo, sin
     * comparaciones: O(N + M).
     */
    public static final class Builder {
        private final int n;
        private int[] from;
        private int[] dest;
        private long[] cost;
        private int m;

        public Builder(int nodes, int expectedArcs) {
            this.n = nodes;
            int cap = Math.max(4, expectedArcs);
            from = new int[cap];
            dest = new int[cap];
            cost = new long[cap];
        }

        public Builder addDirected(int a, int b, long weight) {
            if (m == from.length) grow();
            from[m] = a;
            dest[m] = b;
            cost[m] = weight;
            m++;
            return this;
        }

        public Builder addUndirected(int a, int b, long weight) {
            addDirected(a, b, weight);
            addDirected(b, a, weight);
            return this;
        }

        private void grow() {
            int cap = from.length + (from.length >> 1) + 1;
            from = java.util.Arrays.copyOf(from, cap);
            dest = java.util.Arrays.copyOf(dest, cap);
            cost = java.util.Arrays.copyOf(cost, cap);
        }

        public WeightedGraph build() {
            int[] off = new int[n + 1];
            for (int i = 0; i < m; i++) off[from[i] + 1]++;
            for (int v = 0; v < n; v++) off[v + 1] += off[v];

            int[] to = new int[m];
            long[] w = new long[m];
            int[] cursor = java.util.Arrays.copyOf(off, n);   // posicion libre de cada nodo
            for (int i = 0; i < m; i++) {
                int slot = cursor[from[i]]++;
                to[slot] = dest[i];
                w[slot] = cost[i];
            }
            return new WeightedGraph(n, off, to, w);
        }
    }
}
