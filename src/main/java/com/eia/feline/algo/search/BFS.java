package com.eia.feline.algo.search;

import com.eia.feline.algo.graph.Adjacency;

import java.util.Arrays;

/**
 * BFS con cola explicita. Devuelve la distancia MINIMA en numero de pasos de
 * start a end, o -1 si no es alcanzable.
 *
 * Se marca visitado AL ENCOLAR: eso es lo que garantiza que cada nodo se descubra
 * por el camino mas corto y que entre a la cola exactamente una vez (por eso un
 * arreglo de n posiciones alcanza como cola y no hace falta una LinkedList).
 *
 * POR QUE BFS EN LA MISION 1: en un grafo no ponderado todas las aristas cuestan
 * lo mismo, asi que el orden de descubrimiento por niveles coincide con el orden
 * por distancia. El primer momento en que se toca a Nina es, necesariamente, por
 * el camino mas corto. Esa garantia la da BFS y no la da DFS.
 *
 * Complejidad: O(V + E) tiempo. En la cuadricula E <= 4V, asi que es O(R * C).
 * Espacio: O(V) -- cola, visitados, distancias, padres y orden de visita.
 */
public final class BFS {

    private BFS() {}

    public static SearchResult search(Adjacency g, int start, int end) {
        int n = g.size();

        boolean[] visited = new boolean[n];
        int[] distance = new int[n];
        int[] parent = new int[n];
        Arrays.fill(distance, -1);
        Arrays.fill(parent, -1);

        int[] order = new int[n];
        int seen = 0;

        // Cada nodo entra a la cola a lo sumo una vez (se marca al encolar),
        // asi que n posiciones bastan y los indices nunca se dan la vuelta.
        int[] queue = new int[n];
        int head = 0, tail = 0;

        visited[start] = true;
        distance[start] = 0;
        queue[tail++] = start;

        while (head < tail) {
            int node = queue[head++];
            order[seen++] = node;

            for (int e = g.adjStart(node); e < g.adjEnd(node); e++) {
                int nb = g.adjTarget(e);
                if (!visited[nb]) {
                    visited[nb] = true;
                    distance[nb] = distance[node] + 1;
                    parent[nb] = node;

                    if (nb == end) {
                        order[seen++] = nb;   // para que la animacion llegue hasta Nina
                        return new SearchResult(distance, parent, order, seen);
                    }

                    queue[tail++] = nb;
                }
            }
        }
        // start == end cae aqui: distance[start] ya vale 0.
        return new SearchResult(distance, parent, order, seen);
    }

    /** Forma escalar historica: solo la distancia. Se conserva por comodidad. */
    public static int bfs(Adjacency g, int start, int end) {
        return search(g, start, end).distanceTo(end);
    }
}
