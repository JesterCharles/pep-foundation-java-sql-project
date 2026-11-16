package com.pep.toolshare;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 1: bin/loan-summary.sh summarizes a loan log with grep, pipes and wc. */
class LoanSummaryScriptCompletionTest {
    private record Run(int exitCode, String out, String err) {
    }

    private static Run run(Map<String, String> env, String... args) throws IOException {
        Path script = LearnerRepo.ROOT.resolve("bin/loan-summary.sh");
        assertTrue(Files.isExecutable(script),
                "bin/loan-summary.sh is not executable. Run: chmod 750 bin/loan-summary.sh");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command().add("./bin/loan-summary.sh");
        builder.command().addAll(List.of(args));
        builder.directory(LearnerRepo.ROOT.toFile());
        builder.environment().remove("LANTERN_LOCATION");
        builder.environment().putAll(env);
        LearnerRepo.Result result = LearnerRepo.run(builder, Duration.ofSeconds(15), "bin/loan-summary.sh");
        return new Run(result.exitCode(), result.out(), result.err());
    }

    @Test
    void summarizesTheSuppliedLog() throws Exception {
        Run run = run(Map.of());
        assertEquals(0, run.exitCode(), "exit status; stderr was: " + run.err());
        assertEquals("""
                Loan log: fixtures/loan-log.txt
                Checkouts: 14
                Returns: 10
                Late returns: 3
                Other events: 4
                Location: (not set)
                """, run.out());
    }

    @Test
    void summarizesAnyLogGivenAsArgument(@TempDir Path folder) throws Exception {
        Path log = folder.resolve("week.txt");
        Files.writeString(log, """
                2026-11-02 09:00 CHECKOUT LT-1001-2 M-0001
                2026-11-02 09:30 CHECKOUT LT-1004-5 M-0002
                2026-11-03 10:00 RETURN LT-1001-2 M-0001 LATE
                2026-11-03 11:00 NOTE checkout counter moved
                2026-11-04 12:00 RETURN LT-1004-5 M-0002
                """);
        Run run = run(Map.of(), log.toString());
        assertEquals(0, run.exitCode(), "exit status; stderr was: " + run.err());
        assertEquals("Loan log: " + log + "\nCheckouts: 2\nReturns: 2\nLate returns: 1\nOther events: 1\n"
                + "Location: (not set)\n", run.out());
    }

    @Test
    void countsZeroWhenNothingMatches(@TempDir Path folder) throws Exception {
        Path log = folder.resolve("quiet.txt");
        Files.writeString(log, "2026-11-05 08:00 NOTE desk closed\n");
        Run run = run(Map.of(), log.toString());
        assertEquals(0, run.exitCode(), "an empty count is not an error; stderr was: " + run.err());
        assertTrue(run.out().contains("Checkouts: 0\nReturns: 0\nLate returns: 0\nOther events: 1\n"), run.out());
    }

    @Test
    void readsTheExportedLocationVariable() throws Exception {
        Run run = run(Map.of("LANTERN_LOCATION", "Riverside branch"));
        assertEquals(0, run.exitCode(), "exit status; stderr was: " + run.err());
        assertTrue(run.out().endsWith("Location: Riverside branch\n"), run.out());
    }

    @Test
    void rejectsAMissingLogFile() throws Exception {
        Run run = run(Map.of(), "fixtures/no-such-log.txt");
        assertEquals(2, run.exitCode(), "a missing log file exits with status 2");
        assertTrue(run.err().contains("No such log file: fixtures/no-such-log.txt"),
                "the error goes to stderr; stderr was: " + run.err());
        assertEquals("", run.out(), "nothing is printed to stdout for a missing file");
    }
}
