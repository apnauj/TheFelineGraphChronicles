package com.eia.feline.ui.viz;

import com.eia.feline.algo.graph.EdgeList;

import java.util.HashMap;
import java.util.Map;

/**
 * Cuando el enunciado permite mas de una arista o cable entre el mismo par de
 * nodos (Misiones 2 y 4 lo permiten explicitamente; en la Mision 3, ademas, un
 * sentido y el opuesto son dos aristas distintas), dibujarlas todas como rectas
 * las deja exactamente una encima de otra y solo se ve la ultima.
 *
 * Esta clase le asigna a cada arista un "carril" dentro de su par -- 0, 1, 2...
 * -- y calcula el punto de control de una curva cuadratica para que cada carril
 * se curve a un lado distinto. Con un solo carril el "control" devuelto es el
 * propio punto medio, que es justamente lo que hace que se dibuje recta: quien
 * llama no necesita una rama aparte para el caso comun de una sola arista.
 *
 * Sin estado propio de ningun caso: no sabe nada de Canvas ni de como pinta cada
 * mision, solo hace la geometria del par. La comparten GraphVisualizer,
 * MaxWalkVisualizer y MstVisualizer.
 */
final class EdgeCurves {

    private EdgeCurves() {}

    /** slots[e] = carril de la arista e dentro de su par; groupSizes[e] = cuantas aristas comparten ese par. */
    record Lanes(int[] slots, int[] groupSizes) {}

    static Lanes computeLanes(EdgeList edges) {
        int m = edges.size();
        int[] slots = new int[m];
        int[] groupSizes = new int[m];

        Map<Long, Integer> countSoFar = new HashMap<>();
        for (int e = 0; e < m; e++) {
            long key = pairKey(edges.from(e), edges.to(e));
            slots[e] = countSoFar.merge(key, 1, Integer::sum) - 1;
        }
        for (int e = 0; e < m; e++) {
            groupSizes[e] = countSoFar.get(pairKey(edges.from(e), edges.to(e)));
        }
        return new Lanes(slots, groupSizes);
    }

    /** Clave canonica de un par no ordenado, empaquetada en un long (igual que en SpringLayout). */
    private static long pairKey(int a, int b) {
        int lo = Math.min(a, b), hi = Math.max(a, b);
        return ((long) lo << 32) | (hi & 0xFFFFFFFFL);
    }

    /**
     * Punto de control de la curva entre (ax,ay) y (bx,by) para el carril que le
     * toco. Con groupSize <= 1 devuelve el punto medio: una curva cuadratica cuyo
     * control es el punto medio de sus dos extremos es, precisamente, la recta
     * entre ellos.
     *
     * La perpendicular se toma SIEMPRE en el sentido canonico -- del nodo de
     * indice menor al mayor -- y solo despues se elige el lado segun el carril:
     * si se tomara en el sentido propio de la arista, una arista a->b y su
     * gemela b->a invertirian el vector Y el lado a la vez, los dos signos se
     * cancelarian, y las dos curvas caerian una encima de la otra otra vez.
     *
     * Carriles alternados y crecientes: 0 -> +1, 1 -> -1, 2 -> +2, 3 -> -2... La
     * magnitud se acota para que un par con muchas aristas repetidas no termine
     * con una curva absurdamente ancha.
     */
    static double[] controlPoint(int a, int b, double ax, double ay, double bx, double by,
                                  int slot, int groupSize, double radius) {
        double mx = (ax + bx) / 2, my = (ay + by) / 2;
        if (groupSize <= 1) return new double[]{ mx, my };

        boolean aIsLower = a < b;
        double lx = aIsLower ? ax : bx, ly = aIsLower ? ay : by;
        double hx = aIsLower ? bx : ax, hy = aIsLower ? by : ay;
        double dx = hx - lx, dy = hy - ly;
        double len = Math.max(1e-6, Math.hypot(dx, dy));
        double px = -dy / len, py = dx / len;

        int magnitude = Math.min(6, slot / 2 + 1);
        double sign = (slot % 2 == 0) ? 1 : -1;
        double offset = sign * magnitude * (radius * 1.6 + 18);

        return new double[]{ mx + px * offset, my + py * offset };
    }

    /**
     * Punto real de la curva a mitad de camino (t=0.5 de la cuadratica):
     * B(0.5) = mid + 0.5*(control - mid). Con groupSize <= 1, control == mid y
     * esto devuelve el punto medio de siempre.
     */
    static double[] midpoint(double ax, double ay, double bx, double by, double[] control) {
        double mx = (ax + bx) / 2, my = (ay + by) / 2;
        return new double[]{ mx + 0.5 * (control[0] - mx), my + 0.5 * (control[1] - my) };
    }
}
