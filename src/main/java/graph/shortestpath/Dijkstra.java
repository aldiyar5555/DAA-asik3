package graph.shortestpath;

import benchmark.metrics.OperationCounter;
import graph.DirectedGraph;
import graph.Edge;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Dijkstra's shortest-path algorithm with a binary heap.
 * Requires non-negative edge weights. Unreachable vertices keep distance
 * {@link #UNREACHABLE}.
 *
 * <p>Time: {@code O((V + E) log V)}. Space: {@code O(V)}.
 */
public final class Dijkstra {
    /**
     * Sentinel distance for vertices that cannot be reached from the source.
     */
    public static final long UNREACHABLE = Long.MAX_VALUE;

    private Dijkstra() {
    }

    /**
     * Single-source shortest path result.
     *
     * @param distance     distance from the source, or {@link #UNREACHABLE}
     * @param parent       predecessor on the shortest-path tree, {@code -1} if none
     * @param operations   recorded operations
     * @param elapsedNanos duration
     */
    public record Result(long[] distance,
                         int[] parent,
                         OperationCounter operations,
                         long elapsedNanos) {
        /**
         * @param vertex destination
         * @return {@code true} if {@code vertex} is reachable from the source
         */
        public boolean reachable(int vertex) {
            return distance[vertex] != UNREACHABLE;
        }
    }

    /**
     * Computes shortest paths from {@code source} on a directed graph.
     *
     * @param graph  directed graph with non-negative weights
     * @param source source vertex
     * @return distances and predecessors
     * @throws IllegalArgumentException if a negative-weight edge is present
     */
    public static Result shortestPaths(DirectedGraph graph, int source) {
        if (graph.hasNegativeWeight()) {
            throw new IllegalArgumentException(
                    "Dijkstra requires non-negative edge weights; a negative weight was found");
        }
        if (source < 0 || source >= graph.vertexCount()) {
            throw new IllegalArgumentException("Source out of range: " + source);
        }

        OperationCounter counter = new OperationCounter();
        long started = System.nanoTime();
        int n = graph.vertexCount();
        long[] dist = new long[n];
        int[] parent = new int[n];
        boolean[] settled = new boolean[n];
        Arrays.fill(dist, UNREACHABLE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        PriorityQueue<long[]> heap = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));
        heap.offer(new long[]{0L, source});

        while (!heap.isEmpty()) {
            long[] current = heap.poll();
            int v = (int) current[1];
            long d = current[0];
            counter.comparison();
            if (settled[v] || d != dist[v]) {
                continue;
            }
            settled[v] = true;
            for (Edge edge : graph.outgoing(v)) {
                counter.edgeExamined();
                if (edge.weight() < 0) {
                    throw new IllegalArgumentException("Negative weight on edge " + edge);
                }
                int w = edge.to();
                long candidate = dist[v] + edge.weight();
                counter.comparison();
                if (candidate < dist[w]) {
                    dist[w] = candidate;
                    parent[w] = v;
                    heap.offer(new long[]{candidate, w});
                }
            }
        }

        long elapsed = System.nanoTime() - started;
        return new Result(dist, parent, counter, elapsed);
    }
}
