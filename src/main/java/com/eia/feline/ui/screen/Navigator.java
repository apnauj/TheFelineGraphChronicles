package com.eia.feline.ui.screen;

import javafx.scene.Node;

/**
 * Lo unico que una pantalla sabe del resto de la aplicacion: como pedir que se
 * muestre otra. Mantenerlo en una sola funcion evita que las pantallas se
 * referencien entre si y que aparezcan ciclos de dependencias.
 */
@FunctionalInterface
public interface Navigator {
    void go(Node screen);
}
