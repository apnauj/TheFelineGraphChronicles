package com.eia.feline.missions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    @Test
    @DisplayName("Lee tokens sin importar como esten repartidos en las lineas")
    void readsTokensAcrossArbitraryWhitespace() throws Exception {
        Tokenizer t = new Tokenizer("\n  1 2\t3\r\n\n   4  \n");
        assertEquals(1, t.nextInt("a"));
        assertEquals(2, t.nextInt("b"));
        assertEquals(3, t.nextInt("c"));
        assertEquals(4, t.nextInt("d"));
        assertFalse(t.hasNext());
    }

    @Test
    @DisplayName("Una entrada vacia no tiene tokens")
    void emptyInputHasNoTokens() {
        assertFalse(new Tokenizer("   \n\n  ").hasNext());
        assertFalse(new Tokenizer("").hasNext());
        assertFalse(new Tokenizer(null).hasNext());
    }

    @Test
    @DisplayName("Un token no numerico nombra el token y su posicion")
    void nonNumericTokenIsNamedWithItsPosition() {
        Tokenizer t = new Tokenizer("7 churun");
        InputFormatException e = assertThrows(InputFormatException.class, () -> {
            t.nextInt("el primero");
            t.nextInt("el segundo");
        });
        assertTrue(e.getMessage().contains("churun"), e.getMessage());
        assertTrue(e.getMessage().contains("el segundo"), e.getMessage());
        assertTrue(e.getMessage().contains("token #2"), e.getMessage());
    }

    @Test
    @DisplayName("Quedarse sin tokens se reporta como entrada incompleta")
    void runningOutOfTokensIsReported() {
        Tokenizer t = new Tokenizer("1");
        InputFormatException e = assertThrows(InputFormatException.class, () -> {
            t.nextInt("el primero");
            t.nextInt("el segundo");
        });
        assertTrue(e.getMessage().contains("se acabo"), e.getMessage());
        assertTrue(e.getMessage().contains("el segundo"), e.getMessage());
    }

    @Test
    @DisplayName("El chequeo de rango dice cual era el rango permitido")
    void rangeCheckExplainsTheBounds() {
        Tokenizer t = new Tokenizer("42");
        InputFormatException e = assertThrows(InputFormatException.class,
                () -> t.nextInt("N", 0, 10));
        assertTrue(e.getMessage().contains("entre 0 y 10"), e.getMessage());
        assertTrue(e.getMessage().contains("42"), e.getMessage());
    }

    @Test
    @DisplayName("Un numero que no cabe en int se reporta en vez de truncarse")
    void oversizedIntegerIsReported() {
        Tokenizer t = new Tokenizer("99999999999");
        assertThrows(InputFormatException.class, () -> t.nextInt("N"));
    }

    @Test
    @DisplayName("Los negativos se leen bien cuando el rango los permite")
    void negativesAreReadWhenAllowed() throws Exception {
        assertEquals(-1000, new Tokenizer("-1000").nextInt("W", -1000, 1000));
    }
}
