package com.eia.feline.missions;

/**
 * Flujo de tokens separados por espacios sobre un String.
 *
 * Reemplaza a Scanner(System.in) por dos razones:
 *
 *  1. La entrada llega de un area de texto de la GUI, no de la consola.
 *  2. Scanner.nextInt() lanza InputMismatchException / NoSuchElementException,
 *     que no le dicen nada al usuario. Aqui cada lectura declara QUE esperaba y
 *     el error resultante nombra el token exacto y su posicion.
 *
 * Partir por \s+ ya tolera lineas en blanco, espacios sobrantes y saltos de linea
 * en cualquier lugar, que es lo que pide la seccion 2.2: la entrada es un flujo de
 * tokens y no un formato de lineas fijas.
 */
public final class Tokenizer {

    private final String[] tokens;
    private int pos;

    public Tokenizer(String text) {
        String trimmed = (text == null) ? "" : text.strip();
        this.tokens = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
    }

    public boolean hasNext() { return pos < tokens.length; }

    /** Posicion del proximo token, empezando en 1. Solo para mensajes de error. */
    public int nextPosition() { return pos + 1; }

    public int nextInt(String expected) throws InputFormatException {
        long value = nextLong(expected);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new InputFormatException(
                    "El valor de " + expected + " (" + value + ") no cabe en un entero de 32 bits"
                            + " (token #" + pos + ")");
        }
        return (int) value;
    }

    /** Lee un entero y verifica que este en [low, high]. */
    public int nextInt(String expected, int low, int high) throws InputFormatException {
        int at = nextPosition();
        int value = nextInt(expected);
        if (value < low || value > high) {
            throw new InputFormatException(
                    "El valor de " + expected + " es " + value + ", pero debe estar entre "
                            + low + " y " + high + " (token #" + at + ")");
        }
        return value;
    }

    public long nextLong(String expected) throws InputFormatException {
        if (!hasNext()) {
            throw new InputFormatException(
                    "La entrada se acabo antes de tiempo: faltaba " + expected
                            + " (token #" + nextPosition() + ")");
        }
        String token = tokens[pos++];
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException e) {
            throw new InputFormatException(
                    "Se esperaba un numero entero para " + expected + " pero se encontro \""
                            + token + "\" (token #" + pos + ")");
        }
    }
}
