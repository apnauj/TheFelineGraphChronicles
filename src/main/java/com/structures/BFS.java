package main.java.com.structures;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * BFS con cola explícita. Imprime y devuelve la distancia MÍNIMA en número de pasos
 * de start a end, o -1 si no es alcanzable.
 *
 * Se marca visitado AL ENCOLAR: eso es lo que garantiza que cada nodo se descubra
 * por el camino más corto y que entre a la cola exactamente una vez (por eso el
 * arreglo de n posiciones alcanza).
 */

public class BFS {
    public static int bfs(List<List<Integer>> adj, int start, int end) {
        int n = adj.size();

        if (start == end) {
            return 0;
        }

        boolean[] visited = new boolean[n];
        int[] distance = new int[n];

        Queue<Integer> queue = new LinkedList<>();
        visited[start] = true;
        distance[start] = 0;
        queue.add(start);

        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (int nb : adj.get(node)) {
                if (!visited[nb]) {
                    visited[nb] = true;
                    distance[nb] = distance[node] + 1;

                    if (nb == end) {
                        return distance[nb];
                    }

                    queue.add(nb);
                }
            }
        }
        return -1;
    }
}
