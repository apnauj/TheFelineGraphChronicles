# The Feline Graph Chronicles

Pola y Minerva contra Limon y Nero: cuatro misiones, cada una un problema de
grafos resuelto con algoritmos implementados desde cero, con una interfaz grafica
en JavaFX que los dibuja y los anima.

Lenguajes y Compiladores — Universidad EIA.

## Integrantes

| Nombre | Rol en la entrega |
|---|---|
| Juan Pablo Alzate | _(completar)_ |
| _(completar)_ | _(completar)_ |
| _(completar)_ | _(completar)_ |

## Como compilar y ejecutar

Requisitos: **JDK 17 o superior** y **Maven**. Nada mas; JavaFX lo baja Maven.

```bash
mvn javafx:run
```

Ese es el unico comando necesario desde un clon limpio. Las pruebas:

```bash
mvn test
```

Cada mision se puede correr tambien sin abrir la ventana, leyendo de la entrada
estandar, que es como se comprueba la salida contra el enunciado:

```bash
mvn -q compile
java -cp target/classes com.eia.feline.missions.MissionOneSolver   < entrada.txt
java -cp target/classes com.eia.feline.missions.MissionTwoSolver   < entrada.txt
java -cp target/classes com.eia.feline.missions.MissionThreeSolver < entrada.txt
java -cp target/classes com.eia.feline.missions.MissionFourSolver  < entrada.txt
```

## Estructura del proyecto

Tres capas, y la flecha de dependencia apunta siempre en el mismo sentido:
`ui → missions → algo`.

```
src/main/java/com/eia/feline/
├── algo/            algoritmos puros. Ni un import de JavaFX, Swing o AWT.
│   ├── graph/       Adjacency (vista CSR) · WeightedGraph · EdgeList
│   ├── grid/        GridGraph — el tablero de la Mision 1 como grafo
│   ├── search/      BFS · DFS · SearchResult
│   ├── sp/          Dijkstra · ShortestPathResult
│   ├── maxwalk/     FloydWarshall · BellmanFord — reales, Mision 3 (ver "Estado")
│   └── mst/         reservado para la Mision 4 (ver "Estado")
├── missions/        texto de entrada -> lineas exactas + payload de dibujo
│   ├── Tokenizer · InputFormatException · MissionSolver · CaseResult
│   ├── MissionOneSolver … MissionFourSolver
│   └── stub/        ReferenceMst: andamiaje TEMPORAL de la Mision 4 (se borra)
└── ui/              lo unico que importa JavaFX
    ├── theme/ screen/ viz/ fx/
```

Los dos contratos que sostienen todo:

- **`MissionSolver<P>`** devuelve un `CaseResult` por caso, con `outputLine` (la
  linea exacta, ya formateada) y `payload` (el estado que el dibujo necesita). La
  GUI concatena la linea y nunca la reinterpreta, asi que el texto que se compara
  automaticamente se genera en un solo sitio.
- **`Visualizer<P>`** dibuja un payload, lo reproduce paso a paso y decide si la
  instancia cabe dentro de los topes de la seccion 2.3.

`MissionScreen` es **una sola clase para las cuatro misiones**, configurada con un
`MissionDescriptor`. Agregar una mision es agregar una entrada a `Missions.all()`.

## Estado de las misiones

| Mision | Algoritmos | Estado |
|---|---|---|
| 1 — Rescatar a Nina | BFS y DFS | terminada |
| 2 — Las cuentas de Claude | Dijkstra | terminada |
| 3 — El botin de churun | Floyd-Warshall y Bellman-Ford | terminada |
| 4 — Reconectar la red | Kruskal con union-find | **andamiaje temporal** |

**Para enchufar la Mision 4 definitiva: ver [`INTEGRACION.md`](INTEGRACION.md).**

La Mision 3 ya tiene sus algoritmos definitivos en `algo/maxwalk/`
(`FloydWarshall`, `BellmanFord`); `MissionThreeSolver` y `ui/viz/MatrixPane` los
usan directamente, y el aviso de andamiaje ya se quito de su pantalla. La
Mision 4 sigue funcionando de punta a punta y produce la salida correcta del
enunciado, pero su algoritmo vive en `missions/stub/ReferenceMst.java` y es
provisional: se escribio para poder construir y demostrar la interfaz mientras
se escribe la version definitiva. La aplicacion todavia lo dice en pantalla
para la Mision 4. El paquete `algo/mst/` esta vacio a proposito, reservado
para Kruskal.

## Recursos de terceros

El enunciado pide declarar cualquier libreria usada solo para dibujar. No se usa
ninguna: el grafo se coloca con un Fruchterman-Reingold escrito a mano y todo se
pinta con JavaFX. Lo unico de terceros son **dos tipografias**, incrustadas en
`src/main/resources/com/eia/feline/ui/fonts/`:

| Fuente | Uso | Licencia |
|---|---|---|
| Bangers | rotulos y titulos | SIL Open Font License 1.1 |
| Fredoka | texto de interfaz | SIL Open Font License 1.1 |

La OFL permite incrustarlas y redistribuirlas. El aviso esta en `fonts/OFL.txt`.

**La musica no es de terceros: la genera el propio proyecto.** El tema en bucle
(`ui/audio/theme.wav`, 9,6 s) lo sintetiza `tools/ThemeSynth.java`, un
generador que escribe el WAV nota a nota -- progresion i-VI-III-VII en re menor,
bajo en onda cuadrada, acordes y melodia con envolvente. Se ejecuta a mano solo
si hay que regenerarlo:

```bash
javac tools/ThemeSynth.java -d /tmp/synth
java -cp /tmp/synth ThemeSynth src/main/resources/com/eia/feline/ui/audio/theme.wav
```

