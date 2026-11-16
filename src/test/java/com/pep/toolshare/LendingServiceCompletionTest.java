package com.pep.toolshare;

import com.pep.toolshare.config.SampleData;
import com.pep.toolshare.config.Stores;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.lending.InvalidRequestException;
import com.pep.toolshare.lending.LendingService;
import com.pep.toolshare.lending.NotFoundException;
import com.pep.toolshare.lending.RuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 4: the lending rules, wired through constructor injection, with meaningful exceptions and logs. */
class LendingServiceCompletionTest {
    private final Stores stores = SampleData.inMemory();
    private final LendingService service = Fixtures.service(stores);

    @Test
    void toolsAreListedAllOrByCategory() {
        assertEquals(12, service.tools(null).size());
        assertEquals(12, service.tools(" ").size());
        assertEquals(List.of("LT-1006-7", "LT-1007-8", "LT-1008-9"),
                service.tools("garden").stream().map(Tool::code).toList());
        assertThrows(InvalidRequestException.class, () -> service.tools("PLUMBING"));
    }

    @Test
    void toolAndMemberLookupsNormalizeAndReportMissingOnes() {
        assertEquals("Step ladder", service.tool(" lt-1009-1 ").name());
        assertThrows(InvalidRequestException.class, () -> service.tool("LT-1009-2"));
        assertThrows(NotFoundException.class, () -> service.tool("LT-9999-9"));
        assertEquals("Fern Ito", service.member("M-0006").fullName());
        assertThrows(NotFoundException.class, () -> service.member("M-0404"));
        assertThrows(InvalidRequestException.class, () -> service.member(" "));
    }

    @Test
    void checkoutLendsForTheCategoryLoanPeriod() {
        Loan loan = service.checkout("M-0006", "lt-1004-5");
        assertEquals("LT-1004-5", loan.toolCode());
        assertEquals(Fixtures.TODAY, loan.loanedOn());
        assertEquals(LocalDate.of(2026, 10, 22), loan.dueOn(), "POWER tools are lent for 7 days");
        assertEquals(ToolStatus.ON_LOAN, service.tool("LT-1004-5").status());
        assertEquals(List.of(loan), service.openLoans("M-0006"));
    }

    @Test
    void checkoutRefusesBrokenRules() {
        assertThrows(RuleViolationException.class, () -> service.checkout("M-0005", "LT-1004-5"), "inactive member");
        assertThrows(RuleViolationException.class, () -> service.checkout("M-0006", "LT-1003-4"), "on loan");
        assertThrows(RuleViolationException.class, () -> service.checkout("M-0006", "LT-1011-3"), "retired");
        assertThrows(NotFoundException.class, () -> service.checkout("M-0404", "LT-1004-5"));
        assertThrows(NotFoundException.class, () -> service.checkout("M-0006", "LT-9999-9"));
        assertThrows(InvalidRequestException.class, () -> service.checkout("M-0006", null));
        assertThrows(InvalidRequestException.class, () -> service.checkout(null, "LT-1004-5"));
        assertEquals(14, service.loans().size(), "refused checkouts record nothing");
    }

    @Test
    void aMemberMayHoldAtMostThreeLoans() {
        service.checkout("M-0006", "LT-1001-2");
        service.checkout("M-0006", "LT-1004-5");
        service.checkout("M-0006", "LT-1007-8");
        RuleViolationException refused = assertThrows(RuleViolationException.class,
                () -> service.checkout("M-0006", "LT-1008-9"));
        assertTrue(refused.getMessage().contains("3"), refused.getMessage());
        assertEquals(ToolStatus.AVAILABLE, service.tool("LT-1008-9").status());
    }

    @Test
    void checkinChargesTheTierPolicy() {
        assertEquals(600, service.checkin(9, false).feeCents(), "STARTER, POWER, 6 days late");
        assertEquals(0, service.checkin(10, false).feeCents(), "CHAMPION, 2 days late inside 2 grace days");
        assertEquals(225, service.checkin(11, false).feeCents(), "STARTER, LADDERS, 3 days late");
        assertEquals(0, service.checkin(8, false).feeCents(), "returned on the due date");
        assertEquals(ToolStatus.AVAILABLE, service.tool("LT-1002-3").status());
    }

    @Test
    void damagedReturnsGoToRepair() {
        Loan closed = service.checkin(9, true);
        assertFalse(closed.isOpen());
        assertEquals(ToolStatus.IN_REPAIR, service.tool("LT-1003-4").status());
        assertThrows(RuleViolationException.class, () -> service.checkin(9, false));
        assertThrows(NotFoundException.class, () -> service.checkin(404, false));
    }

    @Test
    void retireAndReceiptFollowTheRules() {
        service.retire("LT-1008-9");
        assertEquals(ToolStatus.RETIRED, service.tool("LT-1008-9").status());
        assertThrows(RuleViolationException.class, () -> service.retire("LT-1008-9"));
        assertThrows(RuleViolationException.class, () -> service.retire("LT-1003-4"));
        assertTrue(service.receipt(2).contains("Fee: $2.00"), service.receipt(2));
        assertThrows(RuleViolationException.class, () -> service.receipt(9), "an open loan has no receipt yet");
    }

    @Test
    void checkoutIsLoggedAtInfoWithoutPersonalData() {
        String log = LogCapture.during(() -> service.checkout("M-0006", "LT-1004-5"));
        assertTrue(log.contains("INFO"), "log a successful checkout at INFO level: " + log);
        assertTrue(log.contains("LT-1004-5") && log.contains("M-0006") && log.contains("15"),
                "the log line names the loan id, tool code and member id: " + log);
        assertFalse(log.contains("fern.ito@example.test") || log.contains("Fern Ito"),
                "never log a member's name or email: " + log);
    }

    @Test
    void refusedCheckoutIsLoggedAtWarn() {
        String log = LogCapture.during(() -> service.checkout("M-0006", "LT-1003-4"));
        assertTrue(log.contains("WARN") && log.contains("LT-1003-4"), "log a refused checkout at WARN: " + log);
        assertFalse(log.contains("@example.test"), "never log an email: " + log);
    }
}
