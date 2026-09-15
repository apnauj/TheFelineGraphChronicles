package com.eia.feline.missions;

/**
 * Un caso de prueba resuelto.
 *
 * @param index      k de "Case #k", empezando en 1 y contado por mision.
 * @param outputLine la linea exacta, en ASCII plano, sin punto final y sin emojis.
 * @param payload    estado estructurado para dibujar; puede ser null si no hay nada que dibujar.
 */
public record CaseResult<P>(int index, String outputLine, P payload) {}
