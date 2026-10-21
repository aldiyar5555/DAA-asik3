package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Weighted directed graph stored as an adjacency list.
 * Used for topological ordering, strongly connected components and shortest paths.
 */
public final class DirectedGraph {
    private final int vertexCount;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges;

    /**
     * Creates an empty directed graph with {@code vertexCount} isolated vertices.
     *
     * @param vertexCount number of vertices, must be non-negative
     */
    public DirectedGraph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count must be non-negative");
        }
        this.vertexCount = vertexCount;
        this.adjacency = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
        }
        this.edges = new ArrayList<>();
    }

    /**
     * Adds a directed weighted edge {@code from -> to}.
     *
     * @param from   source vertex
     * @param to     destination vertex
     * @param weight edge weight
     */
    public void addEdge(int from, int to, int weight) {
        validateVertex(from);
        validateVertex(to);
        Edge edge = new Edge(from, to, weight);
        adjacency.get(from).add(edge);
        edges.add(edge);
    }

    /**
     * @return number of vertices
     */
    public int vertexCount() {
        return vertexCount;
    }

    /**
     * @return number of directed edges
     */
    public int edgeCount() {
        return edges.size();
    }

    /**
     * @return unmodifiable edge list
     */
    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Outgoing edges of {@code vertex}.
     *
     * @param vertex vertex index
     * @return unmodifiable adjacency list
     */
    public List<Edge> outgoing(int vertex) {
        validateVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    /**
     * Builds the transpose graph with all edge directions reversed.
     *
     * @return a new graph with reversed arcs
     */
    public DirectedGraph transpose() {
        DirectedGraph reversed = new DirectedGraph(vertexCount);
        for (Edge edge : edges) {
            reversed.addEdge(edge.to(), edge.from(), edge.weight());
        }
        return reversed;
    }

    /**
     * @return {@code true} if any edge has a negative weight
     */
    public boolean hasNegativeWeight() {
        for (Edge edge : edges) {
            if (edge.weight() < 0) {
                return true;
            }
        }
        return false;
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of range: " + vertex);
        }
    }
}
