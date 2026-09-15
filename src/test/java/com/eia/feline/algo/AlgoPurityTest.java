package com.eia.feline.algo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La seccion 7.2 del enunciado exige que los algoritmos vivan en clases
 * independientes de la GUI, sin un solo import de Swing ni de JavaFX, para que se
 * puedan ejecutar desde una prueba sin abrir una ventana.
 *
 * Eso es facil de cumplir el primer dia y facil de romper sin darse cuenta el dia
 * que alguien quiere "solo pintar esto rapido". Esta prueba lo vuelve mecanico:
 * si entra un import prohibido a algo/ o a missions/, la compilacion pasa pero la
 * prueba falla y dice exactamente en que archivo.
 */
class AlgoPurityTest {

    private static final List<String> FORBIDDEN =
            List.of("javafx.", "javax.swing.", "java.awt.");

    private static final List<String> PURE_PACKAGES =
            List.of("src/main/java/com/eia/feline/algo", "src/main/java/com/eia/feline/missions");

    @Test
    @DisplayName("Ni algo/ ni missions/ importan JavaFX, Swing o AWT")
    void algorithmPackagesHaveNoUiImports() throws IOException {
        List<String> offences = new ArrayList<>();

        for (String pkg : PURE_PACKAGES) {
            Path root = Path.of(pkg);
            if (!Files.isDirectory(root)) continue;

            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                    List<String> lines = Files.readAllLines(file);
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i).strip();
                        if (!line.startsWith("import ")) continue;
                        for (String banned : FORBIDDEN) {
                            if (line.contains(banned)) {
                                offences.add(file + ":" + (i + 1) + "  ->  " + line);
                            }
                        }
                    }
                }
            }
        }

        assertTrue(offences.isEmpty(),
                "La capa de algoritmos debe poder correr sin abrir una ventana, pero encontre:\n"
                        + String.join("\n", offences));
    }
}
