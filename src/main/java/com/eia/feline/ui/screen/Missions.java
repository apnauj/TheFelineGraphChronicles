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
                // Todavia no hay arte de Nina: manda la heroina de la mision.
                () -> portrait("pola", Theme.POLA),
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
                () -> portrait("nero", Theme.CHURUN),
                new MissionThreeSolver(),
                "Floyd-Warshall y Bellman-Ford",
                MaxWalkVisualizer::new,
                false);
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
                () -> portrait("limon", Theme.LIMON),
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
        // El villano cae a una silueta distinta si su PNG no esta.
        boolean villain = name.equals("limon") || name.equals("nero");
        return Art.portrait(name, 150,
                () -> villain ? CatArt.villain(fur, 104) : CatArt.head(fur, 104));
    }
}
