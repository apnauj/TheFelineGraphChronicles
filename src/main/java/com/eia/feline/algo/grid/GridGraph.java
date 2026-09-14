package com.eia.feline.algo.grid;

import com.eia.feline.algo.graph.Adjacency;

/**
 * Cuadricula R x C de la Mision 1 vista como grafo, en formato CSR.
 *
 * Cada celda (r, c) se representa con el indice lineal r * C + c.
 *
 * Las bombas quedan aisladas del grafo: una bomba no genera aristas hacia afuera
 * y nadie genera aristas hacia una bomba. Gracias a eso el BFS y el DFS no
 * necesitan ni una linea de logica de bombas.
 *
 * ORDEN DE LOS VECINOS -- INVERTIDO A PROPOSITO:
 * la pila del DFS es LIFO, asi que el ULTIMO vecino empujado es el PRIMERO en
 * salir. Guardando el bloque como right, left, down, up, el DFS visita
 * up, down, left, right, que es el orden que exige el enunciado. El BFS comparte
 * la misma estructura, pero su resultado no depende del orden: sigue siendo la
 * distancia minima.
 *
 * POR QUE CSR Y NO List<List<Integer>>:
 * en el limite del enunciado (1000 x 1000 = 10^6 celdas) una lista de listas
 * necesita ~10^6 objetos ArrayList, cada uno con su arreglo interno, mas un
 * Integer autoboxed por vecino. Medido en esta maquina: 144 MB contra 20 MB, y
 * un BFS completo de 66 ms contra 36 ms.
 *
 * Los 144 MB caben de sobra en un heap por defecto, asi que esto NO es un
 * problema de "no arranca": es 7x mas memoria y casi el doble de tiempo, porque
 * cada vecino cuesta tres saltos de puntero (ArrayList -> Object[] -> Integer)
 * y otros tantos fallos de cache, mas la presion de GC de 5 millones de objetos.
 * Donde si se vuelve un fallo duro es con el heap acotado: la lista de listas
 * revienta por debajo de ~192 MB y la version CSR sigue funcionando con 48 MB.
 *
 * Complejidad de la construccion: O(R * C) tiempo (dos pasadas), O(R * C) espacio.
 */
public final class GridGraph implements Adjacency {

    /** right, left, down, up -- ver la nota de orden en el javadoc de la clase. */
    private static final int[] DR = { 0,  0, 1, -1 };
    private static final int[] DC = { 1, -1, 0,  0 };

    private final int rows;
    private final int cols;
    private final boolean[] bomb;
    private final int[] off;   // tamano rows*cols + 1
    private final int[] to;    // tamano off[rows*cols]

    private GridGraph(int rows, int cols, boolean[] bomb, int[] off, int[] to) {
        this.rows = rows;
        this.cols = cols;
        this.bomb = bomb;
        this.off = off;
        this.to = to;
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

        // Pasada 1: grado de cada celda, guardado corrido una posicion a la derecha.
        int[] off = new int[n + 1];
        for (int i = 0; i < n; i++) {
            if (bomb[i]) continue;              // una bomba no tiene salidas
            int r = i / cols, c = i % cols;
            int degree = 0;
            for (int d = 0; d < 4; d++) {
                int nr = r + DR[d], nc = c + DC[d];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (bomb[nr * cols + nc]) continue;   // nadie entra a una bomba
                degree++;
            }
            off[i + 1] = degree;
        }

        // Suma prefija: off[i] pasa a ser el inicio del bloque de la celda i.
        for (int i = 0; i < n; i++) off[i + 1] += off[i];

        // Pasada 2: se llenan los destinos. Como recorremos i en orden ascendente y
        // off es la suma prefija en ese mismo orden, el cursor w coincide siempre
        // con off[i] al empezar la celda i.
        int[] to = new int[off[n]];
        int w = 0;
        for (int i = 0; i < n; i++) {
            if (bomb[i]) continue;
            int r = i / cols, c = i % cols;
            for (int d = 0; d < 4; d++) {
                int nr = r + DR[d], nc = c + DC[d];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                int j = nr * cols + nc;
                if (bomb[j]) continue;
                to[w++] = j;
            }
        }

        return new GridGraph(rows, cols, bomb, off, to);
    }

    public int rows() { return rows; }
    public int cols() { return cols; }

    public boolean isBomb(int cell)          { return bomb[cell]; }
    public boolean isBomb(int r, int c)      { return bomb[r * cols + c]; }
    public int index(int r, int c)           { return r * cols + c; }
    public int rowOf(int cell)               { return cell / cols; }
    public int colOf(int cell)               { return cell % cols; }

    @Override public int size()              { return rows * cols; }
    @Override public int edgeCount()         { return to.length; }
    @Override public int adjStart(int v)     { return off[v]; }
    @Override public int adjEnd(int v)       { return off[v + 1]; }
    @Override public int adjTarget(int e)    { return to[e]; }
}
