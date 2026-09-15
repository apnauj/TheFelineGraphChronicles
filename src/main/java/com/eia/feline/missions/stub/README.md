# Andamiaje temporal

> Guia completa de integracion: [`INTEGRACION.md`](../../../../../../../../INTEGRACION.md)
> en la raiz del repositorio.

Este paquete existia para que la interfaz grafica de las Misiones 3 y 4 se
pudiera construir y demostrar mientras se escribian los algoritmos definitivos.

**Mision 3 -- terminada, andamiaje retirado.** Floyd-Warshall y Bellman-Ford de
maximizacion viven en `com.eia.feline.algo.maxwalk` (`FloydWarshall`,
`BellmanFord`); `MissionThreeSolver` y `ui/viz/MatrixPane` usan esas clases
reales. `ReferenceMaxWalk.java` ya no tenia ningun uso en el proyecto y se
borro.

**Mision 4 -- pendiente.** `ReferenceMst.java` sigue siendo andamiaje temporal
completo. Cuando llegue Kruskal definitivo:

1. Entra en `com.eia.feline.algo.mst` (Kruskal y union-find). Ese paquete esta
   vacio a proposito, reservado para el: aqui no hay nada que colisione.
2. `MissionFourSolver` cambia la llamada a `ReferenceMst` por la clase real. Es
   el unico cambio.
3. Este paquete entero se borra.

Ni las pantallas ni los visualizadores cambian: dependen de los payloads
(`MissionThreeSolver.Case`, `MissionFourSolver.Case`), no de quien los calcule.
