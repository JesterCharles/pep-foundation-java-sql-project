package com.pep.toolshare;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Reads the learner's Git repository and work log. Checks only local state: it never contacts a server, and it does
 * not depend on the remote's name or address. "Pushed" means your local copy of the remote branch (for example
 * origin/main) contains your commits, which is true after a successful git push.
 */
public final class LearnerRepo {
    public static final Path ROOT = Path.of("").toAbsolutePath();
    public static final String MAIN = "main";

    private LearnerRepo() {
    }

    public record Result(int exitCode, String out, String err) {
        public List<String> lines() {
            return out.isBlank() ? List.of() : Arrays.asList(out.strip().split("\\R"));
        }
    }

    public static Result git(String... args) {
        return git(ROOT, args);
    }

    static Result git(Path repository, String... args) {
        List<String> command = new ArrayList<>(List.of("git", "-c", "core.quotepath=off"));
        command.addAll(Arrays.asList(args));
        ProcessBuilder builder = new ProcessBuilder(command).directory(repository.toFile());
        try {
            return run(builder, Duration.ofSeconds(15), "git " + String.join(" ", args));
        } catch (IOException exception) {
            throw new AssertionError("Git is not installed or not on the PATH: " + exception.getMessage(), exception);
        }
    }

    /**
     * Runs {@code builder} with no input and its output kept in temporary files, so a full output pipe cannot stall
     * it. A process still running after {@code limit} is stopped, together with every process it started, and fails.
     */
    static Result run(ProcessBuilder builder, Duration limit, String name) throws IOException {
        Path out = Files.createTempFile("lantern-out", ".txt");
        Path err = Files.createTempFile("lantern-err", ".txt");
        try {
            builder.redirectOutput(out.toFile()).redirectError(err.toFile());
            Process process = builder.start();
            process.getOutputStream().close();
            boolean finished;
            try {
                finished = process.waitFor(limit.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                finished = false;
            }
            if (!finished) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
                fail(name + " did not finish within " + limit.toSeconds() + " seconds and was stopped");
            }
            return new Result(process.exitValue(), new String(Files.readAllBytes(out), StandardCharsets.UTF_8),
                    new String(Files.readAllBytes(err), StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(out);
            Files.deleteIfExists(err);
        }
    }

    private static boolean ok(String... args) {
        return git(args).exitCode() == 0;
    }

    /** Fails with guidance unless this project folder is the top of a Git repository with a main branch. */
    public static void requireRepository() {
        Result top = git("rev-parse", "--show-toplevel");
        assertTrue(top.exitCode() == 0,
                "This project folder is not a Git repository. Work in the folder you cloned from your own "
                        + "repository on the course Git server (it contains a .git folder).");
        try {
            assertTrue(Path.of(top.out().strip()).toRealPath().equals(ROOT.toRealPath()),
                    "Run the checks from the top folder of your repository: " + top.out().strip());
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
        assertTrue(ok("rev-parse", "--verify", "--quiet", "refs/heads/" + MAIN),
                "Your repository has no local branch named main.");
    }

    /**
     * The starting point you received: the newest commit on main whose Milestone 1 files are still untouched (the
     * script still says its summary is not written, WORKLOG.md has no name, .env.example lists no LANTERN_PORT and
     * .gitignore does not ignore .env). It depends only on file contents, so any history that came with the starter is
     * left out however it was imported.
     */
    public static String baseline() {
        return baseline(ROOT);
    }

    static String baseline(Path repository) {
        for (String commit : git(repository, "rev-list", MAIN).lines()) {
            if (untouchedStarter(repository, commit)) {
                return commit;
            }
        }
        throw new AssertionError("main has no commit with the untouched starter files, so your own commits cannot be "
                + "told apart from the starter. Ask your trainer.");
    }

    private static boolean untouchedStarter(Path repository, String commit) {
        return found(repository, commit, "-F", "summary not written yet", "bin/loan-summary.sh")
                && found(repository, commit, "-F", "Name: TODO LEARNER 1D", "WORKLOG.md")
                && !found(repository, commit, "-E", "^LANTERN_PORT=", ".env.example")
                && !found(repository, commit, "-E", "^[^#]*\\.env", ".gitignore");
    }

    private static boolean found(Path repository, String commit, String mode, String pattern, String file) {
        return git(repository, "grep", "-q", mode, "-e", pattern, commit, "--", file).exitCode() == 0;
    }

    /** Commits on main made after the starting point, newest first. */
    public static List<String> learnerCommitsOnMain(boolean includeMerges) {
        return learnerCommitsOnMain(ROOT, includeMerges);
    }

    static List<String> learnerCommitsOnMain(Path repository, boolean includeMerges) {
        List<String> args = new ArrayList<>(List.of("rev-list"));
        if (!includeMerges) {
            args.add("--no-merges");
        }
        args.add(baseline(repository) + ".." + MAIN);
        return git(repository, args.toArray(String[]::new)).lines();
    }

    /** The remote main is pushed to, for example origin; fails with guidance when main has no upstream. */
    public static String upstreamOfMain() {
        Result upstream = git("rev-parse", "--abbrev-ref", "--symbolic-full-name", MAIN + "@{upstream}");
        assertTrue(upstream.exitCode() == 0 && upstream.out().contains("/"),
                "main has no upstream branch. Push it once with: git push -u origin main");
        return upstream.out().strip();
    }

    public static void assertMainPushed() {
        String upstream = upstreamOfMain();
        assertTrue(ok("merge-base", "--is-ancestor", MAIN, upstream),
                "main has commits that are not pushed yet. Run: git push   (then rerun the check)");
    }

    /**
     * Milestone delivery: a branch named m{n}/something holds commits that change this milestone's files, it is
     * merged into main, and both the branch and main are pushed. After a merge you may delete the local branch
     * (git branch -d): the pushed copy of the branch, for example origin/m{n}/something, then counts instead.
     */
    public static void assertMilestoneBranchDelivered(int milestone, List<String> files) {
        requireRepository();
        String prefix = "m" + milestone + "/";
        String remote = remoteOfMain();
        List<String> local = git("for-each-ref", "--format=%(refname:short)", "refs/heads/" + prefix).lines();
        List<String> pushed = remote == null ? List.of()
                : git("for-each-ref", "--format=%(refname:short)", "refs/remotes/" + remote + "/" + prefix).lines();
        assertTrue(!local.isEmpty() || !pushed.isEmpty(), "Create a branch for this milestone, for example: "
                + "git switch -c " + prefix + "my-topic   (do the milestone's work and commits on it)");
        List<String> codeFiles = files.stream().filter(file -> !file.equals("WORKLOG.md")).toList();
        List<String> evidenceFiles = codeFiles.isEmpty() ? files : codeFiles;
        String base = baseline();
        String delivered = null;
        List<String> candidates = new ArrayList<>(local);
        candidates.addAll(pushed);
        for (String branch : candidates) {
            List<String> args = new ArrayList<>(List.of("rev-list", base + ".." + branch, "--"));
            args.addAll(evidenceFiles);
            boolean touches = !git(args.toArray(String[]::new)).lines().isEmpty();
            if (touches && ok("merge-base", "--is-ancestor", branch, MAIN)) {
                delivered = branch;
                break;
            }
        }
        String example = local.isEmpty() ? prefix + "my-topic" : local.get(0);
        assertTrue(delivered != null, "No " + prefix + "* branch has commits that change this milestone's files"
                + " and is merged into main. Commit your work on " + example
                + ", then: git switch main && git merge " + example);
        assertMainPushed();
        if (local.contains(delivered)) {
            String remoteBranch = remote + "/" + delivered;
            assertTrue(ok("rev-parse", "--verify", "--quiet", "refs/remotes/" + remoteBranch)
                            && ok("merge-base", "--is-ancestor", delivered, remoteBranch),
                    "Push your milestone branch too: git push -u " + remote + " " + delivered);
        }
    }

    /** The remote name of main's upstream (for example origin), or null when main has no upstream yet. */
    private static String remoteOfMain() {
        Result upstream = git("rev-parse", "--abbrev-ref", "--symbolic-full-name", MAIN + "@{upstream}");
        if (upstream.exitCode() != 0 || !upstream.out().contains("/")) {
            return null;
        }
        String name = upstream.out().strip();
        return name.substring(0, name.indexOf('/'));
    }

    /** Fails when any of {@code files} has uncommitted changes. */
    public static void assertCommitted(List<String> files) {
        requireRepository();
        List<String> args = new ArrayList<>(List.of("status", "--porcelain", "--untracked-files=all", "--"));
        args.addAll(files);
        List<String> dirty = git(args.toArray(String[]::new)).lines();
        assertTrue(dirty.isEmpty(), "Commit (and push) these changes first: " + dirty);
    }

    /** Learner files of a milestone, read from docs/milestones/manifest.tsv. */
    public static List<String> milestoneFiles(int milestone) {
        try {
            for (String line : Files.readAllLines(ROOT.resolve("docs/milestones/manifest.tsv"))) {
                String[] columns = line.split("\t", -1);
                if (columns.length > 4 && columns[0].equals(String.valueOf(milestone))) {
                    return List.of(columns[4].split(","));
                }
            }
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
        throw new AssertionError("Milestone " + milestone + " is missing from the manifest");
    }

    // ---- WORKLOG.md -------------------------------------------------------------------------------------------

    public static List<String> worklog() {
        try {
            return Files.readAllLines(ROOT.resolve("WORKLOG.md"));
        } catch (IOException exception) {
            throw new AssertionError("WORKLOG.md is missing", exception);
        }
    }

    /** "Label: value" lines of the section that starts with {@code heading}, in order (labels may repeat). */
    public static List<Map.Entry<String, String>> fields(String heading) {
        List<Map.Entry<String, String>> found = new ArrayList<>();
        boolean seen = false;
        boolean inside = false;
        for (String line : worklog()) {
            if (line.startsWith("#")) {
                inside = line.strip().equals(heading);
                seen = seen || inside;
                continue;
            }
            int colon = line.indexOf(':');
            if (inside && colon > 0 && !line.startsWith(" ") && !line.startsWith("<")) {
                found.add(Map.entry(line.substring(0, colon).strip(), line.substring(colon + 1).strip()));
            }
        }
        assertTrue(seen, "WORKLOG.md has no section " + heading + " (keep the headings from the starter).");
        return found;
    }

    /** True when a value is written in your own words: at least three words and no TODO marker. */
    public static boolean filled(String value) {
        return value != null && !value.contains("TODO") && value.strip().split("\\s+").length >= 3;
    }

    /** Asserts that the "### M{n}" entry has every field filled in. */
    public static void assertEntryComplete(int milestone, List<String> extraFields) {
        List<String> required = new ArrayList<>(List.of("Yesterday", "Today", "Blockers", "Story",
                "Security check", "AI used", "I verified"));
        required.addAll(extraFields);
        Map<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, String> field : fields("### M" + milestone)) {
            values.putIfAbsent(field.getKey(), field.getValue());
        }
        for (String name : required) {
            assertTrue(filled(values.get(name)), "WORKLOG.md, ### M" + milestone + ": fill in \"" + name
                    + ":\" with at least three words of your own (no TODO).");
        }
    }
}
