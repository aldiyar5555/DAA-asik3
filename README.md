# Assignment 3: Smart City Infrastructure and Route Planning

Java implementation of classic graph algorithms used as a model of city infrastructure (road layout, connectivity of districts, and fastest routes). The project covers Disjoint Set Union, minimum spanning trees, strongly connected components, topological ordering, Dijkstra shortest paths, a reproducible benchmark, and JUnit tests.

## 1. Overview

A **smart city** network is modelled as a weighted graph:

- **Vertices** are intersections, districts or service hubs (`0 .. V-1`).
- **Undirected edges** are two-way roads or utility links (used by MST / connectivity of the physical network).
- **Directed edges** are one-way streets or dependency constraints (used by SCC, topological sort and Dijkstra).

### Graph representation

| Structure | Class | Storage | Typical use |
|---|---|---|---|
| Undirected weighted graph | `graph.UndirectedGraph` | adjacency lists **and** an edge list | Kruskal, Prim |
| Directed weighted graph | `graph.DirectedGraph` | adjacency lists + edge list, plus `transpose()` | SCC, topo-sort, Dijkstra |
| Weighted arc | `graph.Edge` | endpoints + integer weight | all algorithms |

Adjacency lists give `O(1)` neighbour iteration and `O(V + E)` space. Kruskal also needs a flat edge list for sorting; Prim and Dijkstra iterate adjacency lists.

Package layout:

```
graph/
  Edge, UndirectedGraph, DirectedGraph
  dsu/           DisjointSetUnion
  mst/           KruskalMST, PrimMST, MSTResult
  connectivity/  TopologicalSort, KosarajuSCC
  shortestpath/  Dijkstra
benchmark/metrics/
  OperationCounter, GraphGenerator, BenchmarkRunner, BenchmarkResult
src/test/java/test/
  JUnit 5 edge-case suite
```

## 2. Implementation

### DSU (`graph.dsu`)

`DisjointSetUnion` implements **path compression** in `find(i)` and a linking heuristic in `union(i, j)`:

- `UNION_BY_RANK` — attach the shallower tree under the deeper one.
- `UNION_BY_SIZE` — attach the smaller tree under the larger one.

Both heuristics keep trees shallow; together with path compression the amortized cost of a primitive is `O(α(V))`. Every find, union, parent rewrite, comparison and edge scan is recorded in `OperationCounter`.

### MST (`graph.mst`)

- **Kruskal**: sort edges by weight, add an edge iff DSU reports distinct components, stop after `V − 1` tree edges. Returns a **minimum spanning forest** and a connectivity flag when the graph is disconnected.
- **Prim**: grow a tree from each unused vertex with a binary heap (`PriorityQueue`). Isolated vertices start a new component, so disconnected inputs produce a forest of the same total weight as Kruskal.

### Connectivity (`graph.connectivity`)

- **Topological sort** uses Kahn’s algorithm (indegrees + queue). If fewer than `V` vertices are emitted, a cycle is reported: `Result.acyclic == false`, and `sortOrThrow` raises `CycleDetectedException`.
- **SCC** uses **Kosaraju**: DFS finishing times on `G`, then DFS on the transpose `Gᵀ`. Component ids are assigned to every vertex, including isolated ones.

### Shortest paths (`graph.shortestpath`)

Dijkstra with a binary heap. Negative weights are rejected **before** the search (`hasNegativeWeight()`). Unreachable vertices keep distance `Dijkstra.UNREACHABLE` (`Long.MAX_VALUE`) and parent `−1`.

### Benchmark (`benchmark.metrics`)

`GraphGenerator` builds reproducible instances from `java.util.Random` with a documented master seed. `BenchmarkRunner` warms the JVM, then times **5 runs** of each algorithm on Small / Medium / Large graphs using `System.nanoTime()`, averaging time and operation counts.

## 3. Complexity Analysis

`α(V)` is the inverse Ackermann function (practically constant). Heap implementations use Java’s `PriorityQueue` (binary heap).

| Algorithm | Time | Space | Notes |
|---|---|---|---|
| DSU `find` / `union` | Amortized `O(α(V))` | `O(V)` | Path compression + union by rank or size |
| Kruskal | `O(E log E)` | `O(V + E)` | Dominated by sorting; DSU is nearly linear |
| Prim (binary heap) | `O(E log V)` | `O(V + E)` | One heap node per relaxation |
| Kosaraju SCC | `O(V + E)` | `O(V + E)` | Two DFS passes + transpose |
| Topological sort (Kahn) | `O(V + E)` | `O(V)` | Cycle if `|order| < V` |
| Dijkstra (binary heap) | `O((V + E) log V)` | `O(V)` | Non-negative weights only |

## 4. Experimental Setup

