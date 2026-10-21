package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Weighted undirected graph stored as an adjacency list and an edge list.
 * Suitable for MST and connectivity queries on city infrastructure networks.
 */
public final class UndirectedGraph {
    private final int vertexCount;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges;

    /**
     * Creates an empty undirected graph with {@code vertexCount} isolated vertices.
     *
     * @param vertexCount number of vertices, must be non-negative
     */
    public UndirectedGraph(int vertexCount) {
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
     * Adds an undirected weighted edge. Self-loops are rejected.
     *
     * @param u      first endpoint
     * @param v      second endpoint
     * @param weight edge weight
     */
    public void addEdge(int u, int v, int weight) {
        validateVertex(u);
        validateVertex(v);
        if (u == v) {
            throw new IllegalArgumentException("Self-loops are not allowed in undirected graphs");
        }
        Edge forward = new Edge(u, v, weight);
        Edge backward = new Edge(v, u, weight);
        adjacency.get(u).add(forward);
        adjacency.get(v).add(backward);
        edges.add(forward);
    }

    /**
     * @return number of vertices
     */
    public int vertexCount() {
        return vertexCount;
    }

    /**
     * @return number of undirected edges
     */
    public int edgeCount() {
        return edges.size();
    }

    /**
     * @return unmodifiable list of undirected edges (one record per undirected edge)
     */
    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Adjacent edges of {@code vertex}. Each neighbour appears as an outgoing {@link Edge}.
     *
     * @param vertex vertex index
     * @return unmodifiable adjacency list
     */
    public List<Edge> adjacent(int vertex) {
        validateVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of range: " + vertex);
        }
    }
}
