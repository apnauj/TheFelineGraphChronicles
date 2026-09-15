# Uso de IA

> **Pendiente de completar por el grupo.** Las secciones 1 y 3 estan escritas con
> lo que efectivamente paso y son verificables en el historial de git. Las
> secciones marcadas con `_(completar)_` las tiene que llenar cada integrante con
> lo suyo: el enunciado advierte que un informe que no coincida con la defensa se
> califica como deshonestidad academica, asi que **no dejen aqui nada que no
> puedan sostener delante del profesor**.

## 1. Herramientas usadas y en que partes

| Herramienta | Para que |
|---|---|
| Claude (Claude Code) | Reestructuracion a Maven, interfaz grafica completa (JavaFX), visualizadores, suite de pruebas, andamiaje temporal de las Misiones 3 y 4, documentacion |
| _(completar)_ | _(completar: quien uso que para la logica original de las Misiones 1 y 2)_ |

La logica original de las Misiones 1 y 2 (BFS, DFS, Dijkstra) se escribio antes de
esta sesion; **el grupo debe declarar aqui como se escribio**. Todo lo demas que
hay en el repositorio a partir del commit `c1513e6` se produjo asistido por Claude
Code y esta en el historial, commit por commit.

## 2. Prompts decisivos

**a) El encuadre inicial.** Se le dio el enunciado completo y se le pidio
explicitamente **planear antes de escribir**: entender que habia que cambiar en la
logica existente para poder conectarle una interfaz, y preguntar lo que hiciera
falta. Fue decisivo porque destapo, antes de escribir una linea de GUI, que los
algoritmos devolvian un `int` y que con un `int` no se puede dibujar nada.

**b) "sin afectar la logica que ya esta implementada".** Esta restriccion obligo a
distinguir entre cambiar un algoritmo y cambiar la estructura de datos que lee. El
cuerpo del BFS, la pila explicita del DFS y la relajacion de Dijkstra quedaron tal
como estaban; lo que cambio fue el contenedor y el valor de retorno.

**c) _(completar)_** — un tercer prompt propio del grupo, con la razon por la que
hizo falta.

## 3. Casos en que la salida generada estuvo mal o fue suboptima

### 3.1 Cuatro defectos encontrados al portar la logica existente

Al reestructurar el proyecto aparecieron cuatro defectos en el codigo que ya
estaba. **El grupo debe verificar y declarar si ese codigo se escribio con ayuda
de IA o a mano**, porque de eso depende si este apartado cuenta como "salida
generada equivocada" o simplemente como un error propio encontrado despues.

1. **La Mision 1 imprimia los signos `<` y `>`.** El enunciado escribe
   `Case #k: BFS <b> DFS <d>`, donde `<b>` es un marcador de posicion. El codigo
   los tomaba como texto literal y producia `Case #1: BFS <18> DFS <32>`, que
   **fallaba la comparacion automatica** aunque los numeros fueran correctos.
   Arreglo: quitar los signos de la cadena de formato. Hay una prueba que ahora lo
   fija (`outputHasNoAngleBrackets`).

2. **Dijkstra acumulaba en `int`.** Con los limites del enunciado (10.000 nodos,
   pesos de hasta 1.000.000) una ruta llega al orden de 10^10, que no cabe en
   `int`: el resultado se desbordaba en silencio y salia un numero equivocado pero
   verosimil, que es la peor clase de error. La seccion 2.1 pide `long`
   explicitamente. Arreglo: `long[]` con `Long.MAX_VALUE` de centinela.

3. **La deduplicacion de aristas era cuadratica.** `addConnection` recorria la
   lista de vecinos para quedarse con la mas barata. Con 100.000 aristas incidentes
   a un mismo nodo son del orden de 5·10^9 comparaciones. La seccion 2.2 permite
   guardarlas todas; Dijkstra se queda con la mejor sin ayuda.