| Parameter | Value |
|---|---|
| JDK | OpenJDK 17 (Eclipse Temurin 17.0.20.1) |
| OS | Windows 10 (build 26200) |
| Timing | `System.nanoTime()`, average of **5** measured runs |
| Warm-up | one full suite execution discarded before measurement |
| Master seed | `20261021` |
| Weight range | integers in `[1, 50]` (intentional collisions) |

Problem sizes from the specification:

| Size | Vertices `V` | Edges `E` |
|---|---:|---:|
| Small | 10 | 30 |
| Medium | 100 | 500 |
| Large | 1000 | 5000 |

Graph families:

- **MST (Kruskal / Prim)** — connected undirected graphs: a random spanning tree plus extra chords, no self-loops, no parallel undirected edges.
- **Kosaraju / Dijkstra** — random simple directed graphs (no self-loops). Dijkstra source is vertex `0`; some vertices may be unreachable.
- **Topological sort** — random DAGs obtained by orienting edges along a random total order.

Operation counters:

- **comparisons** — key comparisons in sort, heap, DSU linking and relaxation.
- **edge examinations** — each time an algorithm inspects an adjacency-list or edge-list entry.
- **DSU operations** — `find` + `union` + parent updates from path compression / linking.

### How to build and run

Maven (`pom.xml`) is provided. If Maven is not installed, compile with JDK 17:

```powershell
# compile, JUnit, benchmark
powershell -File scripts/run.ps1
```

Or manually:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src\main\java\*.java).FullName
java -cp out benchmark.metrics.BenchmarkRunner
```

Tests (JUnit Platform Console Standalone in `lib/`):

```powershell
javac -encoding UTF-8 -d out-test -cp "out;lib\junit-platform-console-standalone-1.10.2.jar" `
  (Get-ChildItem -Recurse src\test\java\*.java).FullName
java -jar lib\junit-platform-console-standalone-1.10.2.jar execute --class-path "out;out-test" --scan-class-path
```

With Maven: `mvn test` and `mvn exec:java`.

## 5. Results

Averages over 5 runs after warm-up. Times are wall-clock; operation counts are independent of the machine.

| Algorithm | Size | V | E | Avg time (ms) | Avg time (µs) | Comparisons | Edge exams | DSU ops |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| Kruskal (union-by-rank) | SMALL | 10 | 30 | 0.023 | 22.8 | 163.6 | 10.4 | 48.8 |
| Kruskal (union-by-size) | SMALL | 10 | 30 | 0.051 | 51.1 | 165.4 | 12.0 | 59.6 |
| Prim (binary heap) | SMALL | 10 | 30 | 0.112 | 112.2 | 80.0 | 60.0 | 0.0 |
| Kosaraju SCC | SMALL | 10 | 30 | 0.019 | 19.4 | 60.0 | 60.0 | 0.0 |
| Topological sort (Kahn) | SMALL | 10 | 30 | 0.024 | 24.2 | 40.0 | 60.0 | 0.0 |
| Dijkstra (binary heap) | SMALL | 10 | 30 | 0.030 | 29.7 | 41.0 | 30.0 | 0.0 |
| Kruskal (union-by-rank) | MEDIUM | 100 | 500 | 0.392 | 392.5 | 5196.2 | 254.0 | 1345.8 |
| Kruskal (union-by-size) | MEDIUM | 100 | 500 | 0.364 | 363.7 | 4906.6 | 209.0 | 1138.2 |
| Prim (binary heap) | MEDIUM | 100 | 500 | 0.686 | 686.4 | 1246.8 | 1000.0 | 0.0 |
| Kosaraju SCC | MEDIUM | 100 | 500 | 0.199 | 198.6 | 1000.0 | 1000.0 | 0.0 |
| Topological sort (Kahn) | MEDIUM | 100 | 500 | 0.288 | 288.2 | 600.0 | 1000.0 | 0.0 |
| Dijkstra (binary heap) | MEDIUM | 100 | 500 | 0.397 | 396.5 | 630.2 | 495.8 | 0.0 |
| Kruskal (union-by-rank) | LARGE | 1000 | 5000 | 3.187 | 3186.7 | 64576.8 | 3374.6 | 18065.6 |
| Kruskal (union-by-size) | LARGE | 1000 | 5000 | 2.772 | 2771.9 | 64261.8 | 3431.2 | 18285.0 |
| Prim (binary heap) | LARGE | 1000 | 5000 | 1.900 | 1899.7 | 12466.6 | 10000.0 | 0.0 |
| Kosaraju SCC | LARGE | 1000 | 5000 | 1.809 | 1808.7 | 10000.0 | 10000.0 | 0.0 |
| Topological sort (Kahn) | LARGE | 1000 | 5000 | 0.601 | 601.2 | 6000.0 | 10000.0 | 0.0 |
| Dijkstra (binary heap) | LARGE | 1000 | 5000 | 2.134 | 2134.0 | 6372.8 | 4952.8 | 0.0 |

