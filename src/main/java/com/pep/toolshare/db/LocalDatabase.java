package com.pep.toolshare.db;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Configuration for the repository-local, file-backed H2 database. */
public final class LocalDatabase {
    public static final String JDBC_URL =
            "jdbc:h2:file:./data/toolshare;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";

    private LocalDatabase() {
    }

    public static ConnectionFactory connectionFactory() {
        try {
            Files.createDirectories(Path.of("data"));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not create the local data directory", exception);
        }
        return ConnectionFactory.forUrl(JDBC_URL);
    }
}
