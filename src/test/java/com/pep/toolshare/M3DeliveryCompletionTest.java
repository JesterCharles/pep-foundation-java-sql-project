package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import java.util.List;

/** Week 3 delivery: work committed on an m3/ branch, merged into main, pushed, and logged in WORKLOG.md. */
class M3DeliveryCompletionTest {
    private static final int MILESTONE = 3;

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
        LearnerRepo.assertEntryComplete(MILESTONE, List.of());
    }
}
