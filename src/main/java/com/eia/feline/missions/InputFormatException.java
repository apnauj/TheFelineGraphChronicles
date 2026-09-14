package com.eia.feline.missions;

/**
 * Entrada mal formada. Es una excepcion CHEQUEADA a proposito: obliga a que todo
 * el que llame a un solver decida que hacer con ella, y asi la GUI no puede
 * "olvidarse" y dejar escapar un stack trace, que es justo lo que prohibe el
 * enunciado.
 */
public class InputFormatException extends Exception {
    public InputFormatException(String message) {
        super(message);
    }
}
