# Guia de arte: prompts para generar los SVG y los PNG

Como usarla: a cada prompt de personaje se le adjunta **la foto real del gato** y
se antepone el **preambulo de estilo** de la seccion 1. El preambulo es lo que
hace que las diez piezas parezcan del mismo comic y no de diez comics distintos.

Todo lo que se genere va a `src/main/resources/com/eia/feline/ui/art/`.

---

## 1. Preambulo de estilo (va DELANTE de todos los prompts)

Copiar tal cual, en ingles, que es donde los generadores responden mejor:

> Golden-age American superhero comic book art, 1960s Ben-Day printing look.
> Heavy uniform black ink outlines (4-6px at 1000px wide), completely flat
> saturated fills with NO gradients, NO soft shading, NO airbrush, NO 3D
> rendering, NO photorealism. Cel-shaded at most: one flat darker tone for
> shadow, hard-edged. Bold simplified shapes, confident brush line that varies
> slightly in weight. Clean vector-ready silhouette. Pure transparent
> background. Full body inside frame, nothing cropped. Front-facing even
> lighting, no cast shadow on the ground.
>
> Palette, use ONLY these: ink #16131A, paper #F4ECD8, orange #FF6B1A,
> cyan #00A6C7, crimson #D81B4A, gold #FFC42E, pink #FF6FA8, blue #2B5FD9.

Y para los personajes, ademas:

> Use the attached photo ONLY as reference for the cat's fur pattern, markings,
> colour distribution and face proportions. Do NOT copy the photo's lighting,
> background or realism. Redraw the cat completely as a flat comic character.

**Un consejo que ahorra rehacer todo:** genera primero a Pola, dala por buena, y
usa esa imagen como referencia de estilo para las demas. Adjuntar la foto del
gato mas la lamina de Pola ya aprobada mantiene el trazo coherente.

---

## 2. Personajes (SVG)

Destino: `art/svg/`. Lienzo cuadrado 1000 x 1000, personaje centrado ocupando
~85 % de la altura, fondo transparente.

### `pola.svg` — la heroina
> Full-body heroic cat character named Pola, standing in a confident superhero
> pose: chest out, front paws on hips, chin slightly raised, tail curled up
> behind. A ROUND, CHUBBY, well-fed orange cat -- soft belly, thick cheeks --
> but drawn as powerful and proud, never as a joke. Bright orange fur #FF6B1A
> with a small cream chest patch #F4ECD8. Big round confident eyes with a white
> highlight. Tiny pink nose #FF6FA8. Wearing a short blue superhero cape
> #2B5FD9 clasped at the neck. Determined half-smile.

### `minerva.svg` — la estratega
> Full-body cat character named Minerva, the brilliant strategist sidekick.
> Slim and alert, sitting upright with one paw raised mid-explanation as if
> pointing at an invisible diagram. Cyan-grey fur #00A6C7. Round oversized
> glasses with black frames. Sharp intelligent narrow eyes. Small blue scarf
> #2B5FD9. Confident, slightly smug expression.

### `nina.svg` — la secuestrada
> Full-body elegant cat character named Nina, beautiful and poised, sitting
> with tail wrapped neatly around her paws. Soft pink-cream fur #FF6FA8 with
> white chest and paws #F4ECD8. Large gentle eyes. A thin gold collar #FFC42E
> with a small tag. Calm, dignified expression -- captive but not frightened.

### `nina_captive.svg` — variante para el destino del mapa
> Same character as nina.svg, same style and colours, now inside a cage: heavy
> black iron bars in front of her, paws resting on a lower bar, looking out
> hopefully. Cage bars in ink #16131A. Composition still square, cage fills the
> frame.

### `limon.svg` — el jefe
**YA GENERADO** y en el proyecto: `art/png/limon.png`. Salio **azul y crema con
ojos cian**, no carmesi como pedia el prompt de abajo. Como el resultado es bueno
y encaja con el tema, se quedo, y el acento de la vineta de la Mision 4 se cambio
a azul para que combine. Dos consecuencias:

