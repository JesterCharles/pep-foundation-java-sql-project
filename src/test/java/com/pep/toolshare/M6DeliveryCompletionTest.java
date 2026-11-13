package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import java.util.List;

/** Week 6 delivery: work committed on an m6/ branch, merged into main, pushed, and logged in WORKLOG.md. */
class M6DeliveryCompletionTest {
    private static final int MILESTONE = 6;

    @Test
    void milestoneFilesAreCommitted() {
        LearnerRepo.assertCommitted(LearnerRepo.milestoneFiles(MILESTONE));
    }

    @Test
    void milestoneBranchIsMergedIntoMainAndPushed() {
        LearnerRepo.assertMilestoneBranchDelivered(MILESTONE, LearnerRepo.milestoneFiles(MILESTONE));
    }

    @Test
    void worklogEntryIsComplete() {
        LearnerRepo.assertEntryComplete(MILESTONE, List.of("SQL or stream", "Big-O", "Walkthrough"));
    }
}
