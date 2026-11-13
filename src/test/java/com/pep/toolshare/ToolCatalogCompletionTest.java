package com.pep.toolshare;

import com.pep.toolshare.domain.Lendable;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolFilters;
import com.pep.toolshare.domain.ToolKit;
import com.pep.toolshare.domain.ToolStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 3: enums with data, inheritance and abstract classes, composition, and functional interfaces. */
class ToolCatalogCompletionTest {
    private static Tool tool(String code, ToolCategory category, long cents, ToolStatus status) {
        return new Tool(code, "Tool " + code, category, cents, status, "A-01");
    }

    @Test
    void categoriesCarryTheirRules() {
        assertEquals("Hand tools", ToolCategory.HAND.label());
        assertEquals(25, ToolCategory.HAND.dailyFeeCents());
        assertEquals(14, ToolCategory.HAND.maxLoanDays());
        assertEquals(100, ToolCategory.POWER.dailyFeeCents());
        assertEquals(7, ToolCategory.POWER.maxLoanDays());
        assertEquals("Garden", ToolCategory.GARDEN.label());
        assertEquals(10, ToolCategory.GARDEN.maxLoanDays());
        assertEquals("Ladders and access", ToolCategory.LADDERS.label());
        assertEquals(75, ToolCategory.LADDERS.dailyFeeCents());
        assertEquals(40, ToolCategory.CLEANING.dailyFeeCents());
    }

    @Test
    void categoryComputesDueDates() {
        assertEquals(LocalDate.of(2026, 10, 22), ToolCategory.POWER.dueDate(LocalDate.of(2026, 10, 15)));
        assertEquals(LocalDate.of(2026, 11, 4), ToolCategory.HAND.dueDate(LocalDate.of(2026, 10, 21)));
    }

    @Test
    void categoryLookupIgnoresCaseAndRejectsUnknownNames() {
        assertSame(ToolCategory.GARDEN, ToolCategory.fromCode(" garden "));
        assertThrows(IllegalArgumentException.class, () -> ToolCategory.fromCode("PLUMBING"));
        assertThrows(IllegalArgumentException.class, () -> ToolCategory.fromCode(" "));
        assertThrows(IllegalArgumentException.class, () -> ToolCategory.fromCode(null));
    }

    @Test
    void toolIsALendableWithSharedCodeAndName() {
        Tool drill = new Tool("LT-1003-4", " Cordless drill ", ToolCategory.POWER, 12_900, ToolStatus.AVAILABLE,
                "B-01");
        Lendable lendable = drill;
        assertEquals("LT-1003-4", lendable.code());
        assertEquals("Cordless drill", lendable.name());
        assertEquals(12_900, lendable.replacementCents());
        assertEquals("LT-1003-4 Cordless drill", lendable.toString());
        assertEquals("B-01", drill.shelf());
        assertTrue(Modifier.isAbstract(Lendable.class.getModifiers()), "Lendable is abstract");
    }

