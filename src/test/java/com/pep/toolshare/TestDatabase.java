package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

final class TestDatabase {
    private static final AtomicInteger NEXT_ID = new AtomicInteger();

    private TestDatabase() {
    }

    static ConnectionFactory create() {
        String url = "jdbc:h2:mem:toolshare_" + NEXT_ID.incrementAndGet()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        ConnectionFactory connections = ConnectionFactory.forUrl(url);
        DatabaseManager.reset(connections);
        return connections;
    }

    static long count(ConnectionFactory connections, String table) throws SQLException {
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    static String toolStatus(ConnectionFactory connections, long toolId) throws SQLException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status FROM tools WHERE id = ?")) {
            statement.setLong(1, toolId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new AssertionError("Missing test tool " + toolId);
                }
                return rows.getString("status");
            }
        }
    }

    static long activeLoansForTool(ConnectionFactory connections, long toolId) throws SQLException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM loans WHERE tool_id = ? AND returned_on IS NULL")) {
            statement.setLong(1, toolId);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getLong(1);
            }
        }
    }
}
