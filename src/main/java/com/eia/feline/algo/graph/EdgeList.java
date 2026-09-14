package com.eia.feline.algo.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de aristas: la arista i es edges.get(i), con origen, destino y peso.
 *
 * Es la representacion que quieren los algoritmos centrados en aristas y no en
 * nodos: Kruskal (Mision 4), que las ordena por peso, y Bellman-Ford (Mision 3),
 * que relaja las M aristas N-1 veces. Para esos dos recorrer una lista plana de
 * aristas es lo natural; la lista de adyacencia se construye aparte cuando hace
 * falta.
 *
 * Se guardan TODAS las aristas, incluidos duplicados y lazos: el enunciado lo
 * permite explicitamente y evita el costo de deduplicar.
 *
 * Los pesos son long porque los acumulados de las Misiones 2, 3 y 4 no caben en
 * int (10^4 nodos x 10^6 de peso = 10^10).
 */
public final class EdgeList {

    /** Una arista suelta: de donde sale, a donde llega y cuanto vale. */
    public record Edge(int from, int to, long weight) {}

    private final List<Edge> edges;

    public EdgeList() {
        edges = new ArrayList<>();
    }

    public EdgeList(int expectedEdges) {
        edges = new ArrayList<>(Math.max(4, expectedEdges));
    }

    public void add(int from, int to, long weight) {
        edges.add(new Edge(from, to, weight));
    }

    public int size()          { return edges.size(); }
    public Edge get(int i)     { return edges.get(i); }
    public int from(int i)     { return edges.get(i).from(); }
    public int to(int i)       { return edges.get(i).to(); }
    public long weight(int i)  { return edges.get(i).weight(); }

    /** Construye la lista de adyacencia dirigida de estas aristas. */
    public WeightedGraph toDirectedGraph(int nodes) {
        WeightedGraph g = new WeightedGraph(nodes);
        for (Edge e : edges) g.addDirected(e.from(), e.to(), e.weight());
        return g;
    }

    /** Construye la lista de adyacencia no dirigida (cada arista en los dos sentidos). */
    public WeightedGraph toUndirectedGraph(int nodes) {
        WeightedGraph g = new WeightedGraph(nodes);
        for (Edge e : edges) g.addUndirected(e.from(), e.to(), e.weight());
        return g;
    }
}
