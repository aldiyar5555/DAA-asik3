package graph.mst;

import benchmark.metrics.OperationCounter;
import graph.Edge;
import graph.UndirectedGraph;
import graph.dsu.DisjointSetUnion;

import java.util.ArrayList;
import java.util.List;

/**
 * Kruskal's algorithm: sort all edges by weight and add an edge when it
 * connects different DSU components.
 *
 * <p>Time: {@code O(E log E)} due to sorting, plus nearly-linear DSU work.
 * Space: {@code O(V + E)}.
 */
public final class KruskalMST {
    private KruskalMST() {
    }

    /**
     * Computes a minimum spanning tree (or forest if the graph is disconnected).
     *
     * @param graph undirected weighted graph
     * @return MST result with connectivity flag and operation counts
     */
    public static MSTResult compute(UndirectedGraph graph) {
        return compute(graph, DisjointSetUnion.Heuristic.UNION_BY_RANK);
    }

    /**
     * Computes MST/MSF using a specified DSU linking heuristic.
     *
     * @param graph     undirected weighted graph
     * @param heuristic DSU heuristic
     * @return MST result
     */
    public static MSTResult compute(UndirectedGraph graph, DisjointSetUnion.Heuristic heuristic) {
        OperationCounter counter = new OperationCounter();
        long started = System.nanoTime();

        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> {
            counter.comparison();
            return Integer.compare(a.weight(), b.weight());
        });

        DisjointSetUnion dsu = new DisjointSetUnion(graph.vertexCount(), heuristic, counter);
        List<Edge> tree = new ArrayList<>();
        long weight = 0;

        for (Edge edge : sorted) {
            counter.edgeExamined();
            if (dsu.union(edge.from(), edge.to())) {
                tree.add(edge);
                weight += edge.weight();
                if (tree.size() == graph.vertexCount() - 1 && graph.vertexCount() > 0) {
                    break;
                }
            }
        }

        int components = graph.vertexCount() == 0 ? 0 : dsu.componentCount();
        boolean connected = graph.vertexCount() == 0 || components == 1;
        long elapsed = System.nanoTime() - started;
        return new MSTResult(tree, weight, connected, components, counter, elapsed);
    }
}
