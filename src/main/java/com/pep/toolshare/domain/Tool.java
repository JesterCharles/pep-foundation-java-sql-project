package com.pep.toolshare.domain;

import java.util.Objects;

/**
 * One physical tool. Its status changes only through the methods below; an illegal change throws
 * {@link IllegalStateException} and leaves the status unchanged.
 *
 * <pre>
 * AVAILABLE --checkOut--> ON_LOAN --checkIn(false)--> AVAILABLE
 *                         ON_LOAN --checkIn(true)---> IN_REPAIR --finishRepair--> AVAILABLE
 * AVAILABLE or IN_REPAIR --retire--> RETIRED   (an ON_LOAN or RETIRED tool cannot be retired)
 * </pre>
 *
 * <p>Constructor rules (on top of {@link Lendable}'s): category, status and shelf are required; the replacement
 * cost is greater than zero; the shelf looks like {@code A-01} (an upper-case letter, a dash, two digits). A
 * {@code null} required value throws {@link NullPointerException}; any other broken rule throws
 * {@link IllegalArgumentException}.
 */
public class Tool extends Lendable {
    private final ToolCategory category;
    private final long replacementCents;
    private ToolStatus status;
    private final String shelf;

    public Tool(String code, String name, ToolCategory category, long replacementCents, ToolStatus status,
                String shelf) {
        super(code, name);
        // TODO LEARNER 3C: validate and initialize the Tool fields.
        throw new UnsupportedOperationException("TODO LEARNER 3C: Tool constructor");
    }

    public ToolCategory category() {
        return category;
    }

    public ToolStatus status() {
        return status;
    }

    public String shelf() {
        return shelf;
    }

    @Override
    public long replacementCents() {
        return replacementCents;
    }

    @Override
    public boolean isAvailable() {
        // TODO LEARNER 3C
        throw new UnsupportedOperationException("TODO LEARNER 3C: isAvailable");
    }

    public void checkOut() {
        // TODO LEARNER 3C
        throw new UnsupportedOperationException("TODO LEARNER 3C: checkOut");
    }

    public void checkIn(boolean damaged) {
        // TODO LEARNER 3C
        throw new UnsupportedOperationException("TODO LEARNER 3C: checkIn");
    }

    public void finishRepair() {
        // TODO LEARNER 3C
        throw new UnsupportedOperationException("TODO LEARNER 3C: finishRepair");
    }

    public void retire() {
        // TODO LEARNER 3C
        throw new UnsupportedOperationException("TODO LEARNER 3C: retire");
    }
}