4. **Una optimizacion innecesaria, justificada con una cifra inventada.** La IA
   reemplazo la `List<List<Integer>>` de la cuadricula por una estructura
   comprimida (CSR, dos arreglos de `int`) argumentando que la version original
   gastaba "del orden de 200 MB, suficiente para agotar el heap por defecto".

   Las dos mitades de esa frase eran falsas, y la cifra venia de estimar tamanos
   de objeto de cabeza en vez de medirlos. Al escribir un banco de pruebas y
   medirlo de verdad: **144 MB, no 200**, y el heap por defecto son gigabytes,
   asi que no agotaba nada. La lista de listas solo falla por debajo de ~192 MB
   de heap acotado.

   Con los numeros reales encima, el grupo decidio **volver a la lista de
   listas**: el CSR era mas rapido (BFS de 36 ms contra 66 ms) y mas compacto
   (20 MB contra 144 MB), pero ganaba algo que el problema no necesitaba a cambio
   de una estructura mas dificil de leer y de defender. La optimizacion estaba
   bien hecha; simplemente no hacia falta.

   Dos lecciones, y la segunda importa mas que la primera: una justificacion
   plausible que nadie verifico es exactamente lo que el enunciado castiga en la
   defensa; y que una optimizacion sea correcta no quiere decir que valga la pena.

### 3.2 La primera version del dibujo de la Mision 1 era inservible

Se dibujaron los recorridos de BFS y DFS superpuestos sobre la misma cuadricula,
cada uno con su color y transparencia. Parecia lo obvio y era lo peor posible: dos
capas traslucidas sobre las mismas celdas se mezclan en un color intermedio, y
dejaba de verse cual algoritmo habia llegado a que celda — que es exactamente lo
unico que esa mision existe para mostrar. Se detecto **mirando la imagen
renderizada**, no leyendo el codigo. Arreglo: dos tableros lado a lado, como los
presenta el propio enunciado.

### 3.3 Dos valores esperados inventados en las pruebas

Al escribir las primeras pruebas de la Mision 1 se pusieron como esperados dos
resultados de DFS que **no se habian calculado, se habian supuesto**
(`BFS 4 DFS 4` en una cuadricula 3x3 y `DFS 1998` en una de 1000x1000). Los dos
estaban mal: el DFS no es optimo, asi que no tiene por que coincidir con el BFS. Se
corrigieron antes de ejecutarlas — uno trazando el recorrido a mano y el otro
cambiando la prueba para que afirme lo que si es demostrable (`dfs >= bfs`) en vez
de un numero adivinado. Es la trampa tipica: una prueba con un valor esperado
inventado no comprueba nada, solo fija el error.

### 3.4 Desbordamiento en Floyd-Warshall de maximizacion

La primera version del andamiaje de la Mision 3 no acotaba los valores. Con un
ciclo positivo, el triple bucle de maximizacion **duplica el valor en cada `k`**, y
con los 100 nodos que permite el enunciado eso desborda `long` mucho antes de
terminar. Arreglo: recortar a una cota muy por encima de cualquier respuesta
legitima (un paseo sin ciclos positivos vale a lo sumo 99.000), de modo que
recortar nunca pueda confundirse con un resultado real.

## 4. Que aprendio cada integrante

_(completar: uno o dos parrafos por persona, de cosas que realmente no sabian
antes. Ejemplos de temas que salieron en esta entrega y dan para eso: por que
Dijkstra necesita pesos no negativos y que se rompe exactamente si se le mete uno
negativo; por que la deteccion de ciclos positivos hay que propagarla a todos los
nodos alcanzables y no solo marcar el ciclo; por que un DFS recursivo no sirve a
10^6 celdas; que significa "CSR" y por que un arreglo de primitivos gana tanto
sobre una lista de listas; por que el union-find con compresion de caminos y union
por tamano responde en tiempo practicamente constante.)_

- **_(nombre)_**: _(completar)_
- **_(nombre)_**: _(completar)_
- **_(nombre)_**: _(completar)_
