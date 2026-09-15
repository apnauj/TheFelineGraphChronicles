# Como enchufar la Mision 4

Guia para quien esta escribiendo Kruskal. Floyd-Warshall y Bellman-Ford (Mision
3) ya estan integrados en `algo/maxwalk/`; lo que queda de esta guia es Kruskal
+ union-find para `algo/mst/`.

**Resumen en una linea:** las pantallas, los dibujos y las animaciones de las
cuatro misiones ya estan hechos y funcionando. Lo unico que falta es el
algoritmo definitivo de la Mision 4. Para enchufarlo hay que tocar **un
archivo**, y cuando tambien se migre `ui/viz/MatrixPane` (fuera del alcance de
esta guia) se puede borrar una carpeta. Ni la interfaz ni los visualizadores
cambian.

---

## 1. Por que hay codigo que ya funciona si falta la mitad

Para poder construir la interfaz habia que tener con que dibujar, asi que las
Misiones 3 y 4 corrieron sobre **andamiaje provisional** en
`src/main/java/com/eia/feline/missions/stub/`:

| Archivo | Que tenia | Estado |
|---|---|---|
| `ReferenceMaxWalk.java` | Floyd-Warshall y Bellman-Ford de maximizacion | **ya integrado**: `MissionThreeSolver` usa `algo/maxwalk/FloydWarshall` y `algo/maxwalk/BellmanFord`. El archivo sigue aqui solo porque `ui/viz/MatrixPane` todavia lee su constante `NONE`; se borra cuando eso se migre. |
| `ReferenceMst.java` | Kruskal + union-find | **pendiente**: lo reemplaza `algo/mst/` |

El codigo de `ReferenceMst` da la salida correcta del enunciado, **pero no es la
entrega**. La aplicacion lo sigue diciendo en pantalla para la Mision 4 (un
aviso azul y la etiqueta `ANDAMIAJE` en la tarjeta); la Mision 3 ya tiene su
algoritmo real, aunque ese aviso todavia no se ha quitado de la pantalla
(queda para la pasada de interfaz, que es aparte).

El paquete `algo/mst/` esta **vacio a proposito**, reservado para el codigo
definitivo de Kruskal. `algo/maxwalk/` ya no esta vacio: ahi viven
`FloydWarshall.java`, `BellmanFord.java` y sus records de resultado
(`AllPairsResult`, `MaxWalkResult`).

---

## 2. Lo unico que hay que respetar: el payload

La pantalla no sabe nada de algoritmos. Solo sabe leer dos cosas que le devuelve
el solver:

```java
public record CaseResult<P>(int index, String outputLine, P payload) {}
```

- `outputLine` es **la linea exacta**, ya formateada (`"Case #1: 110"`). La GUI la
  concatena y no la vuelve a tocar.
- `payload` es el estado estructurado que el dibujo necesita.

Mientras el payload se llene con los mismos campos, **el dibujo sigue
funcionando sin cambiarle una linea**. Los campos son estos:

### Mision 3 — `MissionThreeSolver.Case`

```java
int nodes                // N
EdgeList edges           // los pasadizos, DIRIGIDOS, en el orden en que se leyeron
int start, destination   // S y D
Outcome outcome          // BLOCKED | INFINITE | VALUE  (en ese orden de precedencia)
long churun              // la respuesta, solo valida si outcome == VALUE
long[][] matrix          // Floyd-Warshall: churun maximo entre todos los pares
boolean[][] unbounded    // matrix[i][j] no acotado -> se pinta "inf"
int[] route              // ruta que alcanza el maximo (vacio si no aplica)
int[] cycle              // ciclo positivo culpable (vacio si no aplica)
String mismatch          // null si los dos algoritmos coinciden; si no, en que discrepan
```

Quien dibuja que:
- `matrix` + `unbounded` -> la tabla N x N con scroll (`-` sin ruta, `inf` no acotado)
- `route` -> se resalta en dorado cuando `outcome == VALUE`
- `cycle` -> se resalta **en vez** de una ruta cuando `outcome == INFINITE`
- `mismatch` -> si no es null, sale un banner rojo

### Mision 4 — `MissionFourSolver.Case`

```java
int nodes                // N
EdgeList cables          // todos los cables, en el orden en que se leyeron
int[] order              // indices de los cables EN EL ORDEN EN QUE KRUSKAL LOS MIRA
boolean[] accepted       // accepted[i] = si el cable order[i] entro al arbol
long total               // costo del MST, valido solo si connected
boolean connected
int components           // cuantas partes sueltas quedaron
```

`order` + `accepted` son los que mueven la animacion: la cola de la derecha se
recorre paso a paso y cada cable se pone dorado (aceptado) o tachado (cerraba
ciclo). **Por eso `order` guarda indices y no aristas**: el dibujo necesita que
las aristas se queden en su posicion original.

---

## 3. El cambio, paso a paso

**Mision 3 -- ya hecho, como referencia del patron a seguir:**

```java
// antes, en MissionThreeSolver.solve()
ReferenceMaxWalk.AllPairs fw = ReferenceMaxWalk.floydWarshall(nodes, edges);
ReferenceMaxWalk.SingleSource bf = ReferenceMaxWalk.bellmanFord(nodes, edges, start);

// ahora
AllPairsResult fw = FloydWarshall.run(nodes, edges);
MaxWalkResult bf = BellmanFord.run(nodes, edges, start);
```