- `Theme.LIMON` (carmesi) sigue siendo el color de **peligro** -- bombas y pesos
  negativos -- y ya no el color del personaje. Son dos cosas distintas.
- Si se regeneran los demas villanos, conviene decidir: o se rehace Limon en
  carmesi, o se cambia el prompt de Nero para que acompane al azul.

> Full-body villain cat character named Limon, the ruthless leader. Large,
> broad-shouldered, looming forward menacingly with one paw extended in a
> grasping gesture. Deep crimson fur #D81B4A with darker markings. Narrow evil
> half-closed eyes with heavy angled brows. Wicked fanged grin. Tiny black
> villain cape with a high collar. Radiating menace.

### `nero.svg` — el secuaz
> Full-body villain cat character named Nero, the cunning henchman. Smaller and
> wirier than Limon, crouching low and sneaking on tiptoes, looking back over
> his shoulder with a sly smirk. Dark grey-purple fur #4A4157. Long thin tail
> raised in a question-mark curve. One eyebrow raised, scheming expression.

---

## 3. Iconos (SVG)

Destino: `art/svg/icons/`. Lienzo 256 x 256, forma centrada, silueta simple y
legible **a 24 px** -- ese es el filtro: si no se entiende diminuto, no sirve.

Prompt comun, cambiando solo el objeto:

> Single centred comic-book icon of {OBJETO}. Heavy black ink outline, flat
> saturated fill, no gradient, no text, no background. Bold simple silhouette
> that stays readable when scaled down to 24 pixels. Slight hand-drawn wobble
> in the outline.

| Archivo | {OBJETO} |
|---|---|
| `bomb.svg` | a round cartoon bomb, crimson #D81B4A body, lit gold fuse #FFC42E with a spark |
| `paw.svg` | a cat paw print, four toes and a pad, orange #FF6B1A |
| `churun.svg` | a cat treat tube, gold #FFC42E wrapper, one end twisted open |
| `cable.svg` | a coiled electrical cable with a plug on each end, blue #2B5FD9 |
| `node.svg` | a network node: a filled circle with three short lines radiating out, cyan #00A6C7 |
| `lock.svg` | a heavy padlock, closed shackle, crimson #D81B4A |
| `claude-account.svg` | a stylised ID badge with a cat silhouette on it and a lanyard hole, gold #FFC42E |
| `star.svg` | a jagged comic impact star, 12 uneven points, gold #FFC42E |
| `cage.svg` | a birdcage seen from the front, empty, heavy black bars |
| `bolt.svg` | a lightning bolt, gold #FFC42E |

---

## 4. Secuencias de animacion (PNG)

Destino: `art/png/<nombre>/frame_00.png` … `frame_NN.png`.

**Reglas que no se pueden saltar**, o la animacion tiembla:

- **Mismo lienzo exacto en todos los fotogramas** de una secuencia, 512 x 512 a 2x.
- **El personaje no se desplaza** dentro del lienzo: solo se mueve la pose. Si el
  cuerpo cambia de sitio entre fotogramas, en la aplicacion parece que salta.
- **Fondo transparente**, sin sombra en el suelo.
- El ciclo tiene que **cerrar**: el ultimo fotograma enlaza con el primero.

### `pola_run/` — 8 fotogramas (el ciclo de carrera de la pantalla de carga)
> Frame {N} of 8 in a seamless side-view running cycle of the orange hero cat
> Pola, facing right, running in place. Standard four-legged gallop cycle:
> frame 1 contact, 2 down, 3 passing, 4 up, 5 contact opposite, 6 down opposite,
> 7 passing opposite, 8 up opposite. Cape streaming back. Tail extended and
> waving. The body stays in the SAME position within the canvas in every frame;
> only the pose changes. Frame 8 must flow back into frame 1.

### `pola_idle/` — 4 fotogramas
> Frame {N} of 4 in a seamless idle breathing loop of the orange hero cat Pola,
> standing in a heroic pose facing the viewer. Very subtle: chest rises and
> falls, tail sways slightly, cape shifts. Feet never move.

