package com.eia.feline.algo.mst;

/**
 * Resultado de Kruskal: el arbol de expansion minima, o la razon de que no se
 * pueda formar.
 *
 * order trae los INDICES de los cables en el orden en que Kruskal los examino
 * (de mas barato a mas caro); accepted[i] dice si el cable order[i] entro al
 * arbol. Se guardan indices y no los cables mismos porque el dibujo necesita
 * que cada cable se quede en su posicion original de lectura.
 *
 * total es el costo del arbol, valido solo si connected es true. components
 * cuenta cuantas partes sueltas quedaron si no se pudo conectar todo.
 */
public record MstResult(int[] order, boolean[] accepted, long total, boolean connected, int components) {}
