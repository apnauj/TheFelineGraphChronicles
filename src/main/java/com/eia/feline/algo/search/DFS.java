package com.eia.feline.algo.search;

import com.eia.feline.algo.graph.Adjacency;

import java.util.Arrays;

/**
 * DFS ITERATIVO con pila explicita. Devuelve la longitud del camino que encontro
 * hasta end, o -1 si no es alcanzable.
 *
 * OJO: esa longitud es la profundidad en el arbol de recorrido, NO la distancia
 * minima. Para la distancia minima esta el BFS. La diferencia entre los dos
 * numeros (18 contra 32 en el ejemplo del enunciado) es el punto de la mision.
 *
 * POR QUE NO ES RECURSIVO: el enunciado permite cuadriculas de hasta 10^6 celdas.
 * Un DFS recursivo abriria hasta 10^6 marcos de pila y desbordaria la pila de la
 * JVM mucho antes. La pila explicita mueve ese estado al heap, donde si cabe, y
 * ademas deja el recorrido inspeccionable para animarlo.
 *
 * Se marca visitado AL SACAR (no al empujar) para que el recorrido sea identico
 * al del DFS recursivo canonico con orden up, down, left, right. Como
 * consecuencia un nodo puede entrar varias veces a la pila, asi que la cota es
 * (aristas + 1) en vez de V. En una cuadricula eso es a lo sumo 4V + 1.
 *
 * El orden up, down, left, right NO esta en este archivo: sale de que GridGraph
 * guarda los vecinos como right, left, down, up y la pila es LIFO. Ver GridGraph.
 *
 * Complejidad: O(V + E) tiempo. Espacio: O(V + E) por la pila explicita.
 */
public final class DFS {

    private DFS() {}

    public static SearchResult search(Adjacency g, int start, int end) {
        int n = g.size();

        boolean[] visited = new boolean[n];
        int[] distance = new int[n];
        int[] parent = new int[n];
        Arrays.fill(distance, -1);
        Arrays.fill(parent, -1);

        int[] order = new int[n];
        int seen = 0;

        // Cota de la pila: cada arista puede empujar su destino una vez, mas el origen.
        int capacity = g.edgeCount() + 1;
        int[] stack = new int[capacity];   // el nodo pendiente
        int[] from = new int[capacity];    // quien lo empujo, en la misma posicion
        int top = 0;

        stack[top] = start;
        from[top] = -1;                    // el origen no tiene padre
        top++;

        while (top > 0) {
            top--;
            int node = stack[top];
            int p = from[top];

            if (visited[node]) continue;   // duplicado: otra rama ya lo proceso
            visited[node] = true;
            distance[node] = (p == -1) ? 0 : distance[p] + 1;
            parent[node] = p;
            order[seen++] = node;

            if (node == end) {
                return new SearchResult(distance, parent, order, seen);
            }

            for (int e = g.adjStart(node); e < g.adjEnd(node); e++) {
                int nb = g.adjTarget(e);
                if (!visited[nb]) {
                    stack[top] = nb;
                    from[top] = node;
                    top++;
                }
            }
        }

        return new SearchResult(distance, parent, order, seen);
    }

    /** Forma escalar historica: solo la longitud del camino hallado. */
    public static int dfs(Adjacency g, int start, int end) {
        return search(g, start, end).distanceTo(end);
    }
}
