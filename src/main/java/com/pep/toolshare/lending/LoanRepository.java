package com.pep.toolshare.lending;

import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.ToolStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Stores loans. Each of {@link #checkout} and {@link #checkin} changes a loan and its tool together, as one unit:
 * either both changes are kept or neither is.
 */
public interface LoanRepository {
    /**
     * Opens a loan and marks the tool {@code ON_LOAN}, as one unit, and returns the saved loan with its new id.
     * Throws {@link NotFoundException} when the tool does not exist and {@link RuleViolationException} when it is
     * not {@code AVAILABLE}; nothing changes in either case.
     */
    Loan checkout(String memberId, String toolCode, LocalDate loanedOn, LocalDate dueOn);

    /**
     * Records the return of an open loan (return date and fee) and sets the tool's status to
     * {@code toolStatusAfter}, as one unit, and returns the updated loan. Throws {@link NotFoundException} for an
     * unknown loan and {@link RuleViolationException} for a loan that is already returned; nothing changes then.
     */
    Loan checkin(long loanId, LocalDate returnedOn, long feeCents, ToolStatus toolStatusAfter);

    /** The loan with {@code id}, or {@code null}. */
    Loan findById(long id);

    /** The member's loans that are not returned yet, ordered by id. */
    List<Loan> findOpenByMember(String memberId);

    /** Every loan, ordered by id. */
    List<Loan> findAll();
}
