package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.jdbc.JdbcLoanRepository;
import com.pep.toolshare.lending.NotFoundException;
import com.pep.toolshare.lending.RuleViolationException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Week 5: each checkout and checkin is one transaction that commits as a unit or rolls back completely. */
class LoanTransactionCompletionTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private final ConnectionFactory db = TestDatabase.create();

    private String status(String code) throws SQLException {
        return TestDatabase.text(db, "SELECT status FROM tools WHERE code = '" + code + "'");
    }

    private long loanCount() throws SQLException {
        return TestDatabase.count(db, "SELECT COUNT(*) FROM loans");
    }

    @Test
    void checkoutCommitsBothChanges() throws SQLException {
        Loan loan = new JdbcLoanRepository(db).checkout("M-0006", "LT-1007-8", DAY, DAY.plusDays(10));
        assertEquals(15, loan.id(), "return the generated id");
        assertEquals("ON_LOAN", status("LT-1007-8"), "another connection sees the committed tool status");
        assertEquals(15, loanCount());
        assertEquals("M-0006", TestDatabase.text(db, "SELECT member_id FROM loans WHERE id = 15"));
    }

    @Test
    void checkoutOfAnUnavailableToolChangesNothing() throws SQLException {
        JdbcLoanRepository loans = new JdbcLoanRepository(db);
        assertThrows(RuleViolationException.class, () -> loans.checkout("M-0006", "LT-1003-4", DAY, DAY));
        assertThrows(RuleViolationException.class, () -> loans.checkout("M-0006", "LT-1011-3", DAY, DAY));
        assertThrows(NotFoundException.class, () -> loans.checkout("M-0006", "LT-9999-9", DAY, DAY));
        assertEquals(14, loanCount());
        assertEquals("RETIRED", status("LT-1011-3"));
    }

    @Test
    void checkoutRollsBackWhenTheSecondWriteFails() throws SQLException {
        JdbcLoanRepository failing = new JdbcLoanRepository(FailingConnections.failingOnUpdate(db, 2));
        assertThrows(RuntimeException.class, () -> failing.checkout("M-0006", "LT-1007-8", DAY, DAY.plusDays(10)));
        assertEquals("AVAILABLE", status("LT-1007-8"), "the first write must be rolled back");
        assertEquals(14, loanCount());
    }

    @Test
    void checkoutRollsBackWhenTheLoanCannotBeInserted() throws SQLException {
        JdbcLoanRepository loans = new JdbcLoanRepository(db);
        assertThrows(RuntimeException.class, () -> loans.checkout("M-0999", "LT-1007-8", DAY, DAY.plusDays(10)));
        assertEquals("AVAILABLE", status("LT-1007-8"), "an unknown member must not leave the tool ON_LOAN");
        assertEquals(14, loanCount());
    }

    @Test
    void checkinCommitsBothChanges() throws SQLException {
        Loan closed = new JdbcLoanRepository(db).checkin(9, DAY, 600, ToolStatus.IN_REPAIR);
        assertFalse(closed.isOpen());
        assertEquals(600, closed.feeCents());
        assertEquals("2026-10-15", TestDatabase.text(db, "SELECT returned_on FROM loans WHERE id = 9"));
        assertEquals(600, TestDatabase.count(db, "SELECT fee_cents FROM loans WHERE id = 9"));
        assertEquals("IN_REPAIR", status("LT-1003-4"));
    }

    @Test
    void checkinRefusesUnknownAndReturnedLoans() throws SQLException {
        JdbcLoanRepository loans = new JdbcLoanRepository(db);
        assertThrows(NotFoundException.class, () -> loans.checkin(404, DAY, 0, ToolStatus.AVAILABLE));
        assertThrows(RuleViolationException.class, () -> loans.checkin(1, DAY, 0, ToolStatus.RETIRED));
        assertEquals("AVAILABLE", status("LT-1001-2"), "a refused checkin changes no tool");
        assertEquals("2026-09-14", TestDatabase.text(db, "SELECT returned_on FROM loans WHERE id = 1"));
    }

    @Test
    void checkinRollsBackWhenTheSecondWriteFails() throws SQLException {
        JdbcLoanRepository failing = new JdbcLoanRepository(FailingConnections.failingOnUpdate(db, 2));
        assertThrows(RuntimeException.class, () -> failing.checkin(9, DAY, 600, ToolStatus.AVAILABLE));
        assertEquals(null, TestDatabase.text(db, "SELECT returned_on FROM loans WHERE id = 9"),
                "the loan must still be open after the rollback");
        assertEquals("ON_LOAN", status("LT-1003-4"));
    }
}
