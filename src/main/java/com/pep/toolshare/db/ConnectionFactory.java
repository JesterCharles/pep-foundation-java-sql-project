package com.pep.toolshare.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/** Opens a new JDBC connection each time it is asked. Callers close what they open. */
@FunctionalInterface
public interface ConnectionFactory {
    Connection open() throws SQLException;

    static ConnectionFactory forUrl(String jdbcUrl) {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        return () -> DriverManager.getConnection(jdbcUrl);
    }
}
