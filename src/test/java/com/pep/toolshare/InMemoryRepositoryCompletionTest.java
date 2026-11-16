package com.pep.toolshare;

import com.pep.toolshare.config.SampleData;
import com.pep.toolshare.config.Stores;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.lending.InMemoryLoanRepository;
import com.pep.toolshare.lending.InMemoryMemberRepository;
import com.pep.toolshare.lending.InMemoryToolRepository;
import com.pep.toolshare.lending.MemberRepository;
import com.pep.toolshare.lending.NotFoundException;
import com.pep.toolshare.lending.RuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 4: repositories behind interfaces, kept in memory with maps and lists. */
class InMemoryRepositoryCompletionTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    private static List<String> codes(List<Tool> tools) {
        return tools.stream().map(Tool::code).toList();
    }

    @Test
    void toolsAreListedByCode() {
        InMemoryToolRepository tools = new InMemoryToolRepository();
        tools.add(new Tool("LT-1010-2", "Extension ladder", ToolCategory.LADDERS, 18_900, ToolStatus.AVAILABLE,
                "D-02"));
        tools.add(new Tool("LT-1001-2", "Claw hammer", ToolCategory.HAND, 1_800, ToolStatus.AVAILABLE, "A-01"));
        tools.add(new Tool("LT-1004-5", "Circular saw", ToolCategory.POWER, 15_900, ToolStatus.AVAILABLE, "B-02"));
        assertEquals(List.of("LT-1001-2", "LT-1004-5", "LT-1010-2"), codes(tools.findAll()));
        assertEquals(List.of("LT-1004-5"), codes(tools.findByCategory(ToolCategory.POWER)));
        assertEquals(List.of(), tools.findByCategory(ToolCategory.CLEANING));
    }

    @Test
    void toolLookupAddAndStatusUpdate() {
        Stores stores = SampleData.inMemory();
        assertEquals("Cordless drill", stores.tools().findByCode("LT-1003-4").name());
        assertNull(stores.tools().findByCode("LT-9999-9"));
        assertThrows(RuleViolationException.class, () -> stores.tools().add(new Tool("LT-1001-2", "Copy",
                ToolCategory.HAND, 100, ToolStatus.AVAILABLE, "A-01")));
        assertTrue(stores.tools().updateStatus("LT-1004-5", ToolStatus.IN_REPAIR));
        assertEquals(ToolStatus.IN_REPAIR, stores.tools().findByCode("LT-1004-5").status());
        assertFalse(stores.tools().updateStatus("LT-9999-9", ToolStatus.RETIRED));
        assertEquals(12, stores.tools().findAll().size());
    }

    @Test
    void checkoutOpensALoanAndMarksTheTool() {
        Stores stores = SampleData.inMemory();
        Loan loan = stores.loans().checkout("M-0006", "LT-1007-8", DAY, DAY.plusDays(10));
        assertEquals(15, loan.id(), "ids continue after the sample loans 1-14");
        assertTrue(loan.isOpen());
        assertEquals(ToolStatus.ON_LOAN, stores.tools().findByCode("LT-1007-8").status());
        assertEquals(loan, stores.loans().findById(15));
        assertEquals(List.of(loan), stores.loans().findOpenByMember("M-0006"));
        Loan next = stores.loans().checkout("M-0006", "LT-1008-9", DAY, DAY.plusDays(10));
        assertEquals(16, next.id());
    }

    @Test
    void checkoutRefusesUnknownOrUnavailableToolsWithoutChanges() {
        Stores stores = SampleData.inMemory();
        assertThrows(NotFoundException.class, () -> stores.loans().checkout("M-0006", "LT-9999-9", DAY, DAY));
        assertThrows(RuleViolationException.class, () -> stores.loans().checkout("M-0006", "LT-1003-4", DAY, DAY));
        assertThrows(RuleViolationException.class, () -> stores.loans().checkout("M-0006", "LT-1005-6", DAY, DAY));
        assertEquals(14, stores.loans().findAll().size(), "a refused checkout records nothing");
        assertEquals(ToolStatus.IN_REPAIR, stores.tools().findByCode("LT-1005-6").status());
    }

    @Test
    void checkinClosesTheLoanAndSetsTheToolStatus() {
        Stores stores = SampleData.inMemory();
        Loan closed = stores.loans().checkin(9, DAY, 300, ToolStatus.IN_REPAIR);
        assertFalse(closed.isOpen());
        assertEquals(DAY, closed.returnedOn());
        assertEquals(300, closed.feeCents());
        assertEquals(closed, stores.loans().findById(9));
        assertEquals(ToolStatus.IN_REPAIR, stores.tools().findByCode("LT-1003-4").status());
        assertThrows(RuleViolationException.class, () -> stores.loans().checkin(9, DAY, 0, ToolStatus.AVAILABLE));
        assertThrows(NotFoundException.class, () -> stores.loans().checkin(99, DAY, 0, ToolStatus.AVAILABLE));
    }

    @Test
    void loansAreListedById() {
        Stores stores = SampleData.inMemory();
        List<Loan> all = stores.loans().findAll();
        assertEquals(14, all.size());
        for (int index = 0; index < all.size(); index++) {
            assertEquals(index + 1, all.get(index).id());
        }
        assertEquals(List.of(8L), stores.loans().findOpenByMember("M-0001").stream().map(Loan::id).toList());
        assertEquals(List.of(), stores.loans().findOpenByMember("M-0006"));
        assertNull(stores.loans().findById(404));
    }

    @Test
    void membersWithLoansCannotBeDeleted() {
        MemberRepository members = SampleData.inMemory().members();
        assertThrows(RuleViolationException.class, () -> members.delete("M-0001"));
        assertTrue(members.delete("M-0006"));
        assertFalse(members.delete("M-0006"));
        InMemoryToolRepository tools = new InMemoryToolRepository();
        InMemoryLoanRepository loans = new InMemoryLoanRepository(tools, new InMemoryMemberRepository());
        assertEquals(List.of(), loans.findAll());
    }
}
