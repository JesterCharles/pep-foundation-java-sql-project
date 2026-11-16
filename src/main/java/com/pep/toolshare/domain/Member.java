package com.pep.toolshare.domain;

import java.util.Objects;

/**
 * A library member. Every {@code Member} object is valid from the moment it is constructed: the constructor and
 * {@link #changeEmail(String)} reject bad values with {@link IllegalArgumentException}, and {@code null} for a
 * required value with {@link NullPointerException} that carries a message naming the value (for example
 * {@code Objects.requireNonNull(id, "id")}).
 *
 * <p>Rules: the id is {@code M-} plus four digits; the full name is not blank and is stored stripped; the email is
 * stored stripped and lower-case and has exactly one {@code @}, with text before the {@code @}, text between the
 * {@code @} and a later {@code .}, and text after that {@code .} (so {@code a@.test} and {@code a@b.} are
 * rejected); the tier is required; {@code referredBy} is either {@code null} or another member's valid id.
 */
public final class Member {
    private final String id;
    private final String fullName;
    private String email;
    private final MemberTier tier;
    private boolean active;
    private final String referredBy;

    public Member(String id, String fullName, String email, MemberTier tier, boolean active, String referredBy) {
        // TODO LEARNER 3A: validate every argument, then initialize every field.
        throw new UnsupportedOperationException("TODO LEARNER 3A: Member constructor");
    }

    /** True when {@code text} is {@code M-} followed by exactly four digits. */
    public static boolean isMemberId(String text) {
        // TODO LEARNER 3A: check the M- prefix and the four digits.
        throw new UnsupportedOperationException("TODO LEARNER 3A: isMemberId");
    }

    public String id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public String email() {
        return email;
    }

    public MemberTier tier() {
        return tier;
    }

    public boolean isActive() {
        return active;
    }

    /** The id of the member who referred this one, or {@code null}. */
    public String referredBy() {
        return referredBy;
    }

    /** Replaces the email after applying the same rules as the constructor. */
    public void changeEmail(String newEmail) {
        // TODO LEARNER 3A: validate, then replace the email.
        throw new UnsupportedOperationException("TODO LEARNER 3A: changeEmail");
    }

    public void deactivate() {
        active = false;
    }

    /** Readable form for logs and receipts: {@code M-0001 Avery Quill (REGULAR)}. It never contains the email. */
    @Override
    public String toString() {
        // TODO LEARNER 3A: build the readable form.
        throw new UnsupportedOperationException("TODO LEARNER 3A: toString");
    }

    /** Two members are the same member when their ids are equal. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Member member && id.equals(member.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
