package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 1: your own Git history shows small commits, a resolved merge conflict and an undone mistake. */
class GitHistoryCompletionTest {
    @Test
    void mainHasAtLeastThreeOfYourOwnCommits() {
        LearnerRepo.requireRepository();
        int commits = LearnerRepo.learnerCommitsOnMain(false).size();
        assertTrue(commits >= 3, "main has " + commits + " of your commits; Week 1 needs at least three small, "
                + "focused commits (not counting merges)");
    }

    @Test
    void aMergeConflictWasResolvedOnMain() {
        LearnerRepo.requireRepository();
        List<String> merges = LearnerRepo.git("rev-list", "--merges", LearnerRepo.baseline() + ".." + LearnerRepo.MAIN)
                .lines();
        boolean resolved = false;
        for (String merge : merges) {
            List<String> parents = LearnerRepo.git("rev-list", "--parents", "-n", "1", merge).lines();
            String[] ids = parents.get(0).split(" ");
            if (ids.length != 3) {
                continue;
            }
            LearnerRepo.Result trial = LearnerRepo.git("merge-tree", "--write-tree", ids[1], ids[2]);
            assertTrue(trial.exitCode() == 0 || trial.exitCode() == 1,
                    "git merge-tree failed (Git 2.38 or newer is required): " + trial.err());
            if (trial.exitCode() == 1) {
                resolved = true;
                break;
            }
        }
        assertTrue(resolved, "main has no merge commit that resolved a conflict. Follow the conflict exercise in "
                + "docs/milestones/1-team-workflow.md: change the same line on two branches, merge, resolve.");
    }

    @Test
    void aCommitWasUndoneWithRevert() {
        LearnerRepo.requireRepository();
        Set<String> ownCommits = new HashSet<>(LearnerRepo.learnerCommitsOnMain(true));
        String log = LearnerRepo.git("log", "--format=%B", LearnerRepo.baseline() + ".." + LearnerRepo.MAIN).out();
        Matcher reverted = Pattern.compile("This reverts commit ([0-9a-f]{7,40})\\b").matcher(log);
        boolean found = false;
        while (reverted.find()) {
            String hash = reverted.group(1);
            found = found || ownCommits.stream().anyMatch(commit -> commit.startsWith(hash));
        }
        assertTrue(found, "main has no commit made with git revert that undoes one of your own commits");
    }

    @Test
    void noConflictMarkersAreCommitted() {
        LearnerRepo.requireRepository();
        LearnerRepo.Result markers = LearnerRepo.git("grep", "-n", "-I", "-E", "^(<<<<<<<|>>>>>>>)( |$)",
                LearnerRepo.MAIN, "--", ".");
        assertTrue(markers.out().isBlank(), "Remove the leftover conflict markers and commit:\n" + markers.out());
    }
}
