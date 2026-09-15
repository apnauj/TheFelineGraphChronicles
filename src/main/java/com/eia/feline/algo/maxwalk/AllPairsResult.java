package com.eia.feline.algo.maxwalk;

/**
 * Resultado de Floyd-Warshall (maximizacion) entre TODOS los pares de nodos,
 * para la Mision 3.
 *
 * best[i][j] es el mayor churun acumulable en un PASEO desde i hasta j, o NONE si
 * j no es alcanzable desde i. unbounded[i][j] marca los pares para los que ese
 * maximo no esta acotado, porque existe un nodo k tal que se puede llegar de i a
 * k, k esta en un ciclo de ganancia positiva, y desde k se puede llegar a j.
 *
 * best[i][i] vale 0 salvo que algun ciclo por i mejore ese valor.
 */
public record AllPairsResult(long[][] best, boolean[][] unbounded) {

    public long churunBetween(int i, int j)  { return best[i][j]; }
    public boolean reached(int i, int j)     { return best[i][j] != FloydWarshall.NONE; }
    public boolean isUnbounded(int i, int j) { return unbounded[i][j]; }
}
