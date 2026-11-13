package com.pep.toolshare.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Creates and resets the deterministic local schema and synthetic fixtures. */
public final class DatabaseManager {
    private DatabaseManager() {
    }

    private static final Set<Integer> TABLE_NOT_FOUND = Set.of(42102, 42103, 42104);
    private static final List<String> REQUIRED_TABLES = List.of("members", "tools", "loans");

    public static void initializeIfNeeded(ConnectionFactory connections) {
        if (!schemaIsComplete(connections)) {
            reset(connections);
        }
    }

    private static boolean schemaIsComplete(ConnectionFactory connections) {
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement()) {
            for (String table : REQUIRED_TABLES) {
                try {
                    statement.executeQuery("SELECT 1 FROM " + table + " FETCH FIRST 1 ROW ONLY").close();
                } catch (SQLException exception) {
                    if (TABLE_NOT_FOUND.contains(exception.getErrorCode())) {
                        return false;
                    }
                    throw exception;
                }
            }
            return true;
        } catch (SQLException exception) {
            throw new RepositoryException("Could not inspect the local database schema", exception);
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
