package com.eia.feline.algo.graph;

/**
 * Lista de aristas en arreglos planos: a[i], b[i], w[i] describen la arista i.
 *
 * Es la representacion que quieren los algoritmos centrados en aristas y no en
 * nodos: Kruskal (Mision 4), que las ordena por peso, y Bellman-Ford (Mision 3),
 * que relaja las M aristas N-1 veces. Para esos dos recorrer un arreglo plano es
 * mas barato que navegar una lista de adyacencia.
 *
 * Se guardan TODAS las aristas, incluidos duplicados y lazos: el enunciado lo
 * permite explicitamente y evita el costo de deduplicar.
 *
 * Los pesos son long porque los acumulados de las Misiones 2, 3 y 4 no caben
 * en int (10^4 nodos x 10^6 de peso = 10^10).
 */
public final class EdgeList {

    private int[] a;
    private int[] b;
    private long[] w;
    private int size;

    public EdgeList(int expectedEdges) {
        int cap = Math.max(4, expectedEdges);
        a = new int[cap];
        b = new int[cap];
        w = new long[cap];
    }

    public void add(int from, int to, long weight) {
        if (size == a.length) grow();
        a[size] = from;
        b[size] = to;
        w[size] = weight;
        size++;
    }

    private void grow() {
        int cap = a.length + (a.length >> 1) + 1;
        a = java.util.Arrays.copyOf(a, cap);
        b = java.util.Arrays.copyOf(b, cap);
        w = java.util.Arrays.copyOf(w, cap);
    }

    public int size()        { return size; }
    public int from(int i)   { return a[i]; }
    public int to(int i)     { return b[i]; }
    public long weight(int i){ return w[i]; }

    /** Construye la vista CSR dirigida de estas aristas (una entrada por arista). */
    public WeightedGraph toDirectedGraph(int nodes) {
        WeightedGraph.Builder builder = new WeightedGraph.Builder(nodes, size);
        for (int i = 0; i < size; i++) builder.addDirected(a[i], b[i], w[i]);
        return builder.build();
    }

    /** Construye la vista CSR no dirigida (dos entradas por arista). */
    public WeightedGraph toUndirectedGraph(int nodes) {
        WeightedGraph.Builder builder = new WeightedGraph.Builder(nodes, size * 2);
        for (int i = 0; i < size; i++) builder.addUndirected(a[i], b[i], w[i]);
        return builder.build();
    }
}
