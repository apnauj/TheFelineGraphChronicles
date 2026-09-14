package com.eia.feline.algo.graph;

/**
 * Vista de solo lectura sobre una lista de adyacencia almacenada en formato CSR
 * (Compressed Sparse Row): dos arreglos planos en vez de una lista de listas.
 *
 * Los vecinos de v ocupan las posiciones [adjStart(v), adjEnd(v)) del arreglo de
 * destinos, y se recorren asi:
 *
 *     for (int e = g.adjStart(v); e < g.adjEnd(v); e++) {
 *         int vecino = g.adjTarget(e);
 *         ...
 *     }
 *
 * BFS y DFS trabajan contra esta interfaz y no contra una clase concreta, de modo
 * que el mismo codigo sirve para la cuadricula de la Mision 1 y para cualquier
 * grafo de prueba construido a mano.
 */
public interface Adjacency {

    /** Numero de nodos. Los nodos son 0 .. size()-1. */
    int size();

    /** Numero total de arcos dirigidos almacenados (cada arista no dirigida cuenta 2). */
    int edgeCount();

    /** Primera posicion del bloque de vecinos de v. */
    int adjStart(int v);

    /** Posicion siguiente a la ultima del bloque de vecinos de v. */
    int adjEnd(int v);

    /** Nodo destino almacenado en la posicion e. */
    int adjTarget(int e);
}
