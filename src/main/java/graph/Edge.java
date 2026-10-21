package graph;

import java.util.Objects;

/**
 * Weighted directed or undirected edge between two vertices.
 * Vertices are identified by non-negative integer indices.
 */
public final class Edge implements Comparable<Edge> {
    private final int from;
    private final int to;
    private final int weight;

    /**
     * Creates a weighted edge.
     *
     * @param from   source vertex
     * @param to     destination vertex
     * @param weight edge weight
     */
    public Edge(int from, int to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    /**
     * @return source vertex
     */
    public int from() {
        return from;
    }

    /**
     * @return destination vertex
     */
    public int to() {
        return to;
    }

    /**
     * @return edge weight
     */
    public int weight() {
        return weight;
    }

    /**
     * Returns the other endpoint of an undirected edge.
     *
     * @param vertex one endpoint
     * @return the opposite endpoint
     * @throws IllegalArgumentException if {@code vertex} is not an endpoint
     */
    public int other(int vertex) {
        if (vertex == from) {
            return to;
        }
        if (vertex == to) {
            return from;
        }
        throw new IllegalArgumentException("Vertex " + vertex + " is not an endpoint of " + this);
    }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Edge edge)) {
            return false;
        }
        return from == edge.from && to == edge.to && weight == edge.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, weight);
    }

    @Override
    public String toString() {
        return from + " --(" + weight + ")--> " + to;
    }
}
