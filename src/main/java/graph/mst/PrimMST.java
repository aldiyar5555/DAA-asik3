package graph.mst;

import benchmark.metrics.OperationCounter;
import graph.Edge;
import graph.UndirectedGraph;
import graph.dsu.DisjointSetUnion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Prim's algorithm grown from the lowest-index unvisited vertex of each component,
 * using a binary heap ({@link PriorityQueue}).
 *
 * <p>Time: {@code O(E log V)} with a binary heap.
 * Space: {@code O(V + E)}.
 */
public final class PrimMST {
    private PrimMST() {
    }

    /**
     * Computes a minimum spanning tree or forest.
     *
     * @param graph undirected weighted graph
     * @return MST result with connectivity information
     */
    public static MSTResult compute(UndirectedGraph graph) {
        OperationCounter counter = new OperationCounter();
        long started = System.nanoTime();

        int n = graph.vertexCount();
        boolean[] inTree = new boolean[n];
        int[] parent = new int[n];
        int[] best = new int[n];
        Arrays.fill(parent, -1);
        Arrays.fill(best, Integer.MAX_VALUE);

        List<Edge> tree = new ArrayList<>();
        long weight = 0;
        int components = 0;

        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            growComponent(graph, start, inTree, parent, best, tree, counter);
        }

        for (Edge edge : tree) {
            weight += edge.weight();
        }

        boolean connected = n == 0 || components == 1;
        long elapsed = System.nanoTime() - started;
        return new MSTResult(tree, weight, connected, components, counter, elapsed);
    }

    /**
     * Counts connected components with DSU (used by tests to cross-check Prim/Kruskal).
     *
     * @param graph undirected graph
     * @return number of components
     */
    public static int countComponents(UndirectedGraph graph) {
        DisjointSetUnion dsu = new DisjointSetUnion(graph.vertexCount());
        for (Edge edge : graph.edges()) {
            dsu.union(edge.from(), edge.to());
        }
        return graph.vertexCount() == 0 ? 0 : dsu.componentCount();
    }

    private static void growComponent(UndirectedGraph graph,
                                      int start,
                                      boolean[] inTree,
                                      int[] parent,
                                      int[] best,
                                      List<Edge> tree,
                                      OperationCounter counter) {
        PriorityQueue<int[]> heap = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        best[start] = 0;
        heap.offer(new int[]{0, start});

        while (!heap.isEmpty()) {
            int[] current = heap.poll();
            int vertex = current[1];
            counter.comparison();
            if (inTree[vertex]) {
                continue;
            }
            inTree[vertex] = true;
            if (parent[vertex] != -1) {
                tree.add(new Edge(parent[vertex], vertex, best[vertex]));
            }
            for (Edge edge : graph.adjacent(vertex)) {
                counter.edgeExamined();
                int neighbour = edge.to();
                counter.comparison();
                if (!inTree[neighbour] && edge.weight() < best[neighbour]) {
                    best[neighbour] = edge.weight();
                    parent[neighbour] = vertex;
                    heap.offer(new int[]{edge.weight(), neighbour});
                }
            }
        }
    }
}
