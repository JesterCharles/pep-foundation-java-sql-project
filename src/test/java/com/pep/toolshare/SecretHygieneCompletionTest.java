package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 1: secrets stay out of Git; the example settings file documents every variable without a real secret. */
class SecretHygieneCompletionTest {
    @Test
    void localEnvFileIsIgnored() {
        LearnerRepo.requireRepository();
        assertEquals(0, LearnerRepo.git("check-ignore", "-q", "--no-index", ".env").exitCode(),
                ".env must be ignored by .gitignore");
        assertEquals(0, LearnerRepo.git("check-ignore", "-q", "--no-index", ".env.local").exitCode(),
                ".env.local (any .env.* file) must be ignored by .gitignore");
        assertEquals(1, LearnerRepo.git("check-ignore", "-q", "--no-index", ".env.example").exitCode(),
                ".env.example must NOT be ignored: it is committed as the example");
    }

    @Test
    void envFileWasNeverCommitted() {
        LearnerRepo.requireRepository();
        assertTrue(LearnerRepo.git("log", "--all", "--format=%h", "--", ".env").lines().isEmpty(),
                "A .env file appears in your Git history. Treat its token as leaked: remove it from Git and "
                        + "choose a new token (ask your mentor how to clean the history).");
    }

    @Test
    void envExampleDocumentsTheWeekOneSettingsWithoutSecrets() throws IOException {
        Map<String, String> settings = readExample();
        assertTrue(settings.containsKey("LANTERN_PORT"), ".env.example must list LANTERN_PORT");
        assertTrue(settings.get("LANTERN_PORT").matches("\\d{2,5}"), "LANTERN_PORT needs an example port number");
        assertTrue(settings.containsKey("LANTERN_ADMIN_TOKEN"), ".env.example must list LANTERN_ADMIN_TOKEN");
        assertEquals("", settings.get("LANTERN_ADMIN_TOKEN"),
                "leave LANTERN_ADMIN_TOKEN empty in .env.example: real tokens live only in your .env");
    }

    @Test
    void summaryScriptIsCommittedAsExecutable() {
        LearnerRepo.requireRepository();
        String entry = LearnerRepo.git("ls-files", "-s", "--", "bin/loan-summary.sh").out();
        assertTrue(entry.startsWith("100755"), "Commit bin/loan-summary.sh with its executable bit "
                + "(chmod 750 bin/loan-summary.sh, then git add and commit). Index entry: " + entry.strip());
    }

    static Map<String, String> readExample() throws IOException {
        Map<String, String> settings = new LinkedHashMap<>();
        for (String line : Files.readAllLines(LearnerRepo.ROOT.resolve(".env.example"))) {
            String trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int equals = trimmed.indexOf('=');
            assertTrue(equals > 0, ".env.example lines look like NAME=value: " + trimmed);
            String value = trimmed.substring(equals + 1).strip();
            if (value.length() >= 2 && (value.charAt(0) == '\'' || value.charAt(0) == '"')
                    && value.charAt(value.length() - 1) == value.charAt(0)) {
                value = value.substring(1, value.length() - 1);
            }
            settings.put(trimmed.substring(0, equals).strip(), value);
        }
        return settings;
    }
}
