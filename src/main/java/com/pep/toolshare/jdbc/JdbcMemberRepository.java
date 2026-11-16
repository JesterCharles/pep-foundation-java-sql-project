package com.pep.toolshare.jdbc;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.StorageException;
import com.pep.toolshare.domain.Member;
import com.pep.toolshare.domain.MemberTier;
import com.pep.toolshare.lending.MemberRepository;
import com.pep.toolshare.lending.RuleViolationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link MemberRepository} backed by the {@code members} table. Every value is bound with a
 * {@link PreparedStatement} parameter, every JDBC object is closed with try-with-resources, and every
 * {@link SQLException} is wrapped in {@link StorageException} with its cause, except the constraint violations the
 * interface turns into {@link RuleViolationException}.
 */
public final class JdbcMemberRepository implements MemberRepository {
    /** SQLState class 23: an integrity constraint (unique, foreign key, check) refused the statement. */
    static final String INTEGRITY_VIOLATION_CLASS = "23";

    private final ConnectionFactory connections;

    public JdbcMemberRepository(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }

    @Override
    public List<Member> findAll() {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: findAll");
    }

    @Override
    public Member findById(String id) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: findById");
    }

    @Override
    public void add(Member member) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: add");
    }

    @Override
    public boolean updateEmail(String id, String email) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: updateEmail");
    }

    @Override
    public boolean delete(String id) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: delete");
    }

    /** True when the failure is a constraint violation (SQLState starting with 23). */
    static boolean isIntegrityViolation(SQLException exception) {
        String state = exception.getSQLState();
        return state != null && state.startsWith(INTEGRITY_VIOLATION_CLASS);
    }
}
