package com.eia.feline.ui;

/**
 * Punto de entrada.
 *
 * NO extiende Application a proposito. Cuando la clase principal hereda de
 * Application, el lanzador de Java exige que los modulos de JavaFX esten en el
 * module-path y se niega a arrancar desde un jar corriente con un mensaje
 * confuso. Con esta indireccion de una linea, "mvn javafx:run" y "java -cp ..."
 * arrancan igual.
 */
public final class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