    @Test
    void toolConstructorRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> tool("LT-1003-5", ToolCategory.POWER, 100,
                ToolStatus.AVAILABLE), "wrong check digit");
        assertThrows(IllegalArgumentException.class, () -> tool("LT-1003-4", ToolCategory.POWER, 0,
                ToolStatus.AVAILABLE), "free tool");
        assertThrows(IllegalArgumentException.class, () -> new Tool("LT-1003-4", " ", ToolCategory.POWER, 100,
                ToolStatus.AVAILABLE, "B-01"));
        assertThrows(IllegalArgumentException.class, () -> new Tool("LT-1003-4", "Drill", ToolCategory.POWER, 100,
                ToolStatus.AVAILABLE, "b-1"));
        assertThrows(NullPointerException.class, () -> tool("LT-1003-4", null, 100, ToolStatus.AVAILABLE));
        assertThrows(NullPointerException.class, () -> tool("LT-1003-4", ToolCategory.POWER, 100, null));
    }

    @Test
    void toolStatusFollowsTheLifecycle() {
        Tool tool = tool("LT-1001-2", ToolCategory.HAND, 1_800, ToolStatus.AVAILABLE);
        assertTrue(tool.isAvailable());
        tool.checkOut();
        assertEquals(ToolStatus.ON_LOAN, tool.status());
        assertFalse(tool.isAvailable());
        tool.checkIn(true);
        assertEquals(ToolStatus.IN_REPAIR, tool.status());
        tool.finishRepair();
        assertEquals(ToolStatus.AVAILABLE, tool.status());
        tool.checkOut();
        tool.checkIn(false);
        assertEquals(ToolStatus.AVAILABLE, tool.status());
        tool.retire();
        assertEquals(ToolStatus.RETIRED, tool.status());
    }

    @Test
    void illegalStatusChangesAreRefusedWithoutChangingTheTool() {
        Tool onLoan = tool("LT-1002-3", ToolCategory.HAND, 4_500, ToolStatus.ON_LOAN);
        assertThrows(IllegalStateException.class, onLoan::checkOut);
        assertThrows(IllegalStateException.class, onLoan::retire);
        assertThrows(IllegalStateException.class, onLoan::finishRepair);
        assertEquals(ToolStatus.ON_LOAN, onLoan.status());
        Tool available = tool("LT-1004-5", ToolCategory.POWER, 15_900, ToolStatus.AVAILABLE);
        assertThrows(IllegalStateException.class, () -> available.checkIn(false));
        Tool retired = tool("LT-1011-3", ToolCategory.HAND, 5_400, ToolStatus.RETIRED);
        assertThrows(IllegalStateException.class, retired::retire);
        assertThrows(IllegalStateException.class, retired::checkOut);
        assertEquals(ToolStatus.RETIRED, retired.status());
        Tool repair = tool("LT-1005-6", ToolCategory.POWER, 7_900, ToolStatus.IN_REPAIR);
        repair.retire();
        assertEquals(ToolStatus.RETIRED, repair.status(), "a tool in repair can be retired");
    }

    @Test
    void kitHoldsCopiesOfItsPartsAndAddsTheirCosts() {
        List<Tool> parts = new ArrayList<>(List.of(
                tool("LT-1001-2", ToolCategory.HAND, 1_800, ToolStatus.AVAILABLE),
                tool("LT-1012-4", ToolCategory.HAND, 2_600, ToolStatus.AVAILABLE)));
        ToolKit kit = new ToolKit("LT-2001-3", "Plumbing kit", parts);
        parts.clear();
        assertEquals(2, kit.parts().size(), "the kit copies the list it is given");
        assertThrows(UnsupportedOperationException.class, () -> kit.parts().clear());
        assertEquals(4_400, kit.replacementCents());
        Lendable lendable = kit;
        assertTrue(lendable.isAvailable());
        assertFalse(Tool.class.isAssignableFrom(ToolKit.class), "a kit has tools; it is not a Tool");
    }

    @Test
    void kitNeedsParts() {
        assertThrows(IllegalArgumentException.class, () -> new ToolKit("LT-2001-3", "Empty", List.of()));
        assertThrows(NullPointerException.class, () -> new ToolKit("LT-2001-3", "Empty", null));
    }

    @Test
    void kitIsLentWholeOrNotAtAll() {
        Tool hammer = tool("LT-1001-2", ToolCategory.HAND, 1_800, ToolStatus.AVAILABLE);
        Tool wrench = tool("LT-1012-4", ToolCategory.HAND, 2_600, ToolStatus.ON_LOAN);
        ToolKit kit = new ToolKit("LT-2001-3", "Plumbing kit", List.of(hammer, wrench));
        assertFalse(kit.isAvailable());
        assertThrows(IllegalStateException.class, kit::checkOutAll);
        assertEquals(ToolStatus.AVAILABLE, hammer.status(), "no part changes when the kit cannot be lent");
        wrench.checkIn(false);
        kit.checkOutAll();
        assertEquals(ToolStatus.ON_LOAN, hammer.status());
        assertEquals(ToolStatus.ON_LOAN, wrench.status());
    }

    @Test
    void filtersAreComposablePredicates() {
        List<Tool> tools = List.of(
                tool("LT-1001-2", ToolCategory.HAND, 1_800, ToolStatus.AVAILABLE),
                tool("LT-1002-3", ToolCategory.HAND, 4_500, ToolStatus.ON_LOAN),
                tool("LT-1004-5", ToolCategory.POWER, 15_900, ToolStatus.AVAILABLE),
                tool("LT-1012-4", ToolCategory.HAND, 2_600, ToolStatus.AVAILABLE));
        Predicate<Tool> cheapHandTools = ToolFilters.inCategory(ToolCategory.HAND)
                .and(ToolFilters.available()).and(ToolFilters.cheaperThan(2_600));
        assertEquals(List.of(tools.get(0)), ToolFilters.select(tools, cheapHandTools));
        assertEquals(List.of(tools.get(0), tools.get(2), tools.get(3)),
                ToolFilters.select(tools, ToolFilters.available()));
        assertEquals(List.of(tools.get(1)), ToolFilters.select(tools, ToolFilters.available().negate()));
        assertTrue(ToolFilters.inCategory(ToolCategory.HAND).getClass().isAnonymousClass(),
                "write inCategory as an anonymous class");
    }
}
