package benchmark.metrics;

/**
 * Accumulates primitive operation counts used in the operational analysis:
 * comparisons, edge examinations and DSU primitives.
 */
public final class OperationCounter {
    private long comparisons;
    private long edgeExaminations;
    private long dsuFinds;
    private long dsuUnions;
    private long dsuParentUpdates;

    /**
     * Records one comparison (keys, ranks, distances, etc.).
     */
    public void comparison() {
        comparisons++;
    }

    /**
     * Records {@code count} comparisons.
     *
     * @param count number of comparisons
     */
    public void comparisons(long count) {
        comparisons += count;
    }

    /**
     * Records inspection of one edge.
     */
    public void edgeExamined() {
        edgeExaminations++;
    }

    /**
     * Records a DSU {@code find} call.
     */
    public void dsuFind() {
        dsuFinds++;
    }

    /**
     * Records a DSU {@code union} call.
     */
    public void dsuUnion() {
        dsuUnions++;
    }

    /**
     * Records a parent-pointer rewrite during path compression.
     */
    public void dsuParentUpdate() {
        dsuParentUpdates++;
    }

    /**
     * @return comparison count
     */
    public long getComparisons() {
        return comparisons;
    }

    /**
     * @return edge examination count
     */
    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    /**
     * @return DSU find count
     */
    public long getDsuFinds() {
        return dsuFinds;
    }

    /**
     * @return DSU union count
     */
    public long getDsuUnions() {
        return dsuUnions;
    }

    /**
     * @return total DSU primitives (find + union + parent updates)
     */
    public long getDsuOperations() {
        return dsuFinds + dsuUnions + dsuParentUpdates;
    }

    /**
     * @return a snapshot copy of this counter
     */
    public OperationCounter copy() {
        OperationCounter copy = new OperationCounter();
        copy.comparisons = this.comparisons;
        copy.edgeExaminations = this.edgeExaminations;
        copy.dsuFinds = this.dsuFinds;
        copy.dsuUnions = this.dsuUnions;
        copy.dsuParentUpdates = this.dsuParentUpdates;
        return copy;
    }

    @Override
    public String toString() {
        return "comparisons=" + comparisons
                + ", edgeExaminations=" + edgeExaminations
                + ", dsuOps=" + getDsuOperations();
    }
}
