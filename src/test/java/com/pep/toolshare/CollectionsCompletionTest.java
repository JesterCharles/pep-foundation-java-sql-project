package com.pep.toolshare;

import com.pep.toolshare.domain.ShelfLocation;
import com.pep.toolshare.reports.ReturnCart;
import com.pep.toolshare.reports.WaitList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 6: equals/hashCode/compareTo contracts, queues, stacks and choosing the right collection. */
class CollectionsCompletionTest {
    @Test
    void shelfLocationsWithTheSameAisleAndBayAreEqual() {
        ShelfLocation a = ShelfLocation.parse("A-01");
        ShelfLocation same = new ShelfLocation('A', 1);
        assertEquals(a, same);
        assertEquals(same, a, "equals must be symmetric");
        assertEquals(a.hashCode(), same.hashCode(), "equal objects need equal hash codes");
        assertNotEquals(a, ShelfLocation.parse("A-02"));
        assertNotEquals(a, ShelfLocation.parse("B-01"));
        assertNotEquals(a, null);
        assertNotEquals(a, "A-01");
    }

    @Test
    void shelfLocationsWorkAsHashKeysAndSetMembers() {
        Set<ShelfLocation> set = new HashSet<>(List.of(ShelfLocation.parse("A-01"), ShelfLocation.parse("A-01"),
                ShelfLocation.parse("B-02")));
        assertEquals(2, set.size(), "a HashSet keeps one copy of equal locations");
        Map<ShelfLocation, String> map = new HashMap<>();
        map.put(ShelfLocation.parse("C-03"), "wheelbarrow");
        assertEquals("wheelbarrow", map.get(new ShelfLocation('C', 3)), "look up with a different but equal key");
    }

    @Test
    void shelfLocationsSortByAisleThenBay() {
        List<ShelfLocation> sorted = new ArrayList<>(new TreeSet<>(List.of(ShelfLocation.parse("B-01"),
                ShelfLocation.parse("A-10"), ShelfLocation.parse("A-02"), ShelfLocation.parse("A-10"))));
        assertEquals("[A-02, A-10, B-01]", sorted.toString());
        assertTrue(ShelfLocation.parse("A-02").compareTo(ShelfLocation.parse("A-10")) < 0);
        assertTrue(ShelfLocation.parse("B-01").compareTo(ShelfLocation.parse("A-99")) > 0);
        assertEquals(0, ShelfLocation.parse("D-07").compareTo(new ShelfLocation('D', 7)));
    }

    @Test
    void waitListIsFirstInFirstOutWithoutDuplicates() {
        WaitList<String> list = new WaitList<>();
        assertEquals(Optional.empty(), list.next());
        assertEquals(Optional.empty(), list.peek());
        assertTrue(list.join("M-0003"));
        assertTrue(list.join("M-0001"));
        assertFalse(list.join("M-0003"), "already waiting");
        assertTrue(list.join("M-0004"));
        assertEquals(3, list.size());
        assertEquals(List.of("M-0003", "M-0001", "M-0004"), list.snapshot());
        assertEquals(2, list.positionOf("M-0001"));
        assertEquals(0, list.positionOf("M-0006"));
        assertEquals(Optional.of("M-0003"), list.peek());
        assertEquals(Optional.of("M-0003"), list.next());
        assertEquals(1, list.positionOf("M-0001"));
        assertTrue(list.join("M-0003"), "after leaving the line a member may join again, at the back");
        assertEquals(List.of("M-0001", "M-0004", "M-0003"), list.snapshot());
        assertThrows(NullPointerException.class, () -> list.join(null));
    }

    @Test
    void waitListSnapshotIsACopy() {
        WaitList<String> list = new WaitList<>();
        list.join("M-0002");
        List<String> snapshot = list.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("M-0009"));
        list.join("M-0004");
        assertEquals(List.of("M-0002"), snapshot, "a snapshot does not change later");
    }

    @Test
    void waitListHandlesManyEntries() {
        WaitList<Integer> list = new WaitList<>();
        for (int i = 0; i < 50_000; i++) {
            list.join(i);
        }
        for (int i = 0; i < 50_000; i++) {
            assertFalse(list.join(i), "the duplicate check must stay correct for large lists");
        }
        assertEquals(50_000, list.size());
        assertEquals(Optional.of(0), list.next());
    }

    @Test
    void returnCartIsLastInFirstOut() {
        ReturnCart cart = new ReturnCart();
        assertNull(cart.peekTop());
        assertThrows(NoSuchElementException.class, cart::takeTop);
        cart.drop("LT-1001-2");
        cart.drop("LT-1003-4");
        cart.drop("LT-1009-1");
        assertEquals(List.of("LT-1009-1", "LT-1003-4", "LT-1001-2"), cart.topToBottom());
        assertEquals("LT-1009-1", cart.peekTop());
        assertEquals("LT-1009-1", cart.takeTop());
        assertEquals("LT-1003-4", cart.takeTop());
        assertEquals(1, cart.size());
        assertThrows(NullPointerException.class, () -> cart.drop(null));
        assertThrows(UnsupportedOperationException.class, () -> cart.topToBottom().add("x"));
    }
}
