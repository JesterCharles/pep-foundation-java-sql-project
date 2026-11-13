package com.pep.toolshare;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs one of your own test classes and reads its source, so the checks can confirm the tests you wrote. */
public final class LearnerTests {
    private LearnerTests() {
    }

    /** Runs {@code className}; asserts at least {@code minimum} tests ran and all passed. */
    public static void assertRunsAndPasses(String className, int minimum) {
        Class<?> type;
        try {
            type = Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("Missing test class " + className, exception);
        }
        LauncherDiscoveryRequest discovery = request().selectors(selectClass(type)).build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.execute(discovery, listener);
        TestExecutionSummary summary = listener.getSummary();
        StringWriter failures = new StringWriter();
        summary.printFailuresTo(new PrintWriter(failures), 5);
        assertTrue(summary.getTestsFoundCount() >= minimum, type.getSimpleName() + " needs at least " + minimum
                + " @Test methods; found " + summary.getTestsFoundCount());
        assertEquals(0, summary.getTotalFailureCount(), "your tests must pass:\n" + failures);
        assertEquals(summary.getTestsFoundCount(), summary.getTestsSucceededCount(),
                "every one of your tests must run and pass (no disabled or aborted tests)");
    }

    public static String source(String relativePath) {
        try {
            return Files.readString(Path.of("src/test/java").resolve(relativePath));
        } catch (IOException exception) {
            throw new AssertionError("Missing " + relativePath, exception);
        }
    }
}
