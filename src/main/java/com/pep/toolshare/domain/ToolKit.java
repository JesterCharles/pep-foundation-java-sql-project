package com.pep.toolshare.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A kit lent as one item, made of several tools (for example a "Deck repair kit"). A kit <em>has</em> tools; it is
 * not itself a kind of tool, so it holds them in a field and delegates to them.
 *
 * <p>The parts list is required, not empty, and copied, so later changes to the caller's list do not change the kit.
 */
public class ToolKit extends Lendable {
    private final List<Tool> parts;

    public ToolKit(String code, String name, List<Tool> parts) {
        super(code, name);
        // TODO LEARNER 3D: validate and copy the parts.
        throw new UnsupportedOperationException("TODO LEARNER 3D: ToolKit constructor");
    }

    /** The parts, as a list the caller cannot modify. */
    public List<Tool> parts() {
        // TODO LEARNER 3D
        throw new UnsupportedOperationException("TODO LEARNER 3D: parts");
    }

    /** The sum of the parts' replacement costs. */
    @Override
    public long replacementCents() {
        // TODO LEARNER 3D
        throw new UnsupportedOperationException("TODO LEARNER 3D: replacementCents");
    }

    /** A kit is available only when every part is available. */
    @Override
    public boolean isAvailable() {
        // TODO LEARNER 3D
        throw new UnsupportedOperationException("TODO LEARNER 3D: isAvailable");
    }

    /**
     * Checks out every part. When any part is unavailable, throws {@link IllegalStateException} before changing
     * any part, so a kit is never half lent.
     */
    public void checkOutAll() {
        // TODO LEARNER 3D
        throw new UnsupportedOperationException("TODO LEARNER 3D: checkOutAll");
    }
}