### `limon_laugh/` — 6 fotogramas
> Frame {N} of 6 in a seamless evil laughing loop of the crimson villain cat
> Limon, facing the viewer. Head tilts back progressively, mouth opens wider
> showing fangs, shoulders shake. Returns to the start pose on frame 6.

### `minerva_type/` — 4 fotogramas
> Frame {N} of 4 in a seamless loop of the cyan cat Minerva typing rapidly on a
> keyboard, seen from the front. Paws alternate on the keys, glasses flash,
> head bobs slightly.

### `nina_wave/` — 4 fotogramas (para cuando la rescatan)
> Frame {N} of 4 in a seamless loop of the pink cat Nina waving one paw
> happily, sitting. Tail curls and uncurls.

---

## 5. Mobiliario de comic

**Lo que NO hay que generar:** la trama de puntos, las estrellas de impacto, los
bocadillos y las lineas de velocidad ya se dibujan por codigo en
`ui/fx/Ink.java`, y asi pueden recolorearse por mision y escalarse sin perder
nitidez. No hacen falta como imagenes.

**Lo que si vale la pena generar:**

### `art/png/panel-border.png` — marco de vineta 9-patch
> A hand-inked comic panel border frame, square, 300 x 300 pixels. Only the
> border: a thick irregular hand-drawn black rectangle outline about 24 pixels
> thick, with natural brush wobble and slightly thicker corners. The centre is
> fully transparent. No content inside, no background.

JavaFX lo estira como 9-patch con
`-fx-border-image-source` + `-fx-border-image-slice: 24` + `-fx-border-image-repeat: stretch`.
Las esquinas se mantienen y solo se estiran los lados, que es lo que hace que el
marco parezca dibujado a mano a cualquier tamano.

### `art/png/cover-logo.png` — rotulo del titulo
> The words "THE FELINE GRAPH CHRONICLES" as a comic book cover logo. Bold
> extended comic lettering, gold #FFC42E letter faces, thick black outline, a
> crimson #D81B4A offset drop shadow to the lower right as if the colour plate
> were misregistered. Slight upward arc. Transparent background, 2000 x 500.

---

## 6. Como enchufarlo

Ahora mismo los personajes son figuras primitivas dibujadas en
`ui/fx/CatArt.java`. Cuando llegue el arte:

1. Dejar los archivos en `src/main/resources/com/eia/feline/ui/art/...`.
2. En `ui/screen/Missions.java`, cambiar los `() -> portrait(...)` por la carga
   de la imagen. Es el unico sitio donde se decide que retrato lleva cada mision.
3. Para las secuencias, `Ink` ya tiene el patron de animacion por `Timeline`;
   basta con un `ImageView` que cambie de `Image` por fotograma.
4. `CatArt` se puede borrar el dia que no quede ningun sitio que lo use.

**Cuidado con el SVG:** JavaFX **no carga archivos .svg**. Hay dos salidas:

- Exportar cada SVG tambien a **PNG a 3x** (3000 px de ancho) y cargar el PNG.
  Es lo mas simple y lo recomendable para los retratos.
- O abrir el `.svg`, copiar el atributo `d` de cada `<path>` y pasarlo a un
  `javafx.scene.shape.SVGPath`. Escala infinitamente y se puede recolorear desde
  el codigo, pero solo funciona bien con SVG de trazados simples, sin filtros ni
  degradados ni grupos anidados.

Por eso el preambulo de estilo insiste en **colores planos y sin degradados**: no
es solo estetica, es lo que mantiene los SVG convertibles a `SVGPath`.

---

## 7. Lista de comprobacion

- [ ] Fondo transparente de verdad (no un blanco que lo parezca)
- [ ] Linea negra gruesa y continua en toda la silueta
- [ ] Nada de degradados ni sombras suaves
- [ ] Solo los colores de la paleta
- [ ] Iconos legibles a 24 px
- [ ] Todos los fotogramas de una secuencia, mismo lienzo y personaje quieto
- [ ] El ciclo cierra: ultimo fotograma enlaza con el primero
- [ ] Retratos exportados tambien a PNG 3x, que es lo que JavaFX puede cargar
