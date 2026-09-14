package com.eia.feline.missions;

import java.util.List;

/**
 * Contrato unico entre la logica y la interfaz grafica.
 *
 * Un solver recibe el texto crudo del area de entrada y devuelve un resultado por
 * caso de prueba. Cada resultado trae DOS cosas:
 *
 *  - outputLine: la linea exacta en ASCII que hay que imprimir, ya formateada.
 *    La GUI solo la concatena; nunca la reinterpreta ni la vuelve a formatear.
 *    Asi el texto comparado automaticamente se genera en un solo lugar.
 *  - payload: el estado estructurado que la visualizacion necesita (recorridos,
 *    caminos, matrices, aristas escogidas). La GUI no sabe nada de algoritmos:
 *    solo dibuja lo que venga aqui.
 *
 * Ninguna implementacion importa JavaFX. El contrapositivo tambien vale: la GUI
 * no importa nada de algo/.
 *
 * @param <P> tipo del payload de visualizacion de esta mision.
 */
public interface MissionSolver<P> {

    /** Nombre de la mision, para el titulo de la pantalla. */
    String title();

    /** Entrada de ejemplo del enunciado. Alimenta el boton "cargar ejemplo". */
    String sampleInput();

    List<CaseResult<P>> solve(String raw) throws InputFormatException;

    /** Une las lineas de salida tal como deben aparecer en el area de resultados. */
    static String render(List<? extends CaseResult<?>> results) {
        StringBuilder sb = new StringBuilder();
        for (CaseResult<?> r : results) sb.append(r.outputLine()).append('\n');
        return sb.toString();
    }
}
