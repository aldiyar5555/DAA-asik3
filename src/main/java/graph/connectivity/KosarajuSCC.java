package graph.connectivity;

import benchmark.metrics.OperationCounter;
import graph.DirectedGraph;
import graph.Edge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Kosaraju's algorithm for strongly connected components:
 * DFS finishing times on {@code G}, then DFS on the transpose {@code G^T}.
 *
 * <p>Time: {@code O(V + E)}. Space: {@code O(V + E)}.
 */
public final class KosarajuSCC {
    private KosarajuSCC() {
    }

    /**
     * SCC decomposition of a directed graph.
     *
     * @param componentOf      component id of each vertex
     * @param components       lists of vertices per component
     * @param componentCount   number of SCCs
     * @param operations       recorded operations
     * @param elapsedNanos     duration
     */
    public record Result(int[] componentOf,
                         List<List<Integer>> components,
                         int componentCount,
                         OperationCounter operations,
                         long elapsedNanos) {
    }

    /**
     * Computes strongly connected components.
     *
     * @param graph directed graph
     * @return SCC result
     */
    public static Result compute(DirectedGraph graph) {
        OperationCounter counter = new OperationCounter();
        long started = System.nanoTime();
        int n = graph.vertexCount();

        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>(n);
        for (int v = 0; v < n; v++) {
            if (!visited[v]) {
                dfsFinish(graph, v, visited, order, counter);
            }
        }

        DirectedGraph transpose = graph.transpose();
        Arrays.fill(visited, false);
        int[] componentOf = new int[n];
        Arrays.fill(componentOf, -1);
        List<List<Integer>> components = new ArrayList<>();
        int componentId = 0;

        for (int i = order.size() - 1; i >= 0; i--) {
            int v = order.get(i);
            if (!visited[v]) {
                List<Integer> bucket = new ArrayList<>();
                dfsAssign(transpose, v, visited, componentOf, componentId, bucket, counter);
                components.add(bucket);
                componentId++;
            }
        }

        long elapsed = System.nanoTime() - started;
        return new Result(componentOf, components, componentId, counter, elapsed);
    }

    private static void dfsFinish(DirectedGraph graph,
                                  int v,
                                  boolean[] visited,
                                  List<Integer> order,
                                  OperationCounter counter) {
        visited[v] = true;
        for (Edge edge : graph.outgoing(v)) {
            counter.edgeExamined();
            int w = edge.to();
            counter.comparison();
            if (!visited[w]) {
                dfsFinish(graph, w, visited, order, counter);
            }
        }
        order.add(v);
    }

    private static void dfsAssign(DirectedGraph graph,
                                  int v,
                                  boolean[] visited,
                                  int[] componentOf,
                                  int componentId,
                                  List<Integer> bucket,
                                  OperationCounter counter) {
        visited[v] = true;
        componentOf[v] = componentId;
        bucket.add(v);
        for (Edge edge : graph.outgoing(v)) {
            counter.edgeExamined();
            int w = edge.to();
            counter.comparison();
            if (!visited[w]) {
                dfsAssign(graph, w, visited, componentOf, componentId, bucket, counter);
            }
        }
    }
}
