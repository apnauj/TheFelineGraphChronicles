package com.eia.feline.algo.grid;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuadricula R x C de la Mision 1 vista como grafo.
 *
 * Cada celda (r, c) se representa con el indice lineal r * C + c, y la lista de
 * adyacencia es una List<List<Integer>>: la lista de la posicion i son los
 * vecinos transitables de la celda i.
 *
 * Las bombas quedan aisladas del grafo: una bomba no genera aristas hacia afuera
 * (el continue) y nadie genera aristas hacia una bomba (los !bomb[...]). Gracias
 * a eso el BFS y el DFS no necesitan ni una linea de logica de bombas.
 *
 * ORDEN DE LOS VECINOS -- INVERTIDO A PROPOSITO:
 * la pila del DFS es LIFO, asi que el ULTIMO vecino empujado es el PRIMERO en
 * salir. Guardando la lista como right, left, down, up, el DFS visita
 * up, down, left, right, que es el orden que exige el enunciado. El BFS comparte
 * esta lista, pero su resultado no depende del orden: sigue siendo la distancia
 * minima.
 *
 * La clase no hace mas que construir esa lista y recordar el tamano de la
 * cuadricula y donde estan las bombas, que es lo que el dibujo necesita para
 * pasar de indice lineal a (fila, columna).
 *
 * Complejidad de la construccion: O(R * C) tiempo y espacio.
 */
public final class GridGraph {

    private final int rows;
    private final int cols;
    private final boolean[] bomb;
    private final List<List<Integer>> adj;

    private GridGraph(int rows, int cols, boolean[] bomb, List<List<Integer>> adj) {
        this.rows = rows;
        this.cols = cols;
        this.bomb = bomb;
        this.adj = adj;
    }

    /**
     * @param bomb arreglo de rows*cols posiciones; true si esa celda tiene bomba.
     *             Se guarda por referencia, no se copia: el llamador no debe mutarlo.
     */
    public static GridGraph of(int rows, int cols, boolean[] bomb) {
        int n = rows * cols;
        if (bomb.length != n) {
            throw new IllegalArgumentException("bomb.length=" + bomb.length + " pero rows*cols=" + n);
        }

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        for (int i = 0; i < n; i++) {
            if (bomb[i]) continue;             // una bomba no tiene salidas

            int r = i / cols, c = i % cols;

            if (c < cols - 1 && !bomb[i + 1])    adj.get(i).add(i + 1);    // right
            if (c > 0        && !bomb[i - 1])    adj.get(i).add(i - 1);    // left
            if (r < rows - 1 && !bomb[i + cols]) adj.get(i).add(i + cols); // down
            if (r > 0        && !bomb[i - cols]) adj.get(i).add(i - cols); // up
        }

        return new GridGraph(rows, cols, bomb, adj);
    }

    /** La lista de adyacencia, que es lo que reciben el BFS y el DFS. */
    public List<List<Integer>> adjacency() { return adj; }

    public int rows() { return rows; }
    public int cols() { return cols; }
    public int size() { return rows * cols; }

    public boolean isBomb(int cell)     { return bomb[cell]; }
    public boolean isBomb(int r, int c) { return bomb[r * cols + c]; }

    public int index(int r, int c) { return r * cols + c; }
    public int rowOf(int cell)     { return cell / cols; }
    public int colOf(int cell)     { return cell % cols; }
}
