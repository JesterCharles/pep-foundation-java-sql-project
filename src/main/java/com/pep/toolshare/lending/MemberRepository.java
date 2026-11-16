package com.pep.toolshare.lending;

import com.pep.toolshare.domain.Member;

import java.util.List;

/** Stores members. The in-memory and JDBC implementations must behave the same way. */
public interface MemberRepository {
    /** Every member, ordered by id. */
    List<Member> findAll();

    /** The member with {@code id}, or {@code null} when there is none. */
    Member findById(String id);

    /** Adds a new member; throws {@link RuleViolationException} when the id or email is already used. */
    void add(Member member);

    /**
     * Changes a member's email (with the {@link Member#changeEmail(String)} rules); returns {@code false} when no member
     * has {@code id}, and throws {@link RuleViolationException} when another member already uses the email.
     */
    boolean updateEmail(String id, String email);

    /**
     * Deletes a member; returns {@code false} when no member has {@code id}, and throws
     * {@link RuleViolationException} when the member still has loans on record.
     */
    boolean delete(String id);
}