Suena con `javax.sound.sampled` (biblioteca estandar), no con `javafx.media`, para
no anadir otro modulo al pom. Si la maquina no tiene sonido, falla en silencio y
la aplicacion funciona igual. Hay un boton para silenciarla.
Si faltaran, la aplicacion cae a una familia del sistema y sigue funcionando (ver
`ui/theme/Fonts.java`).

Para generar el arte de los personajes: **[`ASSETS.md`](ASSETS.md)** trae el
prompt de cada SVG y cada PNG.

## Decisiones tomadas

**DFS iterativo, con pila explicita.** El enunciado permite cuadriculas de hasta
10^6 celdas; un DFS recursivo abriria hasta un millon de marcos y desbordaria la
pila de la JVM. La pila explicita mueve ese estado al heap. Se marca visitado *al
sacar* y no al empujar, para que el recorrido sea identico al del DFS recursivo
canonico; como consecuencia un nodo puede entrar varias veces a la pila, asi que
la cota es `aristas + 1`.

**El orden `up, down, left, right` no esta en el DFS.** Sale de que `GridGraph`
guarda los vecinos como `right, left, down, up` y la pila es LIFO. Si se toca uno
de los dos archivos hay que tocar el otro; hay pruebas que lo fijan.

**Lista de adyacencia con `List<List<Integer>>`, no una estructura comprimida.**
Se probaron las dos. Medido en el limite del enunciado (1000x1000 = 10^6 celdas),
una representacion CSR con dos arreglos de `int` gasta 20 MB contra 144 MB y hace
el BFS en 36 ms contra 66 ms. Se eligio igual la lista de listas: **144 MB caben
de sobra en un heap por defecto**, asi que no hay nada que se rompa, y a cambio la
estructura es la que cualquiera lee sin explicacion previa y la que el grupo puede
defender linea por linea. La ganancia era real pero no hacia falta.

Lo unico que si cambia: con un heap muy acotado la lista de listas revienta por
debajo de ~192 MB, donde la version CSR seguia funcionando con 48 MB. Si algun dia
hubiera que correr esto con `-Xmx128m`, esa es la decision que habria que revisar.

**`long` para los costos acumulados** de las Misiones 2, 3 y 4. Con 10.000 nodos
y pesos de hasta 1.000.000 una ruta llega al orden de 10^10, que no cabe en `int`
y se desbordaria en silencio. El centinela de "sin ruta" es `Long.MAX_VALUE`
(`Long.MIN_VALUE` en la Mision 3, que maximiza) y nunca entra en una suma.

**Aristas duplicadas: se guardan todas.** Deduplicar obliga a buscar en la lista
de vecinos, y con 100.000 aristas incidentes a un mismo nodo esa busqueda es
cuadratica. El enunciado permite guardarlas todas y Dijkstra se queda con la mas
barata sin ayuda.

**Sin libreria de grafos, tampoco para dibujar.** La colocacion de los nodos es un
Fruchterman-Reingold escrito a mano (`ui/viz/SpringLayout`), unas cincuenta lineas.
Es O(iteraciones · N²), lo cual solo es aceptable porque la seccion 2.3 limita el
dibujo a 60 nodos; por encima de ese tope no se dibuja.

**Se dibuja sobre `Canvas`, no con un nodo por celda.** Una cuadricula de 50x50 son
2.500 celdas y la matriz de la Mision 3 son 10.000 casillas; con un nodo de escena
por casilla la construccion tarda segundos.

**BFS y DFS se dibujan en tableros separados.** La primera version los superponia
sobre la misma cuadricula y las dos capas traslucidas se mezclaban en un color
intermedio que ocultaba cual algoritmo habia llegado a que celda, que es lo unico
que esta mision existe para mostrar.

**El calculo corre fuera del hilo de JavaFX.** Con la cuadricula del limite
resolver toma del orden de un segundo, y hacerlo en el hilo grafico congelaria la
ventana.

## Limitaciones conocidas

- La Mision 4 corre sobre andamiaje temporal (ver arriba).
- El arte de los personajes son figuras primitivas dibujadas en codigo
  (`ui/fx/CatArt`), a la espera del material definitivo. Reemplazarlo no exige
  cambios fuera de esa clase y de `ui/screen/Missions.java`; los prompts para
  generarlo estan en [`ASSETS.md`](ASSETS.md).
- JavaFX **no carga archivos SVG**. Los retratos definitivos hay que exportarlos
  tambien a PNG, o convertir sus trazados a `SVGPath`. Explicado en `ASSETS.md`.
- La primera compilacion necesita conexion a internet para que Maven baje JavaFX
  y JUnit. Despues funciona sin red.
- Por encima de los topes de la seccion 2.3 no hay dibujo. La respuesta numerica
  se sigue mostrando junto a un mensaje que explica por que se omitio.
- La colocacion de nodos es cuadratica en N; con instancias grandes seria lenta,
  pero nunca se ejecuta sobre ellas porque el tope de dibujo lo impide antes.

## Pruebas

```bash
mvn test                                             # las 57
mvn test -Dtest=MissionOneSolverTest                 # una clase
mvn test -Dtest=MissionOneSolverTest#statementSample # un metodo
```

Cada mision tiene una prueba que compara la salida completa contra el ejemplo del
enunciado, caracter por caracter. Ademas hay pruebas de desbordamiento, de
entrada mal formada, de determinismo del orden del DFS, de equivalencia entre la
estructura CSR y la lista de listas original, y `AlgoPurityTest`, que falla la
construccion si alguna vez entra un import de JavaFX, Swing o AWT a `algo/` o a
`missions/`.
