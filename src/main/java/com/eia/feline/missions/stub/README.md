# Andamiaje temporal

> Guia completa de integracion: [`INTEGRACION.md`](../../../../../../../../INTEGRACION.md)
> en la raiz del repositorio.

Este paquete existe SOLO para que la interfaz grafica de las Misiones 3 y 4 se
pueda construir y demostrar mientras otro integrante escribe los algoritmos
definitivos.

Cuando lleguen las implementaciones reales:

1. Entran en `com.eia.feline.algo.maxwalk` (Floyd-Warshall y Bellman-Ford) y en
   `com.eia.feline.algo.mst` (Kruskal y union-find). Esos dos paquetes estan
   vacios a proposito, reservados para ellas: aqui no hay nada que colisione.
2. `MissionThreeSolver` y `MissionFourSolver` cambian las llamadas a
   `ReferenceMaxWalk` / `ReferenceMst` por las clases reales. Es el unico cambio.
3. Este paquete entero se borra.

Ni las pantallas ni los visualizadores cambian: dependen de los payloads
(`MissionThreeSolver.Case`, `MissionFourSolver.Case`), no de quien los calcule.
