package com.pep.toolshare;

import com.pep.toolshare.config.SampleData;
import com.pep.toolshare.config.Stores;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.reports.InventoryAnalytics;
import com.pep.toolshare.reports.ToolUsage;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Week 6: lambdas, method references, Optional and stream collectors over the loaded sample data. */
class InventoryAnalyticsCompletionTest {
    private final Stores stores = SampleData.inMemory();
    private final List<Tool> tools = stores.tools().findAll();
    private final List<Loan> loans = stores.loans().findAll();

    private static <T> List<T> reversed(List<T> list) {
        List<T> copy = new ArrayList<>(list);
        Collections.reverse(copy);
        return copy;
    }

    @Test
    void indexByCodeRejectsDuplicates() {
        Map<String, Tool> index = InventoryAnalytics.indexByCode(tools);
        assertEquals(12, index.size());
        assertEquals("Wheelbarrow", index.get("LT-1008-9").name());
        List<Tool> doubled = new ArrayList<>(tools);
        doubled.add(tools.get(0));
        assertThrows(IllegalStateException.class, () -> InventoryAnalytics.indexByCode(doubled));
    }

    @Test
    void loansPerCategoryUsesCategoryOrder() {
        Map<ToolCategory, Long> perCategory = InventoryAnalytics.loansPerCategory(reversed(loans), tools);
        assertEquals("{HAND=4, POWER=4, GARDEN=3, LADDERS=3}", perCategory.toString());
        List<Loan> withUnknown = new ArrayList<>(loans);
        withUnknown.add(Loan.open("M-0001", "LT-9999-9", Fixtures.TODAY, Fixtures.TODAY));
        assertEquals(perCategory, InventoryAnalytics.loansPerCategory(withUnknown, tools));
    }

    @Test
    void mostBorrowedSortsByCountThenCode() {
        assertEquals(List.of(new ToolUsage("LT-1003-4", "Cordless drill", 3),
                        new ToolUsage("LT-1001-2", "Claw hammer", 2),
                        new ToolUsage("LT-1006-7", "Hedge trimmer", 2)),
                InventoryAnalytics.mostBorrowed(reversed(loans), reversed(tools), 3));
        assertEquals(9, InventoryAnalytics.mostBorrowed(loans, tools, 20).size(), "only tools with loans");
        assertEquals(List.of(), InventoryAnalytics.mostBorrowed(List.of(), tools, 3));
    }

    @Test
    void longestOverdueIsOptional() {
        assertEquals(9, InventoryAnalytics.longestOverdue(reversed(loans), Fixtures.TODAY).orElseThrow().id());
        assertEquals(Optional.empty(), InventoryAnalytics.longestOverdue(loans, LocalDate.of(2026, 10, 9)));
        Loan tieA = new Loan(30, "M-0001", "LT-1001-2", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5), null, 0);
        Loan tieB = new Loan(31, "M-0002", "LT-1004-5", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5), null, 0);
        assertEquals(30, InventoryAnalytics.longestOverdue(List.of(tieB, tieA), Fixtures.TODAY).orElseThrow().id());
    }

    @Test
    void feesByMemberAreSummedInIdOrder() {
        assertEquals("{M-0001=0, M-0002=200, M-0003=150, M-0004=225, M-0005=0}",
                InventoryAnalytics.feesByMember(reversed(loans)).toString());
    }

    @Test
    void toolCodesByShelfAreGroupedAndSorted() {
        List<Tool> extra = new ArrayList<>(reversed(tools));
        extra.add(new Tool("LT-1000-1", "Mallet", ToolCategory.HAND, 900, ToolStatus.AVAILABLE, "A-01"));
        Map<?, List<String>> byShelf = InventoryAnalytics.toolCodesByShelf(extra);
        assertEquals("[A-01, A-02, A-03, B-01, B-02, B-03, C-01, C-02, C-03, D-01, D-02]",
                byShelf.keySet().toString());
        assertEquals(List.of("LT-1000-1", "LT-1001-2", "LT-1012-4"), byShelf.values().iterator().next());
    }

    @Test
    void analyticsReadTheirInputsThroughStreams() {
        List<Tool> streamTools = new StreamOnlyList<>(tools);
        List<Loan> streamLoans = new StreamOnlyList<>(loans);
        assertEquals(InventoryAnalytics.indexByCode(tools), InventoryAnalytics.indexByCode(streamTools));
        assertEquals(InventoryAnalytics.loansPerCategory(loans, tools),
                InventoryAnalytics.loansPerCategory(streamLoans, streamTools));
        assertEquals(InventoryAnalytics.mostBorrowed(loans, tools, 3),
                InventoryAnalytics.mostBorrowed(streamLoans, streamTools, 3));
        assertEquals(InventoryAnalytics.longestOverdue(loans, Fixtures.TODAY),
                InventoryAnalytics.longestOverdue(streamLoans, Fixtures.TODAY));
        assertEquals(InventoryAnalytics.feesByMember(loans), InventoryAnalytics.feesByMember(streamLoans));
        assertEquals(InventoryAnalytics.toolCodesByShelf(tools), InventoryAnalytics.toolCodesByShelf(streamTools));
    }

    /** A list that can only be read with stream(): loops, get, forEach and toArray stop with a hint. */
    private static final class StreamOnlyList<T> extends ArrayList<T> {
        StreamOnlyList(List<T> items) {
            super(items);
        }

        private static AssertionError loop() {
            return new AssertionError("InventoryAnalytics must read its lists with stream() pipelines, not with "
                    + "for/while loops, get(i), forEach or a copy of the list");
        }

        @Override
        public Iterator<T> iterator() {
            throw loop();
        }

        @Override
        public ListIterator<T> listIterator() {
            throw loop();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            throw loop();
        }

        @Override
        public T get(int index) {
            throw loop();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            throw loop();
        }

        @Override
        public Object[] toArray() {
            throw loop();
        }

        @Override
        public <A> A[] toArray(A[] array) {
            throw loop();
        }
    }
}
