package com.eia.feline.ui.screen;

import com.eia.feline.missions.MissionSolver;
import com.eia.feline.ui.viz.Visualizer;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.function.Supplier;

/**
 * Todo lo que distingue a una mision de otra, reunido en un dato.
 *
 * MissionScreen se construye una sola vez y se configura con esto, de modo que
 * agregar una mision no significa escribir otra pantalla. Cuando lleguen las
 * Misiones 3 y 4 solo hay que agregar dos entradas a Missions.all().
 *
 * @param <P> tipo del payload de visualizacion de esta mision.
 */
public record MissionDescriptor<P>(
        int number,
        String name,
        String tagline,
        Color accent,
        Supplier<Node> portrait,
        MissionSolver<P> solver,
        String algorithms,
        Supplier<Visualizer<P>> visualizer,
        boolean implemented) {

    public String label() {
        return "Mision " + number;
    }
}
