package com.pep.toolshare.lending;

/**
 * The base class of every lending failure that the API turns into an error response. It is unchecked, so service
 * and repository methods do not have to declare it.
 */
// TODO LEARNER 4A: make this an unchecked exception with a (message) and a (message, cause) constructor.
public class LendingException {
    public LendingException(String message) {
        throw new UnsupportedOperationException("TODO LEARNER 4A: " + message);
    }
}
