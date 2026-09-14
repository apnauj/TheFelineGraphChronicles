package com.eia.feline.missions.stub;

import com.eia.feline.algo.graph.EdgeList;

/**
 * ANDAMIAJE TEMPORAL -- ver el README de este paquete.
 *
 * Kruskal con union-find para la Mision 4, escrito solo para poder construir y
 * demostrar la interfaz. La version definitiva la escribe otro integrante en
 * com.eia.feline.algo.mst y esta clase se borra.
 *
 * Complejidad: O(C log C), dominada por ordenar los cables. El union-find con
 * compresion de caminos y union por tamano responde en tiempo practicamente
 * constante amortizado.
 */
public final class ReferenceMst {

    private ReferenceMst() {}

    /**
     * @param order    indices de los cables en el orden en que Kruskal los examina
     * @param accepted accepted[i] indica si el cable order[i] entro en el arbol
     * @param total    costo total del arbol, valido solo si connected es true
     */
    public record Result(int[] order, boolean[] accepted, long total, boolean connected,
                         int components) {}

    public static Result kruskal(int nodes, EdgeList cables) {
        int m = cables.size();

        // Ordenar por costo: se ordenan los INDICES para no mover las aristas, que
        // la visualizacion necesita en su posicion original.
        Integer[] boxed = new Integer[m];
        for (int i = 0; i < m; i++) boxed[i] = i;
        java.util.Arrays.sort(boxed, (a, b) -> Long.compare(cables.weight(a), cables.weight(b)));

        int[] order = new int[m];
        for (int i = 0; i < m; i++) order[i] = boxed[i];

        UnionFind uf = new UnionFind(nodes);
        boolean[] accepted = new boolean[m];
        long total = 0;
        int joined = 0;

        for (int i = 0; i < m; i++) {
            int e = order[i];
            // union devuelve false si los dos extremos ya estaban conectados: ese
            // cable cerraria un ciclo y se descarta.
            if (uf.union(cables.from(e), cables.to(e))) {
                accepted[i] = true;
                total += cables.weight(e);
                joined++;
                if (joined == nodes - 1) break;     // el arbol ya esta completo
            }
        }

        int components = nodes - joined;
        return new Result(order, accepted, total, components == 1, components);
    }

    /** Union-find con compresion de caminos y union por tamano. */
    public static final class UnionFind {
        private final int[] parent;
        private final int[] size;

        public UnionFind(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        }

        /** Con compresion de caminos: cada nodo visitado se cuelga de la raiz. */
        public int find(int x) {
            int root = x;
            while (parent[root] != root) root = parent[root];
            while (parent[x] != root) {
                int next = parent[x];
                parent[x] = root;
                x = next;
            }
            return root;
        }

        /** Union por tamano: el arbol pequeno cuelga del grande, para no crecer en altura. */
        public boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;
            if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;
            size[ra] += size[rb];
            return true;
        }
    }
}
