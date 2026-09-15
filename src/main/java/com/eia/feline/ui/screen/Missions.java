package com.eia.feline.ui.screen;

import com.eia.feline.missions.MissionFourSolver;
import com.eia.feline.missions.MissionOneSolver;
import com.eia.feline.missions.MissionThreeSolver;
import com.eia.feline.missions.MissionTwoSolver;
import com.eia.feline.ui.fx.Art;
import com.eia.feline.ui.fx.CatArt;
import com.eia.feline.ui.viz.GraphVisualizer;
import com.eia.feline.ui.viz.MaxWalkVisualizer;
import com.eia.feline.ui.viz.MstVisualizer;
import com.eia.feline.ui.viz.GridVisualizer;
import com.eia.feline.ui.theme.Theme;
import javafx.scene.Node;

import java.util.List;

/**
 * Registro de las cuatro misiones.
 *
 * Es el unico sitio donde hay que tocar algo para conectar la Mision 4 cuando
 * llegue: se cambia el solver de marcador de posicion por el real y se pone
 * implemented = true. Ni MissionScreen ni MissionSelectScreen cambian. La
 * Mision 3 ya sigue este mismo patron con implemented = true.
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
                () -> portrait("nina", Theme.NINA),
                new MissionOneSolver(),
                "BFS y DFS",
                GridVisualizer::new,
                true);
    }

    public static MissionDescriptor<MissionTwoSolver.Case> two() {
        return new MissionDescriptor<>(
                2,
                "Las cuentas de Claude",
                "La ruta mas barata hasta el mainframe",
                Theme.MINERVA,
                () -> portrait("minerva", Theme.MINERVA),
                new MissionTwoSolver(),
                "Dijkstra",
                GraphVisualizer::new,
                true);
    }

    public static MissionDescriptor<MissionThreeSolver.Case> three() {
        return new MissionDescriptor<>(
                3,
                "El botin de churun",
                "Maximizar el churun, incluso con pasadizos envenenados",
                Theme.CHURUN,
                () -> portrait("churun", Theme.CHURUN),
                new MissionThreeSolver(),
                "Floyd-Warshall y Bellman-Ford",
                MaxWalkVisualizer::new,
                true);
    }

    public static MissionDescriptor<MissionFourSolver.Case> four() {
        return new MissionDescriptor<>(
                4,
                "Reconectando la red",
                "Volver a unir la universidad con el minimo de cable",
                // El acento va en azul y no en carmesi porque el arte de Limon es
                // azul y crema. Theme.LIMON sigue siendo el color de "peligro"
                // (bombas, pesos negativos); son dos cosas distintas.
                Theme.CAPE,
                () -> Art.portrait("limon", 150, () -> CatArt.villain(Theme.LIMON, 104)),
                new MissionFourSolver(),
                "Kruskal con union-find",
                MstVisualizer::new,
                false);
    }

    /**
     * Retrato de un personaje: usa su PNG si ya existe y si no dibuja el marcador
     * de posicion. Asi el arte definitivo entra pieza a pieza.
     */
    private static Node portrait(String name, javafx.scene.paint.Color fur) {
        return Art.portrait(name, 150, () -> CatArt.head(fur, 104));
    }
}
