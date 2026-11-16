package com.pep.toolshare;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        write(repository, "WORKLOG.md", "# Work log\n\nName: TODO LEARNER 1D\nStory: TODO LEARNER 1D\n");
        commit(repository, "Reference fix to the work log template");
    }

    private static void write(Path repository, String file, String text) throws IOException {
        Path path = repository.resolve(file);
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }

    private static String commit(Path repository, String message) {
        git(repository, "add", "-A");
        git(repository, "-c", "user.name=Learner", "-c", "user.email=learner@example.test", "-c",
                "commit.gpgsign=false", "commit", "-q", "--no-verify", "-m", message);
        return git(repository, "rev-parse", "HEAD").out().strip();
    }

    private static LearnerRepo.Result git(Path repository, String... args) {
        LearnerRepo.Result result = LearnerRepo.git(repository, args);
        assertEquals(0, result.exitCode(), "git " + String.join(" ", args) + ": " + result.err());
        return result;
    }
}
