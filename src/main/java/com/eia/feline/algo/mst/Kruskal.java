package com.eia.feline.algo.mst;

import com.eia.feline.algo.graph.EdgeList;

import java.util.Arrays;

/**
 * Kruskal con union-find para la Mision 4: el costo minimo para conectar todas
 * las intersecciones con los cables disponibles (Arbol de Expansion Minima).
 *
 * POR QUE KRUSKAL EN LA MISION 4: el problema pide exactamente eso -- conectar
 * todos los nodos de un grafo no dirigido y ponderado con el menor costo total
 * y sin ciclos. Kruskal es greedy sobre las aristas ordenadas por costo: un
 * cable entra al arbol si y solo si sus dos extremos todavia no estan
 * conectados, y el union-find responde esa pregunta (y aplica la union) en
 * tiempo casi constante amortizado.
 *
 * Complejidad: O(C log C) tiempo, dominado por ordenar los C cables (las
 * operaciones de union-find con compresion de caminos y union por tamano son
 * practicamente O(1) amortizado). Espacio: O(N + C).
 */
public final class Kruskal {

    private Kruskal() {}

    public static MstResult run(int nodes, EdgeList cables) {
        int m = cables.size();

        // Ordenar por costo: se ordenan los INDICES para no mover las aristas, que
        // la visualizacion necesita en su posicion original.
        Integer[] boxed = new Integer[m];
        for (int i = 0; i < m; i++) boxed[i] = i;
        Arrays.sort(boxed, (a, b) -> Long.compare(cables.weight(a), cables.weight(b)));

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
        return new MstResult(order, accepted, total, components == 1, components);
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
