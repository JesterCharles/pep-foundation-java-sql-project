package com.pep.toolshare;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Supplied health check for how your own commits are told apart from the starter: history that came with the starter,
 * however long, never counts as your work, and every commit you make after your first Milestone 1 change does.
 */
class LearnerHistoryTest {
    @Test
    void starterHistoryAloneIsNoLearnerWork(@TempDir Path repository) throws IOException {
        importStarterWithHistory(repository);
        assertEquals(List.of(), LearnerRepo.learnerCommitsOnMain(repository, true));
    }

    @Test
    void commitsAfterTheStarterAreLearnerWork(@TempDir Path repository) throws IOException {
        importStarterWithHistory(repository);
        List<String> mine = new ArrayList<>();
        write(repository, ".gitignore", "target/\n.env\n.env.*\n!.env.example\n");
        mine.add(0, commit(repository, "Ignore local settings"));
        write(repository, "bin/loan-summary.sh", "#!/usr/bin/env bash\necho \"Loan log: $1\"\n");
        mine.add(0, commit(repository, "Summarize the loan log"));
        write(repository, "README.md", "Lantern\n");
        mine.add(0, commit(repository, "Touch only the readme"));
        assertEquals(mine, LearnerRepo.learnerCommitsOnMain(repository, false));
    }

    @Test
    void exercisesOnMainBeforeTheMilestoneMergeAreLearnerWork(@TempDir Path repository) throws IOException {
        importStarterWithHistory(repository);
        Set<String> mine = new HashSet<>();
        git(repository, "switch", "-q", "-c", "m1/team-workflow");
        write(repository, ".gitignore", "target/\n.env\n");
        mine.add(commit(repository, "Ignore local settings"));
        git(repository, "switch", "-q", LearnerRepo.MAIN);
        git(repository, "switch", "-q", "-c", "m1/notes-a");
        write(repository, "WORKLOG.md", WORKLOG.replace("Blockers: TODO LEARNER 1W", "Blockers: none at all today"));
        mine.add(commit(repository, "Note no blockers"));
        git(repository, "switch", "-q", "-c", "m1/notes-b", LearnerRepo.MAIN);
        write(repository, "WORKLOG.md", WORKLOG.replace("Blockers: TODO LEARNER 1W", "Blockers: waiting on Java"));
        mine.add(commit(repository, "Note a blocker"));
        git(repository, "switch", "-q", LearnerRepo.MAIN);
        identity(repository, "merge", "-q", "m1/notes-a");
        assertEquals(1, asLearner(repository, "merge", "-q", "m1/notes-b").exitCode(),
                "the second merge must conflict");
        write(repository, "WORKLOG.md", WORKLOG.replace("Blockers: TODO LEARNER 1W", "Blockers: waiting on Java"));
        mine.add(commit(repository, "Resolve the blockers conflict"));
        write(repository, "README.md", "A deliberate mistake\n");
        mine.add(commit(repository, "Make a mistake"));
        identity(repository, "revert", "--no-edit", "HEAD");
        mine.add(head(repository));
        identity(repository, "merge", "-q", "--no-ff", "-m", "Merge the team workflow", "m1/team-workflow");
        mine.add(head(repository));
        assertEquals(mine, new HashSet<>(LearnerRepo.learnerCommitsOnMain(repository, true)));
    }

    @Test
    void revertingTheFirstMilestoneCommitKeepsBothAsLearnerWork(@TempDir Path repository) throws IOException {
        importStarterWithHistory(repository);
        List<String> mine = new ArrayList<>();
        write(repository, ".gitignore", "target/\n.env\n");
        mine.add(0, commit(repository, "Ignore local settings"));
        identity(repository, "revert", "--no-edit", "HEAD");
        mine.add(0, head(repository));
        assertEquals(mine, LearnerRepo.learnerCommitsOnMain(repository, false));
    }

    private static final String WORKLOG = "# Work log\n\nName: TODO LEARNER 1D\nStory: TODO LEARNER 1D\n\n### M1\n"
            + "Blockers: TODO LEARNER 1W\n";

    /** An older project, then the starter, then later maintenance commits that keep the starter files untouched. */
    private static void importStarterWithHistory(Path repository) throws IOException {
        git(repository, "init", "-q", "-b", LearnerRepo.MAIN);
        write(repository, "README.md", "An older project\n");
        commit(repository, "Older project");
        write(repository, "bin/loan-summary.sh", "#!/usr/bin/env bash\n"
                + "echo \"TODO LEARNER 1C: summary not written yet\" >&2\nexit 1\n");
        write(repository, "WORKLOG.md", "# Work log\n\nName: TODO LEARNER 1D\n");
        write(repository, ".env.example", "# TODO LEARNER 1B: list LANTERN_PORT here.\n");
        write(repository, ".gitignore", "target/\n# TODO LEARNER 1A: ignore the local .env file.\n");
        commit(repository, "Starter");
        write(repository, "docs/guide.md", "Guide\n");
        commit(repository, "Reference fix to the guide");
        write(repository, "WORKLOG.md", WORKLOG);
        commit(repository, "Reference fix to the work log template");
    }

    private static void write(Path repository, String file, String text) throws IOException {
        Path path = repository.resolve(file);
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }

    private static String commit(Path repository, String message) {
        git(repository, "add", "-A");
        identity(repository, "commit", "-q", "--no-verify", "-m", message);
        return head(repository);
    }

    private static void identity(Path repository, String... args) {
        LearnerRepo.Result result = asLearner(repository, args);
        assertEquals(0, result.exitCode(), "git " + String.join(" ", args) + ": " + result.err());
    }

    private static LearnerRepo.Result asLearner(Path repository, String... args) {
        List<String> all = new ArrayList<>(List.of("-c", "user.name=Learner", "-c", "user.email=learner@example.test",
                "-c", "commit.gpgsign=false", "-c", "merge.ff=true"));
        all.addAll(List.of(args));
        return LearnerRepo.git(repository, all.toArray(String[]::new));
    }

    private static String head(Path repository) {
        return git(repository, "rev-parse", "HEAD").out().strip();
    }

    private static LearnerRepo.Result git(Path repository, String... args) {
        LearnerRepo.Result result = LearnerRepo.git(repository, args);
        assertEquals(0, result.exitCode(), "git " + String.join(" ", args) + ": " + result.err());
        return result;
    }
}
