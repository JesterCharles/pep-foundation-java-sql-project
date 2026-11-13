package com.pep.toolshare.domain;

/** A registered member of the fictional tool library. */
public record Member(Long id, String fullName, String email) {
    public Member {
        // TODO LEARNER 1: Reject a null or blank full name and a null, blank, or malformed email address.
    }
}
