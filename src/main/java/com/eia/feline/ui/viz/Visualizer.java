package com.eia.feline.ui.viz;

import javafx.scene.layout.Region;

/**
 * Como se dibuja una mision.
 *
 * MissionScreen no sabe nada de cuadriculas ni de grafos: le pide un nodo al
 * visualizador, le entrega el payload de un caso y deja que se pinte solo.
 *
 * La seccion 2.3 del enunciado pone topes de tamano al dibujo (50x50 celdas,
 * 60 nodos, 100 intersecciones y 300 cables). Por encima de esos topes la
 * respuesta numerica se tiene que seguir mostrando junto a un mensaje visible
 * que explique por que no hay dibujo. Esa decision vive en withinDrawBudget /
 * overBudgetMessage y la pantalla la aplica en un solo sitio para las cuatro
 * misiones.
 *
 * @param <P> payload del caso que sabe dibujar.
 */
public interface Visualizer<P> {

    /** La superficie de dibujo, ya construida y lista para meter en la pantalla. */
    Region node();

    /** Pinta el estado final del caso: camino resuelto, ciclo, aristas escogidas. */
    void render(P payload);

    /**
     * Prepara la reproduccion paso a paso y devuelve cuantos pasos tiene.
     * Devolver 0 significa que este caso no se anima.
     */
    int prepareSteps(P payload);

    /** Muestra el estado del paso i, con 0 <= i <= prepareSteps(payload). */
    void showStep(int step);

    /** false si el caso supera los topes de la seccion 2.3 y no se debe dibujar. */
    boolean withinDrawBudget(P payload);

    /** Explicacion visible de por que se omitio el dibujo. */
    String overBudgetMessage(P payload);
}
