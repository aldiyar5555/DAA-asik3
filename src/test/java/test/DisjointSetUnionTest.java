package test;

import benchmark.metrics.OperationCounter;
import graph.dsu.DisjointSetUnion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisjointSetUnionTest {

    @Test
    void singleElementIsItsOwnRoot() {
        DisjointSetUnion dsu = new DisjointSetUnion(1);
        assertEquals(0, dsu.find(0));
        assertEquals(1, dsu.componentCount());
        assertFalse(dsu.union(0, 0));
    }

    @Test
    void unionByRankMergesDistinctSets() {
        DisjointSetUnion dsu = new DisjointSetUnion(4);
        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(2, 3));
        assertFalse(dsu.union(1, 0));
        assertTrue(dsu.connected(0, 1));
        assertFalse(dsu.connected(0, 2));
        assertEquals(2, dsu.componentCount());
        assertTrue(dsu.union(1, 3));
        assertEquals(1, dsu.componentCount());
        assertTrue(dsu.connected(0, 3));
    }

    @Test
    void unionBySizeAndPathCompressionRecordOperations() {
        OperationCounter counter = new OperationCounter();
        DisjointSetUnion dsu = new DisjointSetUnion(5, DisjointSetUnion.Heuristic.UNION_BY_SIZE, counter);
        dsu.union(0, 1);
        dsu.union(1, 2);
        dsu.union(3, 4);
        dsu.find(2);
        assertTrue(dsu.connected(0, 2));
        assertTrue(counter.getDsuOperations() > 0);
        assertTrue(counter.getComparisons() > 0);
    }
}
