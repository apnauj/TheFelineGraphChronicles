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

        // Filas, Columnas, Número de Filas con Bombas
        int R, C, numberOfRowsWithBombs;

        R = Integer.parseInt(sc.nextLine());
        C = Integer.parseInt(sc.nextLine());
        numberOfRowsWithBombs = Integer.parseInt(sc.nextLine());

        // Bombas guardadas como ÍNDICES LINEALES en un HashSet -> contains() en O(1)
        Set<Integer> bombs = new HashSet<>();

        int rowNumber, numberOfBombs, col;

        for (int i = 0; i < numberOfRowsWithBombs; i++) {
            rowNumber = Integer.parseInt(sc.nextLine());
            numberOfBombs = Integer.parseInt(sc.nextLine());
            for (int j = 0; j < numberOfBombs; j++) {
                col = Integer.parseInt(sc.nextLine());
                // Única conversión (fila, columna) -> índice lineal de todo el programa.
                // Si el input ya te da índices lineales, cambia esto por: bombs.add(col);
                bombs.add(rowNumber * C + col);
            }
        }

        // Grafo de la cuadrícula, ya sin las bombas
        List<List<Integer>> adj = adjList(R, C, bombs);

        int rowStart, colStart, rowEnd, colEnd;
        rowStart = Integer.parseInt(sc.nextLine());
        colStart = Integer.parseInt(sc.nextLine());
        rowEnd = Integer.parseInt(sc.nextLine());
        colEnd = Integer.parseInt(sc.nextLine());

        int start = rowStart * C + colStart;
        int end = rowEnd * C + colEnd;

        System.out.println(BFS.bfs(adj, start, end));
        System.out.println(DFS.dfs(adj, start, end));
    }
}