package benchmark.metrics;

import graph.DirectedGraph;
import graph.UndirectedGraph;
import graph.connectivity.KosarajuSCC;
import graph.connectivity.TopologicalSort;
import graph.dsu.DisjointSetUnion;
import graph.mst.KruskalMST;
import graph.mst.MSTResult;
import graph.mst.PrimMST;
import graph.shortestpath.Dijkstra;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Reproducible operational benchmark for the Smart City assignment.
 * Each algorithm is executed {@value #RUNS} times on Small / Medium / Large graphs.
 */
public final class BenchmarkRunner {
    /**
     * Master seed; each (algorithm, size, run) derives a distinct child seed.
     */
    public static final long MASTER_SEED = 20261021L;
    public static final int RUNS = 5;

    private BenchmarkRunner() {
    }

    /**
     * Entry point: prints Markdown tables of average time and operation counts.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        warmup();
        List<BenchmarkResult> results = runAll();
        System.out.println("# Smart City Infrastructure - Benchmark Report");
        System.out.println();
        System.out.println("Master seed: `" + MASTER_SEED + "`, runs per cell: " + RUNS + ".");
        System.out.println();
        System.out.println("| Algorithm | Size | V | E | Avg time (ms) | Avg time (us) | Comparisons | Edge exams | DSU ops |");
        System.out.println("|---|---|---:|---:|---:|---:|---:|---:|---:|");
        for (BenchmarkResult result : results) {
            System.out.println(result.toMarkdownRow());
        }
    }

    /**
     * Runs the suite once so class loading and JIT compilation do not dominate Small timings.
     */
    private static void warmup() {
        runAll();
    }

    /**
     * Executes the full assignment suite.
     *
     * @return one aggregated row per algorithm and size
     */
    public static List<BenchmarkResult> runAll() {
        List<BenchmarkResult> results = new ArrayList<>();
        for (GraphGenerator.Size size : GraphGenerator.Size.values()) {
            results.add(benchmark("Kruskal (union-by-rank)", size, graph ->
                    KruskalMST.compute(graph, DisjointSetUnion.Heuristic.UNION_BY_RANK)));
            results.add(benchmark("Kruskal (union-by-size)", size, graph ->
                    KruskalMST.compute(graph, DisjointSetUnion.Heuristic.UNION_BY_SIZE)));
            results.add(benchmark("Prim (binary heap)", size, PrimMST::compute));
            results.add(benchmarkDirected("Kosaraju SCC", size, KosarajuSCC::compute));
            results.add(benchmarkDag("Topological sort (Kahn)", size, TopologicalSort::sort));
            results.add(benchmarkDijkstra("Dijkstra (binary heap)", size));
        }
        return results;
    }

    private static BenchmarkResult benchmark(String name,
                                             GraphGenerator.Size size,
                                             Function<UndirectedGraph, MSTResult> algorithm) {
        long time = 0;
        long comparisons = 0;
        long edges = 0;
        long dsu = 0;
        for (int run = 0; run < RUNS; run++) {
            UndirectedGraph graph = GraphGenerator.connectedUndirected(
                    size.vertices(),
                    size.edges(),
                    rng(name, size, run));
            MSTResult result = algorithm.apply(graph);
            time += result.elapsedNanos();
            comparisons += result.operations().getComparisons();
            edges += result.operations().getEdgeExaminations();
            dsu += result.operations().getDsuOperations();
        }
        return aggregate(name, size, time, comparisons, edges, dsu);
    }

    private static BenchmarkResult benchmarkDirected(String name,
                                                     GraphGenerator.Size size,
                                                     Function<DirectedGraph, KosarajuSCC.Result> algorithm) {
        long time = 0;
        long comparisons = 0;
        long edges = 0;
        for (int run = 0; run < RUNS; run++) {
            DirectedGraph graph = GraphGenerator.randomDirected(
                    size.vertices(),
                    size.edges(),
                    rng(name, size, run));
            KosarajuSCC.Result result = algorithm.apply(graph);
            time += result.elapsedNanos();
            comparisons += result.operations().getComparisons();
            edges += result.operations().getEdgeExaminations();
        }
        return aggregate(name, size, time, comparisons, edges, 0);
    }

    private static BenchmarkResult benchmarkDag(String name,
                                                GraphGenerator.Size size,
                                                Function<DirectedGraph, TopologicalSort.Result> algorithm) {
        long time = 0;
        long comparisons = 0;
        long edges = 0;
        for (int run = 0; run < RUNS; run++) {
            DirectedGraph graph = GraphGenerator.randomDag(
                    size.vertices(),
                    size.edges(),
                    rng(name, size, run));
            TopologicalSort.Result result = algorithm.apply(graph);
            if (!result.acyclic()) {
                throw new IllegalStateException("DAG generator produced a cycle");
            }
            time += result.elapsedNanos();
            comparisons += result.operations().getComparisons();
            edges += result.operations().getEdgeExaminations();
        }
        return aggregate(name, size, time, comparisons, edges, 0);
    }

    private static BenchmarkResult benchmarkDijkstra(String name, GraphGenerator.Size size) {
        long time = 0;
        long comparisons = 0;
        long edges = 0;
        for (int run = 0; run < RUNS; run++) {
            DirectedGraph graph = GraphGenerator.randomDirected(
                    size.vertices(),
                    size.edges(),
                    rng(name, size, run));
            Dijkstra.Result result = Dijkstra.shortestPaths(graph, 0);
            time += result.elapsedNanos();
            comparisons += result.operations().getComparisons();
            edges += result.operations().getEdgeExaminations();
        }
        return aggregate(name, size, time, comparisons, edges, 0);
    }

    private static BenchmarkResult aggregate(String name,
                                             GraphGenerator.Size size,
                                             long time,
                                             long comparisons,
                                             long edges,
                                             long dsu) {
        double n = RUNS;
        return new BenchmarkResult(
                name,
                size.name(),
                size.vertices(),
                size.edges(),
                RUNS,
                time / n,
                comparisons / n,
                edges / n,
                dsu / n);
    }

    private static Random rng(String algorithm, GraphGenerator.Size size, int run) {
        long mixed = MASTER_SEED
                ^ ((long) algorithm.hashCode() * 0x9E3779B97F4A7C15L)
                ^ ((long) size.ordinal() << 32)
                ^ run;
        return new Random(mixed);
    }
}
