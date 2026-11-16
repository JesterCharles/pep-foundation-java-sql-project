package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 1: user stories with acceptance criteria, and an evidence-based review of an assistant's proposal. */
class WorklogStoriesCompletionTest {
    private static final Pattern STORY =
            Pattern.compile("(?i)as (?:a|an|the) .+?, i want .+?, so that .+");
    private static final Pattern LINE_REFERENCE = Pattern.compile("(?i)\\bline (\\d+)\\b");

    @Test
    void nameIsFilledIn() {
        List<Map.Entry<String, String>> header = LearnerRepo.fields("# Lantern work log");
        String name = header.stream().filter(field -> field.getKey().equals("Name"))
                .map(Map.Entry::getValue).findFirst().orElse("");
        assertTrue(!name.isBlank() && !name.contains("TODO"), "WORKLOG.md: write your name on the Name: line");
    }

    @Test
    void atLeastThreeUserStoriesEachWithAcceptanceCriteria() {
        int stories = 0;
        int storiesWithAcceptance = 0;
        boolean currentHasAcceptance = false;
        for (Map.Entry<String, String> field : LearnerRepo.fields("## User stories")) {
            if (field.getKey().equals("Story")) {
                if (stories > 0 && currentHasAcceptance) {
                    storiesWithAcceptance++;
                }
                assertTrue(STORY.matcher(field.getValue()).matches(),
                        "Write each story as: As a <who>, I want <what>, so that <why> (\"As an\" or \"As the\""
                                + " also work). Found: " + field.getValue());
                stories++;
                currentHasAcceptance = false;
            } else if (field.getKey().equals("Acceptance") && LearnerRepo.filled(field.getValue())) {
                currentHasAcceptance = true;
            }
        }
        if (stories > 0 && currentHasAcceptance) {
            storiesWithAcceptance++;
        }
        assertTrue(stories >= 3, "Write at least three Story: lines (found " + stories + ")");
        assertEquals(stories, storiesWithAcceptance,
                "Every story needs at least one Acceptance: line of three or more words right after it");
    }

    @Test
    void assistantProposalReviewCitesTheDiff() throws IOException {
        int lastDiffLine = 0;
        Matcher numbered = Pattern.compile("(?m)^\\s*(\\d+)  ").matcher(
                Files.readString(LearnerRepo.ROOT.resolve("review/assistant-proposal.md")));
        while (numbered.find()) {
            lastDiffLine = Math.max(lastDiffLine, Integer.parseInt(numbered.group(1)));
        }
        String decision = null;
        Set<Integer> cited = new HashSet<>();
        for (Map.Entry<String, String> field : LearnerRepo.fields("## Review of the assistant's proposal")) {
            if (field.getKey().equals("Decision")) {
                decision = field.getValue().toLowerCase();
            }
            if (field.getKey().equals("Evidence") && LearnerRepo.filled(field.getValue())) {
                Matcher reference = LINE_REFERENCE.matcher(field.getValue());
                if (reference.find()) {
                    int line = Integer.parseInt(reference.group(1));
                    assertTrue(line >= 1 && line <= lastDiffLine, "Evidence cites line " + line
                            + ", but the diff has lines 1-" + lastDiffLine);
                    cited.add(line);
                }
            }
        }
        assertTrue("approve".equals(decision) || "request changes".equals(decision),
                "Decision: must be exactly approve or request changes");
        assertTrue(cited.size() >= 2, "Give at least two Evidence: lines that cite different diff lines (line N)");
        assertEquals("request changes", decision,
                "Your evidence should decide the outcome: check every summary claim against the diff again");
    }
}
