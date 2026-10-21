package test;

import graph.DirectedGraph;
import graph.shortestpath.Dijkstra;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DijkstraTest {

    @Test
    void singleVertexDistanceIsZero() {
        DirectedGraph graph = new DirectedGraph(1);
        Dijkstra.Result result = Dijkstra.shortestPaths(graph, 0);
        assertEquals(0, result.distance()[0]);
        assertTrue(result.reachable(0));
    }

    @Test
    void unreachableVerticesStayAtSentinel() {
        DirectedGraph graph = new DirectedGraph(4);
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 2);
        Dijkstra.Result result = Dijkstra.shortestPaths(graph, 0);
        assertEquals(0, result.distance()[0]);
        assertEquals(5, result.distance()[1]);
        assertEquals(7, result.distance()[2]);
        assertEquals(Dijkstra.UNREACHABLE, result.distance()[3]);
        assertFalse(result.reachable(3));
        assertEquals(-1, result.parent()[3]);
    }

    @Test
    void shortestPathPrefersCheaperRoute() {
        DirectedGraph graph = new DirectedGraph(4);
        graph.addEdge(0, 1, 10);
        graph.addEdge(0, 2, 3);
        graph.addEdge(2, 1, 1);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 8);
        Dijkstra.Result result = Dijkstra.shortestPaths(graph, 0);
        assertEquals(6, result.distance()[3]);
        assertEquals(1, result.parent()[3]);
        assertEquals(2, result.parent()[1]);
    }

    @Test
    void negativeWeightIsRejected() {
        DirectedGraph graph = new DirectedGraph(2);
        graph.addEdge(0, 1, -4);
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.shortestPaths(graph, 0));
    }
}
