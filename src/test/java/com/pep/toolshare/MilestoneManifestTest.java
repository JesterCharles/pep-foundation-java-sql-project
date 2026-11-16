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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps docs/milestones/manifest.tsv, the guides, the README, the TODO markers and the checks consistent. */
class MilestoneManifestTest {
    private static final Path MANIFEST = Path.of("docs", "milestones", "manifest.tsv");
    private static final Path TEST_SOURCES = Path.of("src", "test", "java", "com", "pep", "toolshare");
    private static final Pattern TODO = Pattern.compile("TODO LEARNER (\\d[A-Z])");

    private record Milestone(int number, String slug, String guide, List<String> files, List<String> todos,
                             List<String> tests) {
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
            assertTrue(milestone.files().contains("WORKLOG.md"), "every milestone has a WORKLOG.md entry");
            for (String file : milestone.files()) {
                assertTrue(Files.isRegularFile(Path.of(file)), "missing learner file " + file);
            }
            for (String todo : milestone.todos()) {
                assertTrue(todo.matches(milestone.number() + "[A-Z]"), "TODO id " + todo + " belongs to milestone "
                        + todo.charAt(0));
            }
            for (String test : milestone.tests()) {
                assertTrue(Files.isRegularFile(TEST_SOURCES.resolve(test + ".java")), "missing test class " + test);
            }
            String guide = Files.readString(Path.of(milestone.guide()));
            for (String test : milestone.tests()) {
                assertTrue(guide.contains(test), milestone.guide() + " must name the check " + test);
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

    @Test
    void everyRemainingTodoIsInAFileOfItsMilestone() throws IOException {
        List<Milestone> milestones = readManifest();
        Set<String> learnerFiles = new HashSet<>();
        milestones.forEach(milestone -> learnerFiles.addAll(milestone.files()));
        List<Path> roots = List.of(Path.of("src"), Path.of("bin"), Path.of("docs"), Path.of("scripts"));
        List<Path> candidates = new ArrayList<>(List.of(Path.of(".gitignore"), Path.of(".env.example"),
                Path.of("WORKLOG.md"), Path.of("pom.xml"), Path.of("README.md")));
        for (Path root : roots) {
            if (Files.isDirectory(root)) {
                try (Stream<Path> walk = Files.walk(root)) {
                    walk.filter(Files::isRegularFile).forEach(candidates::add);
                }
            }
        }
        for (Path file : candidates) {
            if (!Files.isRegularFile(file) || file.startsWith(Path.of("docs"))) {
                continue;
            }
            String relative = file.toString().replace('\\', '/');
            Matcher matcher = TODO.matcher(Files.readString(file));
            while (matcher.find()) {
                String id = matcher.group(1);
                Milestone owner = milestones.get(id.charAt(0) - '1');
                assertTrue(owner.todos().contains(id), relative + " has TODO " + id + " not listed in the manifest");
                assertTrue(owner.files().contains(relative), relative + " has TODO " + id
                        + " but is not a learner file of milestone " + owner.number());
                assertTrue(learnerFiles.contains(relative), relative + " is not a learner file");
            }
        }
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
                    List.of(columns[5].split(",")),
                    List.of(columns[6].split(","))
            ));
        }
        return milestones;
    }
}
