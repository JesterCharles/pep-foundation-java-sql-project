package com.pep.toolshare.jdbc;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.StorageException;
import com.pep.toolshare.domain.Dates;
import com.pep.toolshare.reports.CategoryUsage;
import com.pep.toolshare.reports.LibraryReports;
import com.pep.toolshare.reports.MemberBalance;
import com.pep.toolshare.reports.MemberRef;
import com.pep.toolshare.reports.OverdueLoan;
import com.pep.toolshare.reports.Referral;
import com.pep.toolshare.reports.TopTool;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link LibraryReports} answered by SQL. Each report is one query: do the filtering, joining, grouping and sorting
 * in SQL, then map each row into its record. {@link #runQuery} handles the JDBC plumbing.
 */
public final class SqlReports implements LibraryReports {
    private final ConnectionFactory connections;

    public SqlReports(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }

    @Override
    public List<CategoryUsage> categoryUsage() {
        // TODO LEARNER 5E
        throw new UnsupportedOperationException("TODO LEARNER 5E: categoryUsage");
    }

    @Override
    public List<String> busyCategories(int minLoans) {
        // TODO LEARNER 5E
        throw new UnsupportedOperationException("TODO LEARNER 5E: busyCategories");
    }

    @Override
    public List<OverdueLoan> overdueLoans(LocalDate asOf) {
        // TODO LEARNER 5E
        throw new UnsupportedOperationException("TODO LEARNER 5E: overdueLoans");
    }

    @Override
    public List<MemberRef> membersWithoutLoans() {
        // TODO LEARNER 5E
        throw new UnsupportedOperationException("TODO LEARNER 5E: membersWithoutLoans");
    }

    @Override
    public List<Referral> referrals() {
        // TODO LEARNER 5E
        throw new UnsupportedOperationException("TODO LEARNER 5E: referrals");
    }

    @Override
    public List<TopTool> topToolsPerCategory(int perCategory) {
        // TODO LEARNER 5E
        throw new UnsupportedOperationException("TODO LEARNER 5E: topToolsPerCategory");
    }

    @Override
    public List<MemberBalance> memberBalances() {
        // TODO LEARNER 5E
        throw new UnsupportedOperationException("TODO LEARNER 5E: memberBalances");
    }

    /** Maps one result row; may throw {@link SQLException}. */
    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rows) throws SQLException;
    }

    /** Runs {@code sql} with its {@code ?} parameters bound in order and maps every row. */
    <T> List<T> runQuery(String sql, List<Object> parameters, RowMapper<T> mapper) {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rows.next()) {
                    results.add(mapper.map(rows));
                }
                return results;
            }
        } catch (SQLException exception) {
            throw new StorageException("Report query failed: " + exception.getMessage(), exception);
        }
    }
}
