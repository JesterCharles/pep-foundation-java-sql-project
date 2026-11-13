package com.pep.toolshare.lending;

import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@link LoanRepository} kept in memory. It shares the tool and member repositories so that a checkout changes the
 * loan and the tool together, as the database version will.
 */
public final class InMemoryLoanRepository implements LoanRepository {
    private final InMemoryToolRepository tools;
    private final InMemoryMemberRepository members;
    private final Map<Long, Loan> loans = new TreeMap<>();
    private long nextId = 1;

    public InMemoryLoanRepository(InMemoryToolRepository tools, InMemoryMemberRepository members) {
        this.tools = tools;
        this.members = members;
    }

    /** Loads a loan that already exists (used for the sample data). Ids continue after the largest loaded id. */
    public void restore(Loan loan) {
        loans.put(loan.id(), loan);
        nextId = Math.max(nextId, loan.id() + 1);
        members.recordLoanFor(loan.memberId());
    }

    @Override
    public Loan checkout(String memberId, String toolCode, LocalDate loanedOn, LocalDate dueOn) {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: checkout");
    }

    @Override
    public Loan checkin(long loanId, LocalDate returnedOn, long feeCents, ToolStatus toolStatusAfter) {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: checkin");
    }

    @Override
    public Loan findById(long id) {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: findById");
    }

    @Override
    public List<Loan> findOpenByMember(String memberId) {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: findOpenByMember");
    }

    @Override
    public List<Loan> findAll() {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: findAll");
    }
}
