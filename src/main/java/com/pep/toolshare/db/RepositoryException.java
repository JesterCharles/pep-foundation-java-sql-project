package com.pep.toolshare.db;

/** Wraps a JDBC failure at the repository boundary. */
public final class RepositoryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
