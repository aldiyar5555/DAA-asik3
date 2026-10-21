package benchmark.metrics;

/**
 * Aggregated statistics of several timed algorithm runs.
 */
public final class BenchmarkResult {
    private final String algorithm;
    private final String sizeLabel;
    private final int vertices;
    private final int edges;
    private final int runs;
    private final double averageNanos;
    private final double averageComparisons;
    private final double averageEdgeExaminations;
    private final double averageDsuOperations;

    public BenchmarkResult(String algorithm,
                           String sizeLabel,
                           int vertices,
                           int edges,
                           int runs,
                           double averageNanos,
                           double averageComparisons,
                           double averageEdgeExaminations,
                           double averageDsuOperations) {
        this.algorithm = algorithm;
        this.sizeLabel = sizeLabel;
        this.vertices = vertices;
        this.edges = edges;
        this.runs = runs;
        this.averageNanos = averageNanos;
        this.averageComparisons = averageComparisons;
        this.averageEdgeExaminations = averageEdgeExaminations;
        this.averageDsuOperations = averageDsuOperations;
    }

    public String algorithm() {
        return algorithm;
    }

    public String sizeLabel() {
        return sizeLabel;
    }

    public int vertices() {
        return vertices;
    }

    public int edges() {
        return edges;
    }

    public int runs() {
        return runs;
    }

    public double averageNanos() {
        return averageNanos;
    }

    public double averageMicros() {
        return averageNanos / 1_000.0;
    }

    public double averageMillis() {
        return averageNanos / 1_000_000.0;
    }

    public double averageComparisons() {
        return averageComparisons;
    }

    public double averageEdgeExaminations() {
        return averageEdgeExaminations;
    }

    public double averageDsuOperations() {
        return averageDsuOperations;
    }

    /**
     * Markdown table row used by the console report.
     */
    public String toMarkdownRow() {
        return String.format(java.util.Locale.US,
                "| %s | %s | %d | %d | %.3f | %.1f | %.1f | %.1f | %.1f |",
                algorithm,
                sizeLabel,
                vertices,
                edges,
                averageMillis(),
                averageMicros(),
                averageComparisons,
                averageEdgeExaminations,
                averageDsuOperations);
    }
}
