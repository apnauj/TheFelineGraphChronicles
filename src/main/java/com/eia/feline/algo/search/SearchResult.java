package com.eia.feline.algo.search;

/**
 * Resultado de un recorrido no ponderado (BFS o DFS).
 *
 * Un entero con la distancia alcanza para imprimir la respuesta, pero no para
 * dibujar nada. Aqui viajan las dos piezas que la visualizacion necesita:
 *
 *  - parent[v]:   quien descubrio a v. Recorriendolo hacia atras desde el destino
 *                 se reconstruye el camino que hay que resaltar.
 *  - visitOrder:  el orden en que los nodos salieron de la cola (BFS) o de la pila
 *                 (DFS). Es lo que anima la busqueda, y es justamente lo que hace
 *                 visible la unica diferencia entre los dos algoritmos: recorren
 *                 el mismo grafo y solo cambian el orden de expansion de la
 *                 frontera. El BFS se abre en anillos, el DFS se hunde en una
 *                 rama.
 *
 * visitOrder tiene tamano size() pero solo sus primeras visitCount posiciones son
 * validas; usar visited() para obtener el recorte exacto.
 *
 * Nota: es un record con arreglos, asi que equals/hashCode son por identidad.
 * No se usa como clave de ningun mapa.
 */
public record SearchResult(int[] dist, int[] parent, int[] visitOrder, int visitCount) {

    /** Numero de movimientos hasta node, o -1 si no se alcanzo. */
    public int distanceTo(int node) { return dist[node]; }

    public boolean reached(int node) { return dist[node] >= 0; }

    /** Los nodos efectivamente expandidos, en orden. Alimenta la animacion. */
    public int[] visited() { return java.util.Arrays.copyOf(visitOrder, visitCount); }

    /**
     * Camino desde el origen hasta node, ambos incluidos, o un arreglo vacio si
     * node no es alcanzable. Se sube por parent[] y luego se invierte.
     */
    public int[] pathTo(int node) {
        if (!reached(node)) return new int[0];
        int[] path = new int[dist[node] + 1];
        int at = node;
        for (int i = path.length - 1; i >= 0; i--) {
            path[i] = at;
            at = parent[at];
        }
        return path;
    }
}
