import javax.sound.sampled.*;
import java.io.*;

/**
 * Generador del tema musical. NO forma parte de la aplicacion: se ejecuta a mano
 * cuando hay que regenerar el WAV.
 *
 *   javac tools/ThemeSynth.java -d /tmp/synth
 *   java -cp /tmp/synth ThemeSynth src/main/resources/com/eia/feline/ui/audio/theme.wav
 *
 * Sintetiza el tema del comic como WAV. Se ejecuta UNA vez y el resultado se
 * guarda en recursos; asi no hay que meter musica de terceros ni preocuparse por
 * licencias, y el archivo pesa lo que pesa un PNG mediano.
 *
 * Progresion en re menor: i - VI - III - VII (Dm - Bb - F - C), que es la de casi
 * todo lo que suena epico. Bajo en onda cuadrada, acordes suaves y una melodia
 * heroica encima, con envolvente para que no chasquee.
 */
public class ThemeSynth {

    static final float RATE = 22050;
    static final double BPM = 100;
    static final double BEAT = 60.0 / BPM;
    static final double BAR = BEAT * 4;

    // Frecuencias (A4 = 440)
    static double n(String name) {
        String[] names = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
        String letter = name.substring(0, name.length() - 1);
        int octave = Integer.parseInt(name.substring(name.length() - 1));
        int idx = java.util.Arrays.asList(names).indexOf(letter);
        int semis = (octave - 4) * 12 + (idx - 9);
        return 440.0 * Math.pow(2, semis / 12.0);
    }

    static double[] buf;

    /** Onda cuadrada suavizada: cuerpo de chiptune sin el filo que molesta. */
    static double square(double phase) {
        double s = Math.sin(phase);
        return Math.tanh(s * 3.2) * 0.62;
    }

    static double triangle(double phase) {
        return Math.asin(Math.sin(phase)) * (2 / Math.PI);
    }

    /** Envolvente ataque-caida-sostenido-relajacion. */
    static double env(double t, double dur, double a, double d, double s, double r) {
        if (t < a) return t / a;
        if (t < a + d) return 1 - (1 - s) * (t - a) / d;
        if (t < dur - r) return s;
        if (t < dur) return s * (dur - t) / r;
        return 0;
    }

    static void note(double startSec, double durSec, double freq, double gain, int shape) {
        int from = (int) (startSec * RATE);
        int len = (int) (durSec * RATE);
        for (int i = 0; i < len; i++) {
            int at = from + i;
            if (at < 0 || at >= buf.length) continue;
            double t = i / RATE;
            double phase = 2 * Math.PI * freq * t;
            double v = switch (shape) {
                case 0 -> square(phase);
                case 1 -> triangle(phase);
                // Voz de acorde: dos osciladores ligeramente desafinados, que es lo
                // que hace que un acorde suene ancho y no plano.
                default -> (Math.sin(phase) + Math.sin(phase * 1.005)) * 0.5;
            };
            buf[at] += v * gain * env(t, durSec, 0.012, 0.06, 0.72, 0.09);
        }
    }

    static void chord(double start, double dur, String[] notes, double gain) {
        for (String nn : notes) note(start, dur, n(nn), gain, 2);
    }

    public static void main(String[] args) throws Exception {
        double loop = BAR * 4;                    // cuatro compases
        buf = new double[(int) (loop * RATE)];

        String[][] chords = {
            {"D4","F4","A4"},      // Dm
            {"D4","F4","A#3"},     // Bb
            {"C4","F4","A4"},      // F
            {"C4","E4","G4"},      // C
        };
        String[] bass = {"D2","A#1","F2","C2"};

        // Melodia heroica, un motivo por compas (nota, compas, tiempo, duracion)
        Object[][] melody = {
            {"D5",0,0.0,0.75},{"F5",0,0.75,0.25},{"A5",0,1.0,1.0},{"G5",0,2.0,0.5},{"F5",0,2.5,1.5},
            {"D5",1,0.0,0.5},{"F5",1,0.5,0.5},{"A#4",1,1.0,1.0},{"D5",1,2.0,2.0},
            {"C5",2,0.0,0.75},{"F5",2,0.75,0.25},{"A5",2,1.0,1.0},{"C6",2,2.0,2.0},
            {"A#5",3,0.0,0.5},{"A5",3,0.5,0.5},{"G5",3,1.0,1.0},{"A5",3,2.0,2.0},
        };

        for (int bar = 0; bar < 4; bar++) {
            double t0 = bar * BAR;
            chord(t0, BAR * 0.96, chords[bar], 0.085);

            // Bajo: corcheas marcando, que es lo que empuja
            for (int b = 0; b < 8; b++) {
                note(t0 + b * BEAT / 2, BEAT * 0.42, n(bass[bar]), 0.30, 0);
            }
            // Octava del bajo en los tiempos fuertes
            note(t0, BEAT * 0.9, n(bass[bar]) * 2, 0.12, 1);
            note(t0 + BEAT * 2, BEAT * 0.9, n(bass[bar]) * 2, 0.12, 1);
        }

        for (Object[] m : melody) {
            double start = (int) m[1] * BAR + (double) m[2] * BEAT;
            note(start, (double) m[3] * BEAT * 0.94, n((String) m[0]), 0.17, 0);
        }

        // Normalizado suave y limitador, para que no sature en los picos
        double peak = 0;
        for (double v : buf) peak = Math.max(peak, Math.abs(v));
        double norm = 0.82 / Math.max(1e-9, peak);

        byte[] out = new byte[buf.length * 2];
        for (int i = 0; i < buf.length; i++) {
            double v = Math.tanh(buf[i] * norm);
            short s = (short) (v * 32000);
            out[i * 2] = (byte) (s & 0xFF);
            out[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }

        AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
        try (AudioInputStream in = new AudioInputStream(
                new ByteArrayInputStream(out), fmt, buf.length)) {
            AudioSystem.write(in, AudioFileFormat.Type.WAVE, new File(args[0]));
        }
        System.out.printf("escrito %s  %.1f s  %.0f KB%n",
                args[0], loop, new File(args[0]).length() / 1024.0);
    }
}
