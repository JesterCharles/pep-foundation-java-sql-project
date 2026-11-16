package com.pep.toolshare;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Captures what the SLF4J simple logger writes to standard error while a block runs. */
public final class LogCapture {
    private LogCapture() {
    }

    public static String during(Runnable action) {
        PrintStream original = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } catch (RuntimeException ignored) {
            // The caller asserts on the log; failures it expects are part of the scenario.
        } finally {
            System.setErr(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
