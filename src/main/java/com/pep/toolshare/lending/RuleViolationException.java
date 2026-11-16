package com.pep.toolshare.lending;

/** A lending rule refuses the request, such as checking out a tool that is on loan (HTTP 409). */
// TODO LEARNER 4A: extend LendingException and add a (message) and a (message, cause) constructor.
public class RuleViolationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RuleViolationException(String message) {
        super("TODO LEARNER 4A: " + message);
    }
}
