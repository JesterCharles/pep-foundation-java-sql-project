package com.pep.toolshare.loan;

/** Reports a checkout rule or database failure to the application boundary. */
public final class LoanException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LoanException(String message) {
        super(message);
    }

    public LoanException(String message, Throwable cause) {
        super(message, cause);
    }
}
