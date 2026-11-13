package com.pep.toolshare.loan;

import com.pep.toolshare.db.ConnectionFactory;

import java.time.LocalDate;
import java.util.Objects;

/** Coordinates the multi-statement checkout transaction. */
public final class LoanService {
    private final ConnectionFactory connections;

    public LoanService(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }

    public long checkout(long memberId, long toolId, LocalDate borrowedOn, LocalDate dueOn) {
        // TODO LEARNER 6: In one JDBC transaction, mark an available tool ON_LOAN and insert its loan.
        // Commit only after both statements succeed; roll back and throw LoanException on every failure.
        throw new UnsupportedOperationException("TODO: implement checkout transaction");
    }
}
