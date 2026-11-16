package com.pep.toolshare.jdbc;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.StorageException;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.lending.LendingException;
import com.pep.toolshare.lending.LoanRepository;
import com.pep.toolshare.lending.NotFoundException;
import com.pep.toolshare.lending.RuleViolationException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link LoanRepository} backed by the {@code loans} and {@code tools} tables. {@link #checkout} and
 * {@link #checkin} each run their statements in one transaction: auto-commit off, commit only after every statement
 * succeeded, roll back on any failure (including a rule violation), so the tables never disagree.
 */
public final class JdbcLoanRepository implements LoanRepository {
    private static final String COLUMNS = "id, member_id, tool_code, loaned_on, due_on, returned_on, fee_cents";

    private final ConnectionFactory connections;

    public JdbcLoanRepository(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }

    @Override
    public Loan checkout(String memberId, String toolCode, LocalDate loanedOn, LocalDate dueOn) {
        // TODO LEARNER 5D: one transaction - mark the tool ON_LOAN only if it is AVAILABLE, insert the loan, commit.
        throw new UnsupportedOperationException("TODO LEARNER 5D: checkout");
    }

    @Override
    public Loan checkin(long loanId, LocalDate returnedOn, long feeCents, ToolStatus toolStatusAfter) {
        // TODO LEARNER 5D: one transaction - close the open loan and set the tool status, commit.
        throw new UnsupportedOperationException("TODO LEARNER 5D: checkin");
    }

    @Override
    public Loan findById(long id) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: findById");
    }

    @Override
    public List<Loan> findOpenByMember(String memberId) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: findOpenByMember " + COLUMNS);
    }

    @Override
    public List<Loan> findAll() {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: findAll");
    }
}
