package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MilestoneManifestTest {
    private static final Path MANIFEST = Path.of("docs", "milestones", "manifest.tsv");
    private static final Path TEST_SOURCES = Path.of("src", "test", "java", "com", "pep", "toolshare");

    private record Milestone(int number, String slug, String guide, List<String> files, List<String> tests) {
    }

    @Test
    void manifestDescribesSixLinkedCumulativeMilestones() throws IOException {
        List<Milestone> milestones = readManifest();
        String readme = Files.readString(Path.of("README.md"));

        assertEquals(6, milestones.size(), "the project defines six milestones");
        Set<String> slugs = new HashSet<>();
        for (int index = 0; index < milestones.size(); index++) {
            Milestone milestone = milestones.get(index);
            assertEquals(index + 1, milestone.number(), "milestones are numbered 1 through 6 in order");
            assertTrue(slugs.add(milestone.slug()), "duplicate milestone slug " + milestone.slug());
            assertTrue(Files.isRegularFile(Path.of(milestone.guide())), "missing guide " + milestone.guide());
            assertTrue(readme.contains("(" + milestone.guide() + ")"), "README must link " + milestone.guide());
            assertTrue(!milestone.files().isEmpty() && !milestone.tests().isEmpty(),
                    "milestone " + milestone.number() + " needs files and tests");
            for (String file : milestone.files()) {
                assertTrue(Files.isRegularFile(Path.of(file)), "missing learner file " + file);
            }
            for (String test : milestone.tests()) {
                assertTrue(Files.isRegularFile(TEST_SOURCES.resolve(test + ".java")), "missing test class " + test);
            }
        }
    }

    @Test
    void everyCompletionTestBelongsToExactlyOneMilestone() throws IOException {
        List<String> listed = new ArrayList<>();
        readManifest().forEach(milestone -> listed.addAll(milestone.tests()));

        Set<String> present = new TreeSet<>();
        try (Stream<Path> files = Files.list(TEST_SOURCES)) {
            files.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith("CompletionTest.java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .forEach(present::add);
        }

        assertEquals(listed.size(), new HashSet<>(listed).size(), "a completion test is listed twice");
        assertEquals(present, new TreeSet<>(listed));
    }

    private static List<Milestone> readManifest() throws IOException {
        List<Milestone> milestones = new ArrayList<>();
        for (String line : Files.readAllLines(MANIFEST)) {
            String[] columns = line.split("\t", -1);
            if (line.startsWith("#") || !columns[0].matches("\\d+")) {
                continue;
            }
            assertEquals(7, columns.length, "manifest rows have seven tab-separated columns");
            milestones.add(new Milestone(
                    Integer.parseInt(columns[0]),
                    columns[1],
                    columns[3],
                    List.of(columns[4].split(",")),
                    List.of(columns[6].split(","))
            ));
        }
        return milestones;
    }
}
