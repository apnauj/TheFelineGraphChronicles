package com.eia.feline.ui.screen;

import com.eia.feline.missions.MissionOneSolver;
import com.eia.feline.missions.MissionTwoSolver;
import com.eia.feline.ui.fx.CatArt;
import com.eia.feline.ui.theme.Theme;
import javafx.scene.Node;

import java.util.List;

/**
 * Registro de las cuatro misiones.
 *
 * Es el unico sitio donde hay que tocar algo para conectar las Misiones 3 y 4
 * cuando lleguen: se cambia el solver de marcador de posicion por el real, se
 * pone implemented = true y se enchufa su visualizador. Ni MissionScreen ni
 * MissionSelectScreen cambian.
 */
public final class Missions {

    private Missions() {}

    public static List<MissionDescriptor<?>> all() {
        return List.of(one(), two(), three(), four());
    }

    public static MissionDescriptor<MissionOneSolver.Case> one() {
        return new MissionDescriptor<>(
                1,
                "Rescatando a Nina",
                "Cruzar el campo minado sin pisar una bomba",
                Theme.POLA,
                () -> portrait(Theme.NINA),
                new MissionOneSolver(),
                "BFS y DFS",
                null,                       // el visualizador entra en la fase siguiente
                true);
    }

    public static MissionDescriptor<MissionTwoSolver.Case> two() {
        return new MissionDescriptor<>(
                2,
                "Las cuentas de Claude",
                "La ruta mas barata hasta el mainframe",
                Theme.MINERVA,
                () -> portrait(Theme.MINERVA),
                new MissionTwoSolver(),
                "Dijkstra",
                null,
                true);
    }

    public static MissionDescriptor<Void> three() {
        return new MissionDescriptor<>(
                3,
                "El botin de churun",
                "Maximizar el churun, incluso con pasadizos envenenados",
                Theme.CHURUN,
                () -> portrait(Theme.CHURUN),
                null,
                "Floyd-Warshall y Bellman-Ford",
                null,
                false);
    }

    public static MissionDescriptor<Void> four() {
        return new MissionDescriptor<>(
                4,
                "Reconectando la red",
                "Volver a unir la universidad con el minimo de cable",
                Theme.LIMON,
                () -> CatArt.villain(Theme.LIMON, 104),
                null,
                "Kruskal con union-find",
                null,
                false);
    }

    private static Node portrait(javafx.scene.paint.Color fur) {
        return CatArt.head(fur, 104);
    }
}
