package com.eia.feline.ui.fx;

import javafx.scene.control.Button;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * El tema del comic, en bucle.
 *
 * Suena con javax.sound.sampled y no con el reproductor de JavaFX a proposito:
 * javafx.scene.media es un modulo aparte que habria que anadir al pom, y para
 * reproducir un WAV corto en bucle un Clip de la biblioteca estandar basta y
 * sobra. Menos dependencias que declarar en el README.
 *
 * La musica es un archivo SINTETIZADO por el propio proyecto, no una pista de
 * terceros: no hay licencia que declarar ni permiso que pedir.
 *
 * Todo lo de aqui falla en silencio a proposito. Una maquina sin tarjeta de
 * sonido, un servidor de audio ocupado o un formato no soportado no pueden
 * impedir que la aplicacion arranque: como mucho, se queda callada.
 */
public final class Music {

    private static final String RESOURCE = "/com/eia/feline/ui/audio/theme.wav";
    /** Volumen de fondo. La musica acompana; no puede tapar a quien esta explicando. */
    private static final float GAIN_DB = -14f;

    private static Clip clip;
    private static boolean muted;
    private static boolean tried;

    private Music() {}

    /** Arranca el bucle. Llamarlo mas de una vez no hace nada. */
    public static void start() {
        if (tried) {
            resumeIfNeeded();
            return;
        }
        tried = true;
        try (InputStream raw = Music.class.getResourceAsStream(RESOURCE)) {
            if (raw == null) return;

            // El Clip necesita poder rebobinar el flujo, asi que se lee entero a
            // memoria: son 400 KB y evita depender de que el recurso sea reposicionable.
            byte[] bytes = raw.readAllBytes();
            try (AudioInputStream in = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(new ByteArrayInputStream(bytes)))) {
                AudioFormat format = in.getFormat();
                Clip c = AudioSystem.getClip();
                c.open(in);
                applyGain(c);
                c.loop(Clip.LOOP_CONTINUOUSLY);
                clip = c;
            }
        } catch (Exception | Error ignored) {
            // Sin sonido la aplicacion funciona igual.
            clip = null;
        }
    }

    private static void applyGain(Clip c) {
        try {
            if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), GAIN_DB)));
            }
        } catch (Exception ignored) {
            // El volumen por defecto tambien sirve.
        }
    }

    private static void resumeIfNeeded() {
        if (clip != null && !muted && !clip.isRunning()) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void setMuted(boolean value) {
        muted = value;
        if (clip == null) return;
        try {
            if (muted) clip.stop();
            else clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception ignored) {
            // Nada que hacer: se queda como este.
        }
    }

    public static boolean isMuted() { return muted; }

    /** Se para del todo al cerrar. */
    public static void dispose() {
        if (clip != null) {
            try { clip.stop(); clip.close(); } catch (Exception ignored) { }
            clip = null;
        }
    }

    /**
     * Boton de silencio, listo para poner en cualquier cabecera. Cada pantalla
     * crea el suyo y todos leen el mismo estado.
     */
    public static Button toggleButton() {
        Button b = new Button();
        b.getStyleClass().addAll("button-ghost", "icon-button");
        b.setFocusTraversable(false);
        updateLabel(b);
        b.setOnAction(e -> {
            setMuted(!isMuted());
            updateLabel(b);
        });
        return b;
    }

    private static void updateLabel(Button b) {
        b.setText(muted ? "MUSICA OFF" : "MUSICA ON");
    }
}
