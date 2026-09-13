package main.java.com.structures;

import java.util.*;

public class Djikstra {

    private static class Edge {
        int target;
        int weight;

        public Edge(int target, int weight) {
            this.weight = weight;
            this.target = target;
        }
    }

    int V;
    Map<Integer, List<Edge>> adj;

    public Djikstra(int v) {
        V = v;
        adj = new HashMap<>();
    }

    public void addEdge(int A, int B, int W){
        addConnection(B, A, W);
        addConnection(A, B, W);
    }

    private void addConnection(int A, int B, int W) {
        if(adj.containsKey(B)){
            boolean found = false;
            for (Edge e : adj.get(B)){
                if(e.target == A){
                    e.weight = Math.min(W, e.weight);
                    found = true;
                }
            }
            if(!found) adj.get(B).add(new Edge(A, W));
        } else {
            adj.put(B, new ArrayList<>(List.of(new Edge(A, W))));
        }
    }

    public int dijkstra(int start, int end) {
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;

        // Min-heap: ordena por distancia
        PriorityQueue<int[]> pq =
                new PriorityQueue<>(
                        Comparator.comparingInt(a -> a[1])
                );
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int u = curr[0], d = curr[1];

            // Obsoleto si ya hay mejor camino
            if (d > dist[u]) continue;

            // RELAJACIÓN de aristas
            for (Edge e : adj.getOrDefault(u, List.of())) {
                if (dist[u] + e.weight < dist[e.target]) {
                    dist[e.target] = dist[u] + e.weight;
                    pq.add(new int[]{
                            e.target, dist[e.target]
                    });
                }
            }
        }
        return dist[end];
    }
}
