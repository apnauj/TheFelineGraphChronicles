package com.eia.feline.algo.search;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * BFS con una cola FIFO. Devuelve la distancia MINIMA en numero de pasos de
 * start a end, o -1 si no es alcanzable.
 *
 * (Antes este comentario decia "cola explicita". Es la pila del DFS la que se
 * llama explicita, porque lo es frente a la recursion; en el BFS la cola no se
 * opone a nada y el adjetivo solo despistaba.)
 *
 * Se marca visitado AL ENCOLAR: eso es lo que garantiza que cada nodo se descubra
 * por el camino mas corto y que entre a la cola exactamente una vez.
 *
 * POR QUE BFS EN LA MISION 1: en un grafo no ponderado todas las aristas cuestan
 * lo mismo, asi que el orden de descubrimiento por niveles coincide con el orden
 * por distancia. El primer momento en que se toca a Nina es, necesariamente, por
 * el camino mas corto. Esa garantia la da BFS y no la da DFS.
 *
 * Ademas de la distancia se guardan parent[] y el orden de visita, que son lo que
 * la interfaz grafica necesita para dibujar el camino y para animar la busqueda.
 * Sin parent[] no hay camino que resaltar; sin el orden de visita no se ve la
 * unica diferencia real con el DFS, que es la forma en que crece la frontera.
 *
 * Complejidad: O(V + E) tiempo. En la cuadricula E <= 4V, asi que es O(R * C).
 * Espacio: O(V).
 */
public final class BFS {

    private BFS() {}

    public static SearchResult search(List<List<Integer>> adj, int start, int end) {
        int n = adj.size();

        boolean[] visited = new boolean[n];
        int[] distance = new int[n];
        int[] parent = new int[n];
        Arrays.fill(distance, -1);
        Arrays.fill(parent, -1);

        int[] order = new int[n];
        int seen = 0;

        Queue<Integer> queue = new LinkedList<>();
        visited[start] = true;
        distance[start] = 0;
        queue.add(start);

        while (!queue.isEmpty()) {
            int node = queue.poll();
            order[seen++] = node;

            for (int nb : adj.get(node)) {
                if (!visited[nb]) {
                    visited[nb] = true;
                    distance[nb] = distance[node] + 1;
                    parent[nb] = node;

                    if (nb == end) {
                        order[seen++] = nb;   // para que la animacion llegue hasta Nina
                        return new SearchResult(distance, parent, order, seen);
                    }

                    queue.add(nb);
                }
            }
        }
        // start == end cae aqui: distance[start] ya vale 0.
        return new SearchResult(distance, parent, order, seen);
    }

    /** Forma escalar historica: solo la distancia. Se conserva por comodidad. */
    public static int bfs(List<List<Integer>> adj, int start, int end) {
        return search(adj, start, end).distanceTo(end);
    }
}
