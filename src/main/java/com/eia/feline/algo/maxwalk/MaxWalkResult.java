package com.eia.feline.algo.maxwalk;

/**
 * Resultado de Bellman-Ford (maximizacion) desde un origen fijo, para la Mision 3.
 *
 * dist trae el maximo churun acumulable en un PASEO desde el origen hasta cada
 * nodo, o NONE si no es alcanzable. parent permite reconstruir esa ruta subiendo
 * hacia atras. unbounded marca los nodos alimentados por un ciclo de ganancia
 * positiva alcanzable desde el origen -- el churun hacia ellos no esta acotado.
 * cycle es un ciclo positivo responsable, para resaltarlo en el dibujo; vacio si
 * no existe ninguno alcanzable desde el origen.
 *
 * Nunca se hace aritmetica sobre NONE: toda relajacion en BellmanFord comprueba
 * antes que el origen de la arista sea alcanzable.
 */
public record MaxWalkResult(long[] dist, int[] parent, boolean[] unbounded, int[] cycle) {

    public static final long NONE = BellmanFord.NONE;

    public long churunTo(int node)       { return dist[node]; }
    public boolean reached(int node)     { return dist[node] != NONE; }
    public boolean isUnbounded(int node) { return unbounded[node]; }
}
