package com.eia.feline.algo.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Grafo con pesos como lista de adyacencia: la lista de la posicion v son las
 * aristas que salen de v.
 *
 * Internamente todo es dirigido; una arista no dirigida simplemente se guarda dos
 * veces, una en cada sentido.
 *
 * Se conservan los duplicados y los lazos. El enunciado de la Mision 2 permite
 * "quedarse con la mas barata o guardarlas todas", y guardarlas todas es lo
 * correcto aqui: deduplicar obliga a recorrer la lista de vecinos por cada
 * arista, y con 100.000 aristas incidentes a un mismo nodo esa busqueda se vuelve
 * cuadratica. Dijkstra relaja las aristas repetidas sin inmutarse -- la mas
 * barata gana sola.
 *
 * Complejidad de la construccion: O(N + M) tiempo y espacio.
 */
public final class WeightedGraph {

    /** Una arista saliente: a donde va y cuanto cuesta. */
    public record Edge(int target, long weight) {}

    private final List<List<Edge>> adj;

    public WeightedGraph(int nodes) {
        adj = new ArrayList<>(nodes);
        for (int i = 0; i < nodes; i++) adj.add(new ArrayList<>());
    }

    public WeightedGraph addDirected(int from, int to, long weight) {
        adj.get(from).add(new Edge(to, weight));
        return this;
    }

    public WeightedGraph addUndirected(int a, int b, long weight) {
        addDirected(a, b, weight);
        addDirected(b, a, weight);
        return this;
    }

    /** Las aristas que salen de v. */
    public List<Edge> neighbours(int v) { return adj.get(v); }

    public int size() { return adj.size(); }
}