**Mision 4 -- lo que falta:**

1. Escribir la clase definitiva en `algo/mst/` (Kruskal + union-find con
   compresion de caminos y union por tamano/rango).

2. En `MissionFourSolver.solve()`, cambiar esta linea:

   ```java
   ReferenceMst.Result mst = ReferenceMst.kruskal(nodes, cables);
   ```

   por la llamada a la clase real de `algo/mst/`.

3. `mvn test`. Las pruebas de la Mision 4 ya existen y ya comparan contra los
   ejemplos del enunciado: si pasan, quedo.

4. Una vez que tambien `ui/viz/MatrixPane` deje de usar `ReferenceMaxWalk.NONE`
   (pasada de interfaz, fuera del alcance de esta guia), se puede borrar
   `src/main/java/com/eia/feline/missions/stub/` entero.

5. En `ui/screen/Missions.java`, poner `implemented = true` en `three()` y
   `four()` (es el ultimo parametro). Eso quita el aviso y cambia la etiqueta de
   `ANDAMIAJE` a `LISTA`. Es un paso de interfaz, no de algoritmo.

**No hay paso mas alla de estos.** No hay que tocar ninguna pantalla ni ningun
visualizador.

---

## 4. Trampas del enunciado (tres ya resueltas, una todavia pendiente)

Vale la pena leerlas: son las que cuestan puntos, y las que probablemente
pregunten en la defensa oral.

**Mision 3 -- ya resueltas; ver el codigo real en
`algo/maxwalk/FloydWarshall.java` y `BellmanFord.java`:**

**a) El orden de precedencia de la Mision 3 es estricto.** Primero se pregunta si
D es alcanzable (`Limon blocked the way`), despues si hay churun infinito
(`Infinite churun!`), y solo al final se da el numero. Un grafo con ciclo positivo
**y** con D inalcanzable imprime `Limon blocked the way`, no `Infinite churun!`.

**b) Un ciclo positivo que no puede llegar a D no sirve de nada.** La marca de "no
acotado" hay que **propagarla a todo lo alcanzable** desde los nodos que siguen
mejorando, y D es infinito **solo si queda marcado**. Los dos algoritmos tienen
que aplicar el mismo criterio o la comprobacion cruzada falla en todo grafo que
tenga un ciclo positivo que no alcanza a D. Hay una prueba para este caso
(`positiveCycleThatCannotReachDestinationIsIgnored`).

**c) Floyd-Warshall de maximizacion se desborda.** Con un ciclo positivo el triple
bucle **duplica el valor en cada `k`**: con los 100 nodos que permite el enunciado
eso revienta el `long` antes de terminar. Hay que acotar el valor. Cualquier
respuesta legitima vale a lo sumo `(N-1) * 1000 = 99.000`, asi que recortar en
`10^9` no se puede confundir nunca con un resultado real.

Ademas hace falta la **pasada extra** despues del triple bucle: `(i, j)` es no
acotado si y solo si existe un `k` con `d[i][k]` finito, `d[k][k] > 0` y `d[k][j]`
finito. Sin esa pasada la matriz guarda numeros grandes sin sentido en vez de
infinitos.

**Mision 4 -- todavia pendiente:**

**d) La Mision 4 numera las intersecciones de 1 a N**, no de 0 a N-1 como las
Misiones 2 y 3. La conversion ya se hace en un solo sitio al leer la entrada
(`MissionFourSolver`, no `algo/mst/`): a Kruskal le llegan indices en 0..N-1.

---

## 5. Cosas del resto del proyecto que conviene no romper

- **`algo/` y `missions/` no pueden importar JavaFX, Swing ni AWT.** Lo exige la
  seccion 7.2 del enunciado y hay una prueba (`AlgoPurityTest`) que falla la
  construccion si alguna vez entra uno.
- **Las lineas de salida se comparan caracter por caracter.** ASCII plano, sin
  tildes, sin emojis, sin punto final.
- **`long` para los acumulados** de las Misiones 2, 3 y 4, y nunca aritmetica
  sobre el centinela de "sin ruta".
- **No convertir la lista de adyacencia en arreglos planos.** Se probo, se midio
  (20 MB contra 144 MB, BFS de 36 ms contra 66 ms) y se revirtio a proposito: los
  144 MB caben de sobra y la lista de listas es la que se puede defender leyendola.
  Esta explicado en `README.md` y en `CLAUDE.md`.
- **El orden `up, down, left, right` del DFS no esta en `DFS.java`.** Sale de que
  `GridGraph` guarda los vecinos como `right, left, down, up` y la pila es LIFO.
  Si se toca uno hay que tocar el otro; hay pruebas que lo fijan.

---

## 6. Para empezar

```bash
git fetch origin
git checkout develop && git pull
mvn javafx:run     # ver las cuatro misiones funcionando
mvn test           # 58 pruebas
```

Y para probar una mision sin abrir la ventana:

```bash
mvn -q compile
java -cp target/classes com.eia.feline.missions.MissionFourSolver < entrada.txt
```
