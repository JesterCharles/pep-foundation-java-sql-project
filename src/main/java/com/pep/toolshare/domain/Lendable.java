package com.pep.toolshare.domain;

import com.pep.toolshare.basics.LendingMath;

import java.util.Objects;

/**
 * Anything the library lends: a single {@link Tool} or a {@link ToolKit} of tools. Subclasses share the code and
 * name kept here and decide their own replacement cost and availability.
 *
 * <p>The code must pass {@link LendingMath#isValidToolCode(String)} and the name must not be blank (stored
 * stripped); otherwise the constructor throws {@link IllegalArgumentException}.
 */
public abstract class Lendable {
    private final String code;
    private final String name;

    protected Lendable(String code, String name) {
        // TODO LEARNER 3C: validate the code and name, then initialize both fields.
        throw new UnsupportedOperationException("TODO LEARNER 3C: Lendable constructor");
    }

    public final String code() {
        return code;
    }

    public final String name() {
        return name;
    }

    /** What the library would pay to replace this item, in cents. */
    public abstract long replacementCents();

    /** True when this item can be lent right now. */
    public abstract boolean isAvailable();

    /** {@code LT-1001-2 Claw hammer} */
    @Override
    public String toString() {
        return code + " " + name;
    }
}
