package com.pep.toolshare;

import com.pep.toolshare.domain.Dates;
import com.pep.toolshare.domain.MemberTier;
import com.pep.toolshare.reports.WaitListBoard;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Starter health: the supplied files are present and the supplied helpers work before any TODO is done. */
class ProjectHealthTest {
    @Test
    void suppliedDataFilesArePresent() throws IOException {
        String seed = Files.readString(Path.of("src/main/resources/db/seed.sql"));
        assertTrue(seed.contains("INSERT INTO loans"), "seed.sql is supplied with the sample loans");
        assertTrue(seed.contains("example.test"), "the sample data uses reserved example.test addresses only");
        List<String> log = Files.readAllLines(Path.of("fixtures/loan-log.txt"));
        assertEquals(28, log.size(), "fixtures/loan-log.txt is the supplied synthetic log");
        assertTrue(Files.isRegularFile(Path.of("review/assistant-proposal.md")));
        assertTrue(Files.isRegularFile(Path.of("WORKLOG.md")));
        assertTrue(Files.isRegularFile(Path.of(".env.example")));
    }

    @Test
    void suppliedHelpersWork() {
        assertEquals(6, Dates.daysBetween(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 15)));
        assertEquals(LocalDate.of(2026, 10, 15), Dates.parseOrNull("2026-10-15"));
        assertNull(Dates.parseOrNull("15/10/2026"));
        WaitListBoard board = new WaitListBoard();
        assertSame(board.forTool("LT-1003-4"), board.forTool("LT-1003-4"));
        assertEquals(3, MemberTier.values().length);
    }

    @Test
    void appPrintsHelpWithoutAnyLearnerCode() {
        java.io.PrintStream original = System.out;
        java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(captured, true, java.nio.charset.StandardCharsets.UTF_8));
        try {
            App.main(new String[] {"help"});
        } finally {
            System.setOut(original);
        }
        String help = captured.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(help.contains("reset") && help.contains("report") && help.contains("serve"), help);
    }
}
