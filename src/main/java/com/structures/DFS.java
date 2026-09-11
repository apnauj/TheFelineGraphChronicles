package main.java.com.structures;

import java.util.List;
/**
 * DFS iterativo con pila explícita. Imprime y devuelve la longitud del camino que
 * encontró hasta end, o -1 si no es alcanzable.
 *
 * OJO: esa longitud es la profundidad en el árbol de recorrido, NO la distancia
 * mínima. Para la distancia mínima, el BFS.
 *
 * Se marca visitado AL SACAR (no al empujar) para que el recorrido sea idéntico al
 * del DFS recursivo canónico con orden up, down, left, right. Como consecuencia un
 * nodo puede entrar varias veces a la pila, así que la cota es (aristas + 1) en vez
 * de V. En un grid eso es a lo sumo 4V + 1.
 */
public class DFS {
    public static int dfs(List<List<Integer>> adj, int start, int end) {
        int n = adj.size();

        if (start == end) {
            return 0;
        }

        boolean[] visited = new boolean[n];
        int[] distance = new int[n];

        // Cota de la pila: cada arista puede empujar su destino una vez, más el origen.
        int edges = 0;
        for (List<Integer> neighbours : adj) edges += neighbours.size();

        int[] stack = new int[edges + 1];   // el nodo pendiente
        int[] parent = new int[edges + 1];  // quién lo empujó, en la misma posición
        int top = 0;

        stack[top] = start;
        parent[top] = -1;                   // el origen no tiene padre
        top++;

        while (top > 0) {
            top--;
            int node = stack[top];
            int p = parent[top];

            if (visited[node]) continue;    // duplicado: otra rama ya lo procesó
            visited[node] = true;
            distance[node] = (p == -1) ? 0 : distance[p] + 1;

            if (node == end) {
                return distance[node];
            }

            for (int nb : adj.get(node)) {
                if (!visited[nb]) {
                    stack[top] = nb;
                    parent[top] = node;
                    top++;
                }
            }
        }

        return -1;
    }

}