Observations from the counters:

- Kruskal’s **edge examinations** are below `E` because the algorithm stops once `V − 1` tree edges are accepted.
- Prim examines each undirected edge **twice** (once from each endpoint), so Large reports `10 000` examinations for `5 000` edges.
- Linear algorithms (Kahn, Kosaraju) scale as `Θ(V + E)`: Large SCC examines `10 000` directed incidences (original + transpose).
- Dijkstra edge exams are slightly below `E` when some arcs never leave the reachable set of source `0`.

## 6. Comparison & Discussion

### Kruskal vs Prim

On **Small** and **Medium** connected graphs Kruskal is faster: sorting a few hundred edges is cheap, and DSU rejects most chords after the tree is almost complete. On **Large** (`V = 1000`, `E = 5000`, about `5` edges per vertex) Prim with a binary heap wins (`1.90 ms` vs `2.77–3.19 ms`). Kruskal still pays `O(E log E)` to sort every edge; Prim only pays heap operations on relaxations and never sorts a global list.

Rule of thumb for this codebase:

- Prefer **Kruskal** when the edge list is already available and `E` is not much larger than `V`, or when you also need a forest on a disconnected network with a single DSU pass.
- Prefer **Prim** on larger sparse networks where the heap avoids a full sort, and when the graph is given as adjacency lists.

Both algorithms produced the **same MST weight** on generated connected instances (checked in tests). Duplicate weights allow several distinct trees of that weight; the implementations may return different edge sets, which is correct.

### Effect of DSU heuristics

Union-by-rank and union-by-size stay within a few percent of each other. On Medium, size-based linking used fewer comparisons (`4906` vs `5196`) and fewer DSU primitives (`1138` vs `1346`). On Large the operation counts are essentially tied; wall-clock still favours size linking slightly (`2.77 ms` vs `3.19 ms`) on this seed. Path compression is enabled in both variants and is the dominant reason trees stay flat. Dropping both heuristics would turn `find` into a linear walk up a chain — unacceptable inside Kruskal.

### Why Dijkstra does not work with negative weights

Dijkstra is a **greedy** algorithm: once a vertex `u` is dequeued with the smallest key, `dist[u]` is declared final. That claim holds only if every remaining edge has non-negative weight, so a later path cannot undercut `dist[u]`.

If some edge `x → y` has weight `−w`, a vertex `y` that was already settled may later receive a strictly better distance through `x`. The binary-heap implementation never re-opens settled vertices, so the answer would be wrong. Classic counter-example:

```
0 --(1)--> 1 --(1)--> 2
0 --(100)--> 2 --(-200)--> 1
```

From `0`, Dijkstra settles `1` at distance `1` and never notices the path `0 → 2 → 1` of length `−100`. Bellman–Ford (or a potential reduction / Johnson) is required when negatives appear. This project therefore **rejects** any negative edge before the search.

## 7. Testing

JUnit 5 suite in `src/test/java/test` (19 tests, all passing):

| Area | Cases |
|---|---|
| DSU | `V = 1`; union-by-rank merges; union-by-size + recorded operations |
| MST | `V = 1` empty tree, connected; disconnected 4-vertex forest; Kruskal = Prim = 37 on the classic 9-vertex textbook graph; duplicate unit weights with several valid MSTs of weight 3 |
| Topo-sort | `V = 1`; DAG precedence constraints; 3-cycle returns `acyclic = false` and throws `CycleDetectedException` |
| SCC | two 2-cycles plus a sink (`3` components); three isolated vertices (`3` SCCs) |
| Dijkstra | `V = 1`; unreachable vertex keeps `UNREACHABLE`; cheaper indirect path; negative weight throws |
| Generator | identical graphs for seed `42`; Small graph is connected and Kruskal/Prim weights match; generated DAGs are acyclic |

## 8. Conclusion

The assignment models city infrastructure as graphs and implements the standard toolkit with explicit operation counts.

Recommendations:

1. **Physical backbone (roads, fibre, power)** — compute an MST. Use Kruskal with compressed DSU on small / medium sparse instances; switch to Prim’s heap as `E` grows into the thousands.
2. **Always enable DSU heuristics** — rank or size plus path compression. The two linking rules are interchangeable in practice; never use naive union.
3. **One-way centres of activity** — Kosaraju SCCs identify districts that remain mutually reachable under directed traffic.
4. **Construction / dependency order** — Kahn topological sort, with the cycle flag treated as a hard planning error.
5. **Fastest route with non-negative travel times** — Dijkstra. If toll refunds or other negative costs appear, do not use this implementation; switch to Bellman–Ford.

All algorithms are implemented with Javadoc, assemble on JDK 17, pass the JUnit edge-case suite, and report reproducible averages for the three required graph sizes.
