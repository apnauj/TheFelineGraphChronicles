# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A university project (Languages and Compilers, Universidad EIA) themed as "The Feline Graph Chronicles".
Four *missions*, each one a competitive-programming-style problem solved with graph algorithms that must
be **implemented from scratch** — graph libraries (JGraphT, GraphStream, Guava Graphs) are forbidden for
the algorithmic core. A library used only to lay out or render a drawing is allowed but must be declared
in the README.

| Mission | Algorithms | Status |
|---|---|---|
| 1 — Rescue Nina from the minefield | BFS + DFS on a grid | implemented |
| 2 — Retrieve the Claude accounts | Dijkstra | implemented |
| 3 — The ultimate food stash | Floyd-Warshall + Bellman-Ford | runs on temporary scaffolding |
| 4 — Reconnect the network | Kruskal + union-find | runs on temporary scaffolding |

**Missions 3 and 4 are not finished.** They work end to end and produce the statement's expected
output, but their algorithms live in `missions/stub/` and are placeholders written so the GUI could
be built and demonstrated while another member writes the real ones. `algo/maxwalk/` and `algo/mst/`
are deliberately empty and reserved for those. When they land: change the two calls in
`MissionThreeSolver.solve` / `MissionFourSolver.solve`, delete `missions/stub/`, and flip
`implemented` to `true` in `ui/screen/Missions.java`. No screen or visualizer changes. Do not quietly
promote the scaffolding to `algo/` — the in-app banner and the README both say it is temporary.

## Commands

```bash
mvn test                                     # all tests; never opens a window
mvn test -Dtest=MissionOneSolverTest         # one class
mvn test -Dtest=MissionOneSolverTest#statementSample   # one method
mvn javafx:run                               # launch the GUI (the one documented command)
                                             # needs network on first run to fetch JavaFX

# Run a mission headless, straight from the statement's sample input:
mvn -q compile
java -cp target/classes com.eia.feline.missions.MissionOneSolver < input.txt
```

`maven.compiler.release` is pinned to **17**, not the local JDK, so the project compiles on any
evaluator machine running JDK 17 or later.

## Architecture

Three layers, and the dependency arrow only ever points one way: `ui → missions → algo`.

```
algo/       pure algorithms. No JavaFX, Swing or AWT import — enforced by AlgoPurityTest.
  graph/    Adjacency (CSR read interface) · WeightedGraph · EdgeList
  grid/     GridGraph — the Mission 1 board as a graph
  search/   BFS · DFS · SearchResult
  sp/       Dijkstra · ShortestPathResult
  maxwalk/  Mission 3 slot (empty, reserved)
  mst/      Mission 4 slot (empty, reserved)

missions/   text in → exact output lines + structured payload out. Still no UI imports.
  Tokenizer · InputFormatException · MissionSolver · CaseResult · MissionOneSolver … MissionFourSolver
  stub/     TEMPORARY scaffolding for missions 3 and 4 — see its README

ui/         the only package allowed to import JavaFX.
  theme/    Theme.java + theme.css — the palette, defined once in two formats
  screen/   Navigator · LoadingScreen · MissionSelectScreen · MissionScreen · MissionDescriptor · Missions
  viz/      Visualizer · Playback · GridVisualizer · GraphVisualizer · MaxWalkVisualizer
            MstVisualizer · MatrixPane · SpringLayout
  fx/       CatArt — placeholder character art drawn from primitives
```

`MissionScreen` is **one class for all four missions**, configured by a `MissionDescriptor`. Adding a
mission means adding an entry to `Missions.all()`, not writing another screen. `Visualizer<P>` is the
drawing contract: build a node, render a payload, step through it, and answer whether the instance
fits the §2.3 size budget.

### The two contracts everything hangs off

**`MissionSolver<P>`** — every mission parses raw text and returns one `CaseResult` per test case:

```java
public record CaseResult<P>(int index, String outputLine, P payload) {}
```

`outputLine` is the finished ASCII line (`"Case #1: BFS 18 DFS 32"`). The GUI concatenates it and never
reformats it, so the auto-compared text is produced in exactly one place. `payload` is the structured
state the drawing needs. Adding a mission means adding a solver and a payload record, not touching the UI.

**`Adjacency`** — the CSR read interface. BFS and DFS take `Adjacency`, not a concrete class, so the same
code runs on the Mission 1 grid and on hand-built test graphs:

```java
for (int e = g.adjStart(v); e < g.adjEnd(v); e++) {
    int neighbour = g.adjTarget(e);
}
```

## Invariants that are easy to break

