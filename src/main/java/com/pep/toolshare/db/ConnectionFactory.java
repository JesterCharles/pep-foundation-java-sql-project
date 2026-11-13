package com.pep.toolshare.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/** Opens independent JDBC connections for repositories and services. */
@FunctionalInterface
public interface ConnectionFactory {
    Connection open() throws SQLException;

    static ConnectionFactory forUrl(String jdbcUrl) {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        return () -> DriverManager.getConnection(jdbcUrl);
    }
}
