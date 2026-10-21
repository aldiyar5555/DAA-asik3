package graph.dsu;

import benchmark.metrics.OperationCounter;

/**
 * Disjoint Set Union (Union-Find) with path compression and union by rank.
 * Optional union-by-size is available through {@link Heuristic}.
 *
 * <p>Amortized time of {@code find}/{@code union} is nearly constant:
 * {@code O(α(n))}, where {@code α} is the inverse Ackermann function.
 */
public final class DisjointSetUnion {
    /**
     * Linking heuristic used when two trees are merged.
     */
    public enum Heuristic {
        /** Attach the shallower tree under the deeper one. */
        UNION_BY_RANK,
        /** Attach the smaller tree under the larger one. */
        UNION_BY_SIZE
    }

    private final int[] parent;
    private final int[] rank;
    private final int[] size;
    private final Heuristic heuristic;
    private final OperationCounter counter;
    private int componentCount;

    /**
     * Creates {@code n} singleton sets with union-by-rank and path compression.
     *
     * @param n number of elements
     */
    public DisjointSetUnion(int n) {
        this(n, Heuristic.UNION_BY_RANK, new OperationCounter());
    }

    /**
     * Creates {@code n} singleton sets with a chosen linking heuristic.
     *
     * @param n         number of elements
     * @param heuristic linking heuristic
     * @param counter   operation accumulator, must not be {@code null}
     */
    public DisjointSetUnion(int n, Heuristic heuristic, OperationCounter counter) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }
        if (heuristic == null) {
            throw new IllegalArgumentException("heuristic must not be null");
        }
        if (counter == null) {
            throw new IllegalArgumentException("counter must not be null");
        }
        this.parent = new int[n];
        this.rank = new int[n];
        this.size = new int[n];
        this.heuristic = heuristic;
        this.counter = counter;
        this.componentCount = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
            size[i] = 1;
        }
    }

    /**
     * Finds the representative of the set containing {@code i} with path compression.
     *
     * @param i element index
     * @return root of the tree
     */
    public int find(int i) {
        counter.dsuFind();
        validate(i);
        if (parent[i] != i) {
            counter.comparison();
            int root = find(parent[i]);
            if (parent[i] != root) {
                parent[i] = root;
                counter.dsuParentUpdate();
            }
            return root;
        }
        counter.comparison();
        return i;
    }

    /**
     * Unites the sets containing {@code i} and {@code j}.
     *
     * @param i first element
     * @param j second element
     * @return {@code true} if the sets were disjoint and a merge occurred
     */
    public boolean union(int i, int j) {
        counter.dsuUnion();
        int rootI = find(i);
        int rootJ = find(j);
        counter.comparison();
        if (rootI == rootJ) {
            return false;
        }
        if (heuristic == Heuristic.UNION_BY_SIZE) {
            linkBySize(rootI, rootJ);
        } else {
            linkByRank(rootI, rootJ);
        }
        componentCount--;
        return true;
    }

    /**
     * @return remaining number of disjoint components
     */
    public int componentCount() {
        return componentCount;
    }

    /**
     * @return {@code true} if {@code a} and {@code b} belong to the same set
     */
    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    /**
     * @return operation counter used by this structure
     */
    public OperationCounter counter() {
        return counter;
    }

    private void linkByRank(int rootI, int rootJ) {
        counter.comparison();
        if (rank[rootI] < rank[rootJ]) {
            parent[rootI] = rootJ;
            size[rootJ] += size[rootI];
            counter.dsuParentUpdate();
        } else if (rank[rootI] > rank[rootJ]) {
            counter.comparison();
            parent[rootJ] = rootI;
            size[rootI] += size[rootJ];
            counter.dsuParentUpdate();
        } else {
            counter.comparison();
            parent[rootJ] = rootI;
            size[rootI] += size[rootJ];
            rank[rootI]++;
            counter.dsuParentUpdate();
        }
    }

    private void linkBySize(int rootI, int rootJ) {
        counter.comparison();
        if (size[rootI] < size[rootJ]) {
            parent[rootI] = rootJ;
            size[rootJ] += size[rootI];
            counter.dsuParentUpdate();
        } else {
            parent[rootJ] = rootI;
            size[rootI] += size[rootJ];
            counter.dsuParentUpdate();
        }
    }

    private void validate(int i) {
        if (i < 0 || i >= parent.length) {
            throw new IllegalArgumentException("Index out of range: " + i);
        }
    }
}
