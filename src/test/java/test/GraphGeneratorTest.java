package test;

import benchmark.metrics.GraphGenerator;
import graph.UndirectedGraph;
import graph.connectivity.TopologicalSort;
import graph.mst.KruskalMST;
import graph.mst.PrimMST;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphGeneratorTest {

    @Test
    void connectedGeneratorIsDeterministicForFixedSeed() {
        UndirectedGraph a = GraphGenerator.connectedUndirected(10, 30, new Random(42));
        UndirectedGraph b = GraphGenerator.connectedUndirected(10, 30, new Random(42));
        assertEquals(a.edgeCount(), b.edgeCount());
        assertEquals(a.edges(), b.edges());
    }

    @Test
    void connectedSmallGraphHasExpectedSizeAndMatchingMstWeight() {
        UndirectedGraph graph = GraphGenerator.connectedUndirected(10, 30, new Random(7));
        assertEquals(10, graph.vertexCount());
        assertEquals(30, graph.edgeCount());
        assertTrue(KruskalMST.compute(graph).connected());
        assertEquals(KruskalMST.compute(graph).totalWeight(), PrimMST.compute(graph).totalWeight());
    }

    @Test
    void dagGeneratorIsAcyclic() {
        var dag = GraphGenerator.randomDag(20, 40, new Random(99));
        assertTrue(TopologicalSort.sort(dag).acyclic());
    }
}
