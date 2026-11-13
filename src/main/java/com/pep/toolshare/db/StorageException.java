package com.pep.toolshare.db;

/**
 * Wraps a checked {@link java.sql.SQLException} in an unchecked exception at the repository boundary, keeping the
 * original as its cause so the stack trace still shows the root problem.
 */
public final class StorageException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
