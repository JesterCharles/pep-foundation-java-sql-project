package com.pep.toolshare.jdbc;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.StorageException;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.lending.RuleViolationException;
import com.pep.toolshare.lending.ToolRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** {@link ToolRepository} backed by the {@code tools} table. Same JDBC rules as {@link JdbcMemberRepository}. */
public final class JdbcToolRepository implements ToolRepository {
    private static final String COLUMNS = "code, name, category_code, replacement_cents, status, shelf";

    private final ConnectionFactory connections;

    public JdbcToolRepository(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }

    @Override
    public List<Tool> findAll() {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: findAll " + COLUMNS);
    }

    @Override
    public List<Tool> findByCategory(ToolCategory category) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: findByCategory");
    }

    @Override
    public Tool findByCode(String code) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: findByCode");
    }

    @Override
    public void add(Tool tool) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: add");
    }

    @Override
    public boolean updateStatus(String code, ToolStatus status) {
        // TODO LEARNER 5C
        throw new UnsupportedOperationException("TODO LEARNER 5C: updateStatus");
    }
}
