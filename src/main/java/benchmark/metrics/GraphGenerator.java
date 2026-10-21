package benchmark.metrics;

import graph.DirectedGraph;
import graph.UndirectedGraph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Reproducible random-graph factory. All generators take a {@link Random}
 * seeded by the caller so benchmark suites can be replayed exactly.
 */
public final class GraphGenerator {
    /**
     * Named problem sizes used by the assignment benchmark.
     */
    public enum Size {
        SMALL(10, 30),
        MEDIUM(100, 500),
        LARGE(1000, 5000);

        private final int vertices;
        private final int edges;

        Size(int vertices, int edges) {
            this.vertices = vertices;
            this.edges = edges;
        }

        public int vertices() {
            return vertices;
        }

        public int edges() {
            return edges;
        }
    }

    private GraphGenerator() {
    }

    /**
     * Builds a connected undirected weighted graph with exactly {@code edgeCount}
     * unique undirected edges (a spanning tree plus extra random chords).
     *
     * @param vertexCount number of vertices
     * @param edgeCount   number of undirected edges, at least {@code vertexCount - 1}
     *                    when {@code vertexCount > 0}
     * @param random      seeded RNG
     * @return connected undirected graph
     */
    public static UndirectedGraph connectedUndirected(int vertexCount, int edgeCount, Random random) {
        if (vertexCount < 0 || edgeCount < 0) {
            throw new IllegalArgumentException("Counts must be non-negative");
        }
        int maxEdges = vertexCount <= 1 ? 0 : vertexCount * (vertexCount - 1) / 2;
        if (vertexCount > 0 && edgeCount < vertexCount - 1) {
            throw new IllegalArgumentException("A connected graph needs at least V-1 edges");
        }
        if (edgeCount > maxEdges) {
            throw new IllegalArgumentException("Too many edges for a simple undirected graph");
        }

        UndirectedGraph graph = new UndirectedGraph(vertexCount);
        Set<Long> used = new HashSet<>();
        if (vertexCount <= 1) {
            return graph;
        }

        int[] order = shuffledVertices(vertexCount, random);
        for (int i = 1; i < vertexCount; i++) {
            int parent = order[random.nextInt(i)];
            int child = order[i];
            int weight = randomWeight(random);
            addUndirected(graph, used, parent, child, weight);
        }

        while (graph.edgeCount() < edgeCount) {
            int u = random.nextInt(vertexCount);
            int v = random.nextInt(vertexCount);
            if (u == v || used.contains(undirectedKey(u, v))) {
                continue;
            }
            addUndirected(graph, used, u, v, randomWeight(random));
        }
        return graph;
    }

    /**
     * Builds a possibly disconnected undirected graph (no spanning-tree scaffold).
     *
     * @param vertexCount number of vertices
     * @param edgeCount   number of unique undirected edges
     * @param random      seeded RNG
     * @return undirected graph that may have several components
     */
    public static UndirectedGraph randomUndirected(int vertexCount, int edgeCount, Random random) {
        UndirectedGraph graph = new UndirectedGraph(vertexCount);
        Set<Long> used = new HashSet<>();
        int attempts = 0;
        while (graph.edgeCount() < edgeCount && attempts < edgeCount * 40 + 16) {
            attempts++;
            if (vertexCount < 2) {
                break;
            }
            int u = random.nextInt(vertexCount);
            int v = random.nextInt(vertexCount);
            if (u == v || used.contains(undirectedKey(u, v))) {
                continue;
            }
            addUndirected(graph, used, u, v, randomWeight(random));
        }
        return graph;
    }

    /**
     * Builds a random directed weighted graph with unique arcs (no self-loops).
     *
     * @param vertexCount number of vertices
     * @param edgeCount   target number of arcs
     * @param random      seeded RNG
     * @return directed graph
     */
    public static DirectedGraph randomDirected(int vertexCount, int edgeCount, Random random) {
        DirectedGraph graph = new DirectedGraph(vertexCount);
        Set<Long> used = new HashSet<>();
        int attempts = 0;
        while (graph.edgeCount() < edgeCount && attempts < Math.max(64, edgeCount * 40)) {
            attempts++;
            if (vertexCount == 0) {
                break;
            }
            int u = random.nextInt(vertexCount);
            int v = random.nextInt(vertexCount);
            if (u == v) {
                continue;
            }
            long key = directedKey(u, v);
            if (!used.add(key)) {
                continue;
            }
            graph.addEdge(u, v, randomWeight(random));
        }
        return graph;
    }

    /**
     * Builds a random DAG by directing every generated edge along a random total order.
     *
     * @param vertexCount number of vertices
     * @param edgeCount   number of arcs
     * @param random      seeded RNG
     * @return DAG
     */
    public static DirectedGraph randomDag(int vertexCount, int edgeCount, Random random) {
        DirectedGraph graph = new DirectedGraph(vertexCount);
        if (vertexCount <= 1) {
            return graph;
        }
        int[] order = shuffledVertices(vertexCount, random);
        int[] position = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            position[order[i]] = i;
        }
        Set<Long> used = new HashSet<>();
        int attempts = 0;
        while (graph.edgeCount() < edgeCount && attempts < edgeCount * 40 + 16) {
            attempts++;
            int u = random.nextInt(vertexCount);
            int v = random.nextInt(vertexCount);
            if (u == v) {
                continue;
            }
            if (position[u] > position[v]) {
                int tmp = u;
                u = v;
                v = tmp;
            }
            long key = directedKey(u, v);
            if (!used.add(key)) {
                continue;
            }
            graph.addEdge(u, v, randomWeight(random));
        }
        return graph;
    }

    private static int[] shuffledVertices(int n, Random random) {
        int[] order = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }
        return order;
    }

    /**
     * Weights in {@code [1, 50]} with intentional collisions so that multiple
     * MSTs can exist on the same instance.
     */
    private static int randomWeight(Random random) {
        return 1 + random.nextInt(50);
    }

    private static void addUndirected(UndirectedGraph graph, Set<Long> used, int u, int v, int weight) {
        used.add(undirectedKey(u, v));
        graph.addEdge(u, v, weight);
    }

    private static long undirectedKey(int u, int v) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    private static long directedKey(int u, int v) {
        return (((long) u) << 32) | (v & 0xffffffffL);
    }
}
