package com.pep.toolshare.lending;

/** The request itself is malformed, such as a missing field or a badly formed value (HTTP 400). */
// TODO LEARNER 4A: extend LendingException and add a (message) and a (message, cause) constructor.
public class InvalidRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String message) {
        super("TODO LEARNER 4A: " + message);
    }
}
