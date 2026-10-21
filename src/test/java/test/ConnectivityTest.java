package test;

import graph.DirectedGraph;
import graph.connectivity.KosarajuSCC;
import graph.connectivity.TopologicalSort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectivityTest {

    @Test
    void topologicalSortOnSingleVertex() {
        DirectedGraph graph = new DirectedGraph(1);
        TopologicalSort.Result result = TopologicalSort.sort(graph);
        assertTrue(result.acyclic());
        assertEquals(List.of(0), result.order());
    }

    @Test
    void topologicalSortOfDag() {
        DirectedGraph dag = new DirectedGraph(4);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 3, 1);
        dag.addEdge(2, 3, 1);
        List<Integer> order = TopologicalSort.sortOrThrow(dag);
        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    void topologicalSortDetectsCycle() {
        DirectedGraph cyclic = new DirectedGraph(3);
        cyclic.addEdge(0, 1, 1);
        cyclic.addEdge(1, 2, 1);
        cyclic.addEdge(2, 0, 1);
        TopologicalSort.Result result = TopologicalSort.sort(cyclic);
        assertFalse(result.acyclic());
        assertTrue(result.order().isEmpty());
        assertThrows(TopologicalSort.CycleDetectedException.class, () -> TopologicalSort.sortOrThrow(cyclic));
    }

    @Test
    void kosarajuFindsThreeComponents() {
        DirectedGraph graph = new DirectedGraph(5);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);
        graph.addEdge(3, 4, 1);

        KosarajuSCC.Result scc = KosarajuSCC.compute(graph);
        assertEquals(3, scc.componentCount());
        assertEquals(scc.componentOf()[0], scc.componentOf()[1]);
        assertEquals(scc.componentOf()[2], scc.componentOf()[3]);
        assertTrue(scc.componentOf()[0] != scc.componentOf()[2]);
        assertTrue(scc.componentOf()[4] != scc.componentOf()[2]);
    }

    @Test
    void kosarajuOnDisconnectedVertices() {
        DirectedGraph graph = new DirectedGraph(3);
        KosarajuSCC.Result scc = KosarajuSCC.compute(graph);
        assertEquals(3, scc.componentCount());
    }
}