**DFS neighbour order is graded.** The statement requires DFS to expand **up, down, left, right**, and a
different order yields a different (still valid) path that will not match the expected output.
`DFS.java` contains no direction logic at all: the order comes from `GridGraph` storing each cell's
neighbours as **right, left, down, up** and the stack being LIFO. If you touch either file, keep them in
sync — `GridGraphTest.neighbourBlockIsStoredInReverseOfTheVisitOrder` and
`MissionOneSolverTest.dfsNeighbourOrderIsDeterministic` pin this.

**Output strings are compared character by character.** Plain ASCII, no emoji, no accents, no trailing
period. The exact special messages are `Nina is unreachable`, `Nina is very sad`,
`Limon blocked the way`, `Infinite churun!`, `Limon cut too many cables`. Note the statement writes
`Case #k: BFS <b> DFS <d>` with `<b>` as a *placeholder* — the angle brackets are not part of the output.

**`long` for accumulated weights** in Missions 2, 3 and 4. Mission 2 allows 10,000 nodes at 1,000,000 per
edge, so a route can cost ~10^10 and silently overflows `int`. The "no route" sentinel is
`Long.MAX_VALUE`, and arithmetic must never be performed on it — in `Dijkstra` this holds because only
nodes popped from the heap get relaxed, and those always have a finite distance.

**DFS must not be recursive.** Grids reach 10^6 cells, which would blow the JVM stack. It uses an
explicit stack bounded by `edgeCount() + 1`, because visited is marked on *pop* (matching the canonical
recursive traversal), so a node can be pushed more than once.

**Size limits on drawing** (statement §2.3): grids up to 50×50, Missions 2–3 up to 60 nodes, Mission 4 up
to 100 intersections and 300 cables; the Mission 3 N×N matrix must be shown scrollable for every N ≤ 100.
Above a threshold the GUI still computes and displays the numeric answer plus a visible message saying
the drawing was omitted because of the instance size.

**Malformed input must surface as a readable in-GUI message, never a stack trace.** That is why
`InputFormatException` is a *checked* exception — callers cannot forget it. Parse through `Tokenizer`,
which names both the offending token and its position, and use the range-checked
`nextInt(what, low, high)` so out-of-range node ids are reported instead of crashing deep inside an
algorithm.

## Why CSR instead of `List<List<Integer>>`

At the statement's limit (1000×1000 = 10^6 cells) a list of lists needs ~10^6 `ArrayList` objects plus an
autoboxed `Integer` per neighbour — on the order of 200 MB, enough to exhaust the default heap. Two
primitive `int[]` arrays use ~20 MB and iterate contiguously. `GridGraphTest` keeps the original
list-of-lists construction as a reference and asserts the CSR build matches it neighbour for neighbour,
in order.

## Conventions

- Algorithm classes carry a comment stating **time complexity, space complexity, and why that algorithm
  is the right choice for that mission** — the statement grades this, and the oral defense asks about it.
- Existing code comments are in Spanish; match the surrounding language when editing a file.
- Each mission's `sampleInput()` is the verbatim sample from the statement and powers both the GUI's
  "load sample" button and the tests. Keep the two uses in sync by never duplicating the literal.
- Solvers keep a `main()` that pipes stdin through `solve()`, so any mission can be diffed against the
  statement sample without opening a window.

## Deliverables the statement requires (do not drop these)

`README.md` (members, one-command build/run, structure, known limitations, decisions taken),
`AI_USAGE.md` (tools used, decisive prompts, **at least two cases where generated output was wrong and
how it was fixed**, what each member learned), at least one automated test per algorithm using the
statement samples as expected values, and a full git history — a single commit on the due date is itself
a finding at the defense.

## Drawing

Everything is painted on a `Canvas`, never one scene node per cell: a 50×50 grid is 2,500 cells and
the Mission 3 matrix is 10,000, and a node each takes seconds to build. Node placement is
`ui/viz/SpringLayout`, a hand-written Fruchterman-Reingold — no graph library is used for drawing
either. It is O(iterations · N²), which is only acceptable because §2.3 caps drawing at 60 nodes.

BFS and DFS are drawn on **separate boards side by side**. An earlier version overlaid both on one
grid and the translucent layers blended into a colour that hid which algorithm reached which cell —
the one thing that mission exists to show. Do not merge them back.

Solving runs on a background `Task`; at the 1000×1000 limit it takes about a second and would
otherwise freeze the window.

## Verifying UI changes

The screens can be rendered to PNG headlessly-ish and inspected rather than guessed at: build a
throwaway `Application` that shows a `Stage`, calls `scene.snapshot(...)`, writes the image with
`ImageIO`, and exits. Note `javafx-swing` is not a dependency, so copy pixels via `PixelReader` into
a `BufferedImage` instead of `SwingFXUtils`. The launcher class must not extend `Application` or the
JDK rejects it for not being on the module path — same reason `ui/Launcher` exists.
