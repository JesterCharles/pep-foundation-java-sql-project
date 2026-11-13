package com.pep.toolshare.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/** Creates and resets the deterministic local schema and synthetic fixtures. */
public final class DatabaseManager {
    private DatabaseManager() {
    }

    public static void initializeIfNeeded(ConnectionFactory connections) {
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1 FROM members FETCH FIRST 1 ROW ONLY");
        } catch (SQLException missingSchema) {
            reset(connections);
        }
    }

    public static void reset(ConnectionFactory connections) {
        try (Connection connection = connections.open()) {
            connection.setAutoCommit(false);
            try {
                execute(connection, "DROP ALL OBJECTS");
                executeScript(connection, "/db/schema.sql");
                executeScript(connection, "/db/seed.sql");
                connection.commit();
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw new RepositoryException("Could not reset the database", exception);
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Could not open the database for reset", exception);
        }
    }

    private static void executeScript(Connection connection, String resource) throws SQLException {
        String script = readResource(resource).lines()
                .map(DatabaseManager::removeLineComment)
                .collect(Collectors.joining("\n"));
        for (String sql : script.split(";")) {
            if (!sql.isBlank()) {
                execute(connection, sql);
            }
        }
    }

    private static String removeLineComment(String line) {
        int commentStart = line.indexOf("--");
        return commentStart < 0 ? line : line.substring(0, commentStart);
    }

    private static String readResource(String resource) {
        try (InputStream input = DatabaseManager.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Missing classpath resource: " + resource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read " + resource, exception);
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void rollback(Connection connection, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            original.addSuppressed(rollbackFailure);
        }
    }
}
