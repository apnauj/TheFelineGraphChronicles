package com.eia.feline.algo.sp;

/**
 * Resultado de Dijkstra desde un origen fijo.
 *
 * Los costos son long y el centinela de "sin ruta" es Long.MAX_VALUE. Nunca se
 * hace aritmetica sobre el centinela: la relajacion solo se ejecuta para nodos
 * que ya salieron de la cola de prioridad, y esos tienen distancia finita por
 * construccion.
 *
 * settleOrder guarda el orden en que los nodos quedaron definitivamente
 * resueltos, que es exactamente el orden en que Dijkstra los saca del heap. Es lo
 * que anima la mision: se ve el frente avanzando siempre por el nodo pendiente
 * mas barato.
 */
public record ShortestPathResult(long[] dist, int[] parent, int[] settleOrder, int settledCount) {

    public static final long UNREACHABLE = Long.MAX_VALUE;

    public long costTo(int node)     { return dist[node]; }
    public boolean reached(int node) { return dist[node] != UNREACHABLE; }

    public int[] settled() { return java.util.Arrays.copyOf(settleOrder, settledCount); }

    /** Camino minimo del origen a node, ambos incluidos; vacio si no se alcanza. */
    public int[] pathTo(int node) {
        if (!reached(node)) return new int[0];
        int length = 0;
        for (int at = node; at != -1; at = parent[at]) length++;
        int[] path = new int[length];
        int at = node;
        for (int i = length - 1; i >= 0; i--) {
            path[i] = at;
            at = parent[at];
        }
        return path;
    }
}
