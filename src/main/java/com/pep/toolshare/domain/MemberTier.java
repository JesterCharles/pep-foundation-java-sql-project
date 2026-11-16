package com.pep.toolshare.domain;

import com.pep.toolshare.basics.LendingMath;

/** Membership tiers. A member's tier decides which fee policy applies to their late returns. */
public enum MemberTier {
    STARTER,
    REGULAR,
    CHAMPION;

    /** The tier earned by a number of completed loans, using the Week 2 rule in {@link LendingMath}. */
    public static MemberTier forCompletedLoans(int completedLoans) {
        String name = LendingMath.membershipTier(completedLoans);
        if ("INVALID".equals(name)) {
            throw new IllegalArgumentException("completed loans cannot be negative: " + completedLoans);
        }
        return valueOf(name);
    }
}
