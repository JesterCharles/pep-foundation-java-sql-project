package com.pep.toolshare;

import com.pep.toolshare.domain.FeePolicies;
import com.pep.toolshare.domain.FeePolicy;
import com.pep.toolshare.domain.GraceFeePolicy;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Member;
import com.pep.toolshare.domain.MemberTier;
import com.pep.toolshare.domain.Receipts;
import com.pep.toolshare.domain.StandardFeePolicy;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 3: records with validation, interfaces, composition over inheritance, factories and StringBuilder. */
class LoanAndFeesCompletionTest {
    private static final LocalDate LOANED = LocalDate.of(2026, 10, 1);
    private static final LocalDate DUE = LocalDate.of(2026, 10, 8);

    private static Loan open() {
        return Loan.open("M-0001", "LT-1003-4", LOANED, DUE);
    }

    @Test
    void recordValidatesItsComponents() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(1, "M-0001", "LT-1003-4", LOANED, LOANED.minusDays(1), null, 0), "due before loan");
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(1, "M-0001", "LT-1003-4", LOANED, DUE, LOANED.minusDays(1), 0), "returned early");
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(1, "M-0001", "LT-1003-4", LOANED, DUE, null, -5), "negative fee");
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(-1, "M-0001", "LT-1003-4", LOANED, DUE, null, 0), "negative id");
        assertThrows(NullPointerException.class, () -> new Loan(1, null, "LT-1003-4", LOANED, DUE, null, 0));
        assertThrows(NullPointerException.class, () -> new Loan(1, "M-0001", "LT-1003-4", null, DUE, null, 0));
        assertEquals(DUE, new Loan(1, "M-0001", "LT-1003-4", LOANED, LOANED, LOANED, 0).returnedOn().plusDays(7),
                "a same-day loan and return is valid");
    }

    @Test
    void daysLateUsesReturnDateOrAsOf() {
        assertEquals(0, open().daysLate(DUE));
        assertEquals(3, open().daysLate(DUE.plusDays(3)));
        assertEquals(0, open().daysLate(LOANED), "never negative");
        Loan returned = open().returned(DUE.plusDays(2), 0);
        assertEquals(2, returned.daysLate(DUE.plusDays(30)), "a returned loan is judged on its return date");
    }

    @Test
    void returningCreatesANewRecord() {
        Loan open = open().withId(7);
        Loan closed = open.returned(DUE, 150);
        assertTrue(open.isOpen(), "the original record is unchanged");
        assertFalse(closed.isOpen());
        assertEquals(7, closed.id());
        assertEquals(150, closed.feeCents());
        assertNotSame(open, closed);
        assertThrows(IllegalStateException.class, () -> closed.returned(DUE, 0));
        assertEquals(new Loan(7, "M-0001", "LT-1003-4", LOANED, DUE, null, 0), open, "records compare by value");
    }

    @Test
    void standardPolicyChargesDailyFeeUpToTheCap() {
        FeePolicy policy = new StandardFeePolicy();
        assertEquals(300, policy.feeCents(open(), ToolCategory.POWER, DUE.plusDays(3)));
        assertEquals(75, policy.feeCents(open(), ToolCategory.HAND, DUE.plusDays(3)));
        assertEquals(2_000, policy.feeCents(open(), ToolCategory.POWER, DUE.plusDays(40)));
        assertEquals(500, new StandardFeePolicy(500).feeCents(open(), ToolCategory.POWER, DUE.plusDays(40)));
        assertEquals(0, policy.feeCents(open(), ToolCategory.POWER, DUE));
        assertThrows(IllegalArgumentException.class, () -> new StandardFeePolicy(-1));
        assertEquals("Standard: daily fee, capped at $20.00", policy.describe());
    }

    @Test
    void gracePolicyWrapsAnyPolicy() {
        FeePolicy grace = new GraceFeePolicy(2, new StandardFeePolicy());
        assertEquals(0, grace.feeCents(open(), ToolCategory.POWER, DUE.plusDays(2)));
        assertEquals(100, grace.feeCents(open(), ToolCategory.POWER, DUE.plusDays(3)));
        FeePolicy flat = (loan, category, asOf) -> loan.daysLate(asOf) > 0 ? 999 : 0;
        assertEquals(0, new GraceFeePolicy(1, flat).feeCents(open(), ToolCategory.HAND, DUE.plusDays(1)));
        assertEquals(999, new GraceFeePolicy(1, flat).feeCents(open(), ToolCategory.HAND, DUE.plusDays(2)));
        assertEquals("2 grace days, then Standard: daily fee, capped at $20.00", grace.describe());
        assertThrows(IllegalArgumentException.class, () -> new GraceFeePolicy(-1, flat));
        assertThrows(NullPointerException.class, () -> new GraceFeePolicy(1, null));
    }

    @Test
    void factoryChoosesThePolicyForEachTier() {
        LocalDate fourDaysLate = DUE.plusDays(4);
        assertEquals(400, FeePolicies.forTier(MemberTier.STARTER).feeCents(open(), ToolCategory.POWER, fourDaysLate));
        assertEquals(300, FeePolicies.forTier(MemberTier.REGULAR).feeCents(open(), ToolCategory.POWER, fourDaysLate));
        assertEquals(200, FeePolicies.forTier(MemberTier.CHAMPION).feeCents(open(), ToolCategory.POWER,
                fourDaysLate));
        assertInstanceOf(StandardFeePolicy.class, FeePolicies.forTier(MemberTier.STARTER));
        assertEquals(0, FeePolicies.waived().feeCents(open(), ToolCategory.POWER, fourDaysLate));
        assertEquals("Custom fee policy", FeePolicies.waived().describe(), "an anonymous class that does not override describe() keeps the default method");
    }

    @Test
    void receiptIsBuiltLineByLine() {
        Member bram = new Member("M-0002", "Bram Okafor", "bram.okafor@example.test", MemberTier.CHAMPION, true,
                "M-0001");
        Tool drill = new Tool("LT-1003-4", "Cordless drill", ToolCategory.POWER, 12_900, ToolStatus.AVAILABLE,
                "B-01");
        Loan loan = new Loan(2, "M-0002", "LT-1003-4", LocalDate.of(2026, 9, 3), LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 14), 200);
        assertEquals("""
                Lantern Tool Library - return receipt
                Member: M-0002 Bram Okafor (CHAMPION)
                Tool: LT-1003-4 Cordless drill
                Loaned: 2026-09-03  Due: 2026-09-10  Returned: 2026-09-14
                Days late: 4
                Policy: 2 grace days, then Standard: daily fee, capped at $20.00
                Fee: $2.00
                """, Receipts.returnReceipt(bram, drill, loan, FeePolicies.forTier(MemberTier.CHAMPION)));
    }
}
