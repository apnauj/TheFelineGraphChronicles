package main.java.com.missions;

import main.java.com.structures.BFS;
import main.java.com.structures.DFS;

import java.util.*;

public class MissionOne {

    /**
     * Construye la lista de adyacencia de una cuadrícula R x C, saltándose las bombas.
     *
     * Cada celda (r, c) se representa con el índice lineal  r * C + c.
     *
     * Las bombas quedan aisladas del grafo: una bomba no genera aristas hacia afuera
     * (el continue) y nadie genera aristas hacia una bomba (los !bombs.contains(...)).
     * Gracias a eso el DFS y el BFS no necesitan ni una línea de lógica de bombas.
     *
     * ORDEN DE LOS VECINOS — INVERTIDO A PROPÓSITO:
     * la pila del DFS es LIFO, así que el ÚLTIMO vecino empujado es el PRIMERO en salir.
     * Guardando la lista como right, left, down, up, el DFS visita up, down, left, right.
     * El BFS comparte esta lista, pero su resultado no depende del orden: sigue siendo
     * la distancia mínima.
     */
    public static List<List<Integer>> adjList(int R, int C, Set<Integer> bombs) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < R * C; i++) {
            adj.add(new ArrayList<>());
        }

        for (int i = 0; i < R * C; i++) {
            if (bombs.contains(i)) continue;   // una bomba no tiene salidas

            int r = i / C, c = i % C;

            if (c < C - 1 && !bombs.contains(i + 1)) adj.get(i).add(i + 1); // right
            if (c > 0     && !bombs.contains(i - 1)) adj.get(i).add(i - 1); // left
            if (r < R - 1 && !bombs.contains(i + C)) adj.get(i).add(i + C); // down
            if (r > 0     && !bombs.contains(i - C)) adj.get(i).add(i - C); // up
        }
        return adj;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Lectura anticipada: el primer caso se lee antes del while,
        // y cada vuelta termina leyendo el encabezado del siguiente.
        int R = sc.nextInt();
        int C = sc.nextInt();
        int t = 1;

        while (R != 0 || C != 0) {

            int numberOfRowsWithBombs = sc.nextInt();

            // Bombas guardadas como ÍNDICES LINEALES en un HashSet -> contains() en O(1)
            Set<Integer> bombs = new HashSet<>();

            for (int i = 0; i < numberOfRowsWithBombs; i++) {
                int rowNumber = sc.nextInt();
                int numberOfBombs = sc.nextInt();
                for (int j = 0; j < numberOfBombs; j++) {
                    // Única conversión (fila, columna) -> índice lineal de todo el programa.
                    bombs.add(rowNumber * C + sc.nextInt());
                }
            }

            // Grafo de la cuadrícula, ya sin las bombas
            List<List<Integer>> adj = adjList(R, C, bombs);

            int rowStart = sc.nextInt(), colStart = sc.nextInt();
            int rowEnd   = sc.nextInt(), colEnd   = sc.nextInt();

            int start = rowStart * C + colStart;
            int end   = rowEnd   * C + colEnd;

            int bfs = BFS.bfs(adj, start, end);
            int dfs = DFS.dfs(adj, start, end);

            if (bfs == -1 || dfs == -1) {
                System.out.printf("Case #%d: Nina is unreachable%n", t);
            } else {
                System.out.printf("Case #%d: BFS <%d> DFS <%d>%n", t, bfs, dfs);
            }

            t++;
            R = sc.nextInt();   // encabezado del siguiente caso (o el 0 0 final)
            C = sc.nextInt();
        }
    }
}