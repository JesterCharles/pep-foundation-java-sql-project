package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

/** A fresh in-memory database with the project schema and seed data, separate from the local data/ folder. */
public final class TestDatabase {
    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private static final AtomicInteger CREATED = new AtomicInteger();
    private static final AtomicInteger READS = new AtomicInteger();

    private TestDatabase() {
    }

    public static String newUrl() {
        return "jdbc:h2:mem:lantern_test_" + NEXT_ID.incrementAndGet()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    /** Creates the database and runs schema.sql and seed.sql. */
    public static ConnectionFactory create() {
        ConnectionFactory connections = ConnectionFactory.forUrl(newUrl());
        DatabaseManager.reset(connections);
        CREATED.incrementAndGet();
        return connections;
    }

    /** How many databases {@link #create()} has made in this test run. */
    public static int created() {
        return CREATED.get();
    }

    /** How many times {@link #count} or {@link #text} has read a database back in this test run. */
    public static int reads() {
        return READS.get();
    }

    public static void execute(ConnectionFactory connections, String sql) throws SQLException {
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public static long count(ConnectionFactory connections, String sql) throws SQLException {
        READS.incrementAndGet();
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    public static String text(ConnectionFactory connections, String sql) throws SQLException {
        READS.incrementAndGet();
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            return rows.next() ? rows.getString(1) : null;
        }
    }
}
