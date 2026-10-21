package test;

import graph.Edge;
import graph.UndirectedGraph;
import graph.mst.KruskalMST;
import graph.mst.MSTResult;
import graph.mst.PrimMST;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MstTest {

    @Test
    void singleVertexHasEmptyTreeAndIsConnected() {
        UndirectedGraph graph = new UndirectedGraph(1);
        MSTResult kruskal = KruskalMST.compute(graph);
        MSTResult prim = PrimMST.compute(graph);
        assertTrue(kruskal.connected());
        assertTrue(prim.connected());
        assertEquals(0, kruskal.totalWeight());
        assertEquals(0, prim.totalWeight());
        assertTrue(kruskal.edges().isEmpty());
        assertTrue(prim.edges().isEmpty());
        assertEquals(1, kruskal.componentCount());
    }

    @Test
    void disconnectedGraphProducesForest() {
        UndirectedGraph graph = new UndirectedGraph(4);
        graph.addEdge(0, 1, 3);
        graph.addEdge(2, 3, 5);
        MSTResult kruskal = KruskalMST.compute(graph);
        MSTResult prim = PrimMST.compute(graph);
        assertFalse(kruskal.connected());
        assertFalse(prim.connected());
        assertEquals(2, kruskal.componentCount());
        assertEquals(2, prim.componentCount());
        assertEquals(8, kruskal.totalWeight());
        assertEquals(8, prim.totalWeight());
        assertEquals(2, kruskal.edges().size());
        assertEquals(2, prim.edges().size());
    }

    @Test
    void kruskalAndPrimAgreeOnClassicTextbookGraph() {
        UndirectedGraph graph = textbookGraph();
        MSTResult kruskal = KruskalMST.compute(graph);
        MSTResult prim = PrimMST.compute(graph);
        assertTrue(kruskal.connected());
        assertTrue(prim.connected());
        assertEquals(kruskal.totalWeight(), prim.totalWeight());
        assertEquals(graph.vertexCount() - 1, kruskal.edges().size());
        assertEquals(graph.vertexCount() - 1, prim.edges().size());
        assertEquals(37, kruskal.totalWeight());
    }

    @Test
    void duplicateWeightsAllowMultipleMinimumSpanningTrees() {
        UndirectedGraph graph = new UndirectedGraph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 0, 1);
        graph.addEdge(0, 2, 2);

        MSTResult kruskal = KruskalMST.compute(graph);
        MSTResult prim = PrimMST.compute(graph);
        assertEquals(3, kruskal.totalWeight());
        assertEquals(3, prim.totalWeight());
        assertEquals(3, kruskal.edges().size());

        Set<String> kruskalEdges = undirectedKeys(kruskal);
        Set<String> primEdges = undirectedKeys(prim);
        assertEquals(3, kruskalEdges.size());
        assertEquals(3, primEdges.size());
        // Both are valid MSTs of weight 3; they may differ when weights collide.
        assertTrue(kruskalEdges.contains("0-1") || kruskalEdges.contains("0-3"));
    }

    private static UndirectedGraph textbookGraph() {
        UndirectedGraph graph = new UndirectedGraph(9);
        int[][] edges = {
                {0, 1, 4}, {0, 7, 8}, {1, 2, 8}, {1, 7, 11},
                {2, 3, 7}, {2, 8, 2}, {2, 5, 4}, {3, 4, 9},
                {3, 5, 14}, {4, 5, 10}, {5, 6, 2}, {6, 7, 1},
                {6, 8, 6}, {7, 8, 7}
        };
        for (int[] e : edges) {
            graph.addEdge(e[0], e[1], e[2]);
        }
        return graph;
    }

    private static Set<String> undirectedKeys(MSTResult result) {
        Set<String> keys = new HashSet<>();
        for (Edge edge : result.edges()) {
            int a = Math.min(edge.from(), edge.to());
            int b = Math.max(edge.from(), edge.to());
            keys.add(a + "-" + b);
        }
        return keys;
    }
}
