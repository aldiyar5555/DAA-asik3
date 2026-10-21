package graph.mst;

import benchmark.metrics.OperationCounter;
import graph.Edge;

import java.util.Collections;
import java.util.List;

/**
 * Outcome of a minimum spanning tree (or forest) computation.
 */
public final class MSTResult {
    private final List<Edge> edges;
    private final long totalWeight;
    private final boolean connected;
    private final int componentCount;
    private final OperationCounter operations;
    private final long elapsedNanos;

    /**
     * @param edges           selected tree/forest edges
     * @param totalWeight     sum of selected weights
     * @param connected       whether the original graph was connected
     * @param componentCount  number of connected components
     * @param operations      recorded primitive operations
     * @param elapsedNanos    wall-clock duration of the algorithm
     */
    public MSTResult(List<Edge> edges,
                     long totalWeight,
                     boolean connected,
                     int componentCount,
                     OperationCounter operations,
                     long elapsedNanos) {
        this.edges = List.copyOf(edges);
        this.totalWeight = totalWeight;
        this.connected = connected;
        this.componentCount = componentCount;
        this.operations = operations;
        this.elapsedNanos = elapsedNanos;
    }

    /**
     * @return selected MST/MSF edges
     */
    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * @return total weight of selected edges
     */
    public long totalWeight() {
        return totalWeight;
    }

    /**
     * @return {@code true} if the input graph was connected (a tree was produced)
     */
    public boolean connected() {
        return connected;
    }

    /**
     * @return number of connected components in the input graph
     */
    public int componentCount() {
        return componentCount;
    }

    /**
     * @return operation counts
     */
    public OperationCounter operations() {
        return operations;
    }

    /**
     * @return elapsed time in nanoseconds
     */
    public long elapsedNanos() {
        return elapsedNanos;
    }
}
