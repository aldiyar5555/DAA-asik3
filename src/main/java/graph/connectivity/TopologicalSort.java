package graph.connectivity;

import benchmark.metrics.OperationCounter;
import graph.DirectedGraph;
import graph.Edge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Kahn's algorithm for topological ordering of a directed acyclic graph (DAG).
 * Cycle detection is explicit: a cycle yields {@link CycleDetectedException}
 * or a failed {@link Result} depending on the API used.
 *
 * <p>Time: {@code O(V + E)}. Space: {@code O(V)}.
 */
public final class TopologicalSort {
    private TopologicalSort() {
    }

    /**
     * Result of a topological sort attempt.
     *
     * @param order      linear order, empty if a cycle was found
     * @param acyclic    {@code true} if the graph is a DAG
     * @param operations recorded operations
     * @param elapsedNanos duration
     */
    public record Result(List<Integer> order,
                         boolean acyclic,
                         OperationCounter operations,
                         long elapsedNanos) {
    }

    /**
     * Thrown when a cycle prevents a topological order.
     */
    public static final class CycleDetectedException extends IllegalStateException {
        public CycleDetectedException(String message) {
            super(message);
        }
    }

    /**
     * Computes a topological order or reports a cycle via the result flag.
     *
     * @param graph directed graph
     * @return result with {@code acyclic=false} if a cycle exists
     */
    public static Result sort(DirectedGraph graph) {
        OperationCounter counter = new OperationCounter();
        long started = System.nanoTime();

        int n = graph.vertexCount();
        int[] indegree = new int[n];
        for (Edge edge : graph.edges()) {
            counter.edgeExamined();
            indegree[edge.to()]++;
        }

        Deque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            counter.comparison();
            if (indegree[v] == 0) {
                queue.add(v);
            }
        }

        List<Integer> order = new ArrayList<>(n);
        while (!queue.isEmpty()) {
            int v = queue.removeFirst();
            order.add(v);
            for (Edge edge : graph.outgoing(v)) {
                counter.edgeExamined();
                int w = edge.to();
                indegree[w]--;
                counter.comparison();
                if (indegree[w] == 0) {
                    queue.add(w);
                }
            }
        }

        boolean acyclic = order.size() == n;
        long elapsed = System.nanoTime() - started;
        if (!acyclic) {
            order = List.of();
        }
        return new Result(order, acyclic, counter, elapsed);
    }

    /**
     * Computes a topological order and throws if the graph contains a cycle.
     *
     * @param graph directed graph
     * @return vertex order
     * @throws CycleDetectedException if a cycle is present
     */
    public static List<Integer> sortOrThrow(DirectedGraph graph) {
        Result result = sort(graph);
        if (!result.acyclic()) {
            throw new CycleDetectedException("Graph contains a cycle; topological order does not exist");
        }
        return result.order();
    }
}
