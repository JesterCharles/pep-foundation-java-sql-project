package com.pep.toolshare.tool;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.domain.CategorySummary;
import com.pep.toolshare.domain.Tool;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** JDBC implementation completed by the learner. */
public final class JdbcToolRepository implements ToolRepository {
    private final ConnectionFactory connections;

    public JdbcToolRepository(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }

    @Override
    public Optional<Tool> findById(long id) {
        // TODO LEARNER 4A: SELECT and map one tool row, including ToolStatus.
        return Optional.empty();
    }

    @Override
    public List<Tool> findAvailableByCategory(String category) {
        // TODO LEARNER 4B: Use a prepared filter and deterministic ORDER BY; null means every category.
        return List.of();
    }

    @Override
    public List<CategorySummary> summarizeCategories(int minimumToolCount) {
        // TODO LEARNER 5: Build the grouped category report with a LEFT JOIN, CASE, HAVING, and ORDER BY.
        return List.of();
    }
}
