# Uso de IA

## 1. Herramientas usadas y en que partes

| Herramienta | Para que                                                                                                       |
|---|----------------------------------------------------------------------------------------------------------------|
| Claude (Claude Code) | Reestructuracion a Maven, interfaz grafica completa (JavaFX), visualizadores, suite de pruebas y documentacion |
La logica original de las Misiones 1 y 2 (BFS, DFS, Dijkstra) se por Juan Pablo Alzate, mientras que la lógica de las misiones 3 y 4 fue escrita por Jerónimo Duque. Este proceso de realizo a concienca por cada integrante al tratarse del core del proyecto y la parte escencial para poder realizar una sustentación y comprender como se construyo sobre eso.

## 2. Prompts decisivos

**a) El encuadre inicial (para la reestructuración a maven y ui).** Se le dio el enunciado completo y se le pidio
explicitamente **planear antes de escribir**: entender que habia que cambiar en la
logica existente para poder conectarle una interfaz, y preguntar lo que hiciera
falta. Fue decisivo porque destapo, antes de escribir una linea de GUI, que los
algoritmos devolvian un `int` y que con un `int` no se puede dibujar nada. La AI nos ayudó a notar pequeños errores de lógica que era importante resolver antes de seguir construyendo sobre lo que ya estaba.

**b) "sin afectar la logica que ya esta implementada".** Esta restriccion obligó a
distinguir entre cambiar un algoritmo y cambiar la estructura de datos que lee. El
cuerpo del BFS, la pila explicita del DFS y la relajacion de Dijkstra quedaron tal
como estaban; lo que cambio fue el contenedor y el valor de retorno.


## 3. Casos en que la salida generada estuvo mal o fue suboptima

### 3.1 Cuatro defectos encontrados al portar la logica existente

Al reestructurar el proyecto aparecieron cuatro defectos en el codigo que ya
estaba, pues este había sido escrito a mano.

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

- **_Juan Pablo Alzate_**: _Este trabajo fue muy valioso para poder rectificar y comprender más a profundidad cada uno de los algoritmos de grafos que se habían discutido en clase. El uso de IA fue extremadamente útil para hacer correcciones importantes que no se habían notado en primera instancia y también para llegar a buenos resultados en la UI. Sin embargo la IA sigue sin poder tener el criterio que si tenemos como seres humanos para decidir como hacer las cosas, a veces hace de más y otras veces se engancha con alguna idea que no es relevante simplemente porque no es capaz de decidir de la misma manera que nosotros lo haríamos. Para poder aprovecharla al máximo es necesario ser muy claro con lo que se requiere y como se requiere de lo contrario nunca se va a conseguir llegar con precisión a lo que se quiere. Es por esto que antes de usar IA lo más importante es comprender nosotros mismos: que se quiere, como se quiere, por que se quiere, para que se quiere y tener claridad conceptual en la implementación, de lo contrario nuestra sesión de IA se convertirá en un desastre y no podremos sacarle el mejor partido._
- **_Jeronimo Duque_**: _Fue un trabajo bastante enriquecedor, principalmente porque los problemas requerían modificar y adaptar algoritmos clásicos, lo que llevó a un entendimiento más profundo de su funcionamiento y evitó que el proceso se limitara a copiar, pegar y adaptar código. Asimismo, considero que el uso de IA puede favorecer la obtención de resultados de mayor calidad cuando se utiliza como una herramienta de apoyo: plantear una lógica inicial propia y utilizarla como tester y consultor permite mantener claridad sobre el objetivo y la funcionalidad de la solución, mientras se identifican errores, casos límite y posibles mejoras. De esta manera, la combinación entre el razonamiento propio y la IA no solo permite llegar a una solución funcional, sino también desarrollar un código más óptimo, robusto y alineado con estándares de calidad más altos._