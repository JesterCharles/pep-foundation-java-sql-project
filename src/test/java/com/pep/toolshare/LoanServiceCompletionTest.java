package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.loan.LoanException;
import com.pep.toolshare.loan.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanServiceCompletionTest {
    private ConnectionFactory connections;
    private LoanService loans;

    @BeforeEach
    void resetDatabase() {
        connections = TestDatabase.create();
        loans = new LoanService(connections);
    }

    @Test
    void checkoutUpdatesTheToolAndCreatesOneLoan() throws Exception {
        long loanId = loans.checkout(
                2L,
                3L,
                LocalDate.of(2030, 2, 1),
                LocalDate.of(2030, 2, 8)
        );

        assertTrue(loanId > 0);
        assertEquals("ON_LOAN", TestDatabase.toolStatus(connections, 3L));
        assertEquals(1L, TestDatabase.activeLoansForTool(connections, 3L));
    }

    @Test
    void failedInsertRollsBackTheEarlierToolUpdate() throws Exception {
        assertThrows(LoanException.class, () -> loans.checkout(
                999L,
                5L,
                LocalDate.of(2030, 2, 1),
                LocalDate.of(2030, 2, 8)
        ));

        assertEquals("AVAILABLE", TestDatabase.toolStatus(connections, 5L));
        assertEquals(0L, TestDatabase.activeLoansForTool(connections, 5L));
    }

    @Test
    void unavailableToolIsRejectedWithoutCreatingAnotherLoan() throws Exception {
        assertThrows(LoanException.class, () -> loans.checkout(
                1L,
                2L,
                LocalDate.of(2030, 2, 1),
                LocalDate.of(2030, 2, 8)
        ));

        assertEquals("ON_LOAN", TestDatabase.toolStatus(connections, 2L));
        assertEquals(1L, TestDatabase.activeLoansForTool(connections, 2L));
    }
}
