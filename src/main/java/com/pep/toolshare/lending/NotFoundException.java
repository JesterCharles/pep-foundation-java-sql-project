package com.pep.toolshare.lending;

/** Something the request names does not exist (HTTP 404). */
// TODO LEARNER 4A: extend LendingException and add a (message) and a (message, cause) constructor.
public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super("TODO LEARNER 4A: " + message);
    }
}
