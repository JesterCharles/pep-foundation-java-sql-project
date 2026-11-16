package com.pep.toolshare;

import com.pep.toolshare.config.SampleData;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordingFile;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs one of your own test classes and records what the run actually did: how many tests ran and passed, which tests
 * made the code under test call a Mockito mock and which used the in-memory sample data instead, how many fresh test
 * databases were created and read back, and which refusals (LendingException and its subclasses) the code under test
 * threw while your tests ran. How you stub and verify your mocks is reviewed with your trainer, not checked here.
 * A run that has not finished after {@link #RUN_LIMIT} is stopped and fails, so a loop that never ends cannot hang
 * the checks.
 */
public final class LearnerTests {
    private static final String LENDING_EXCEPTION = "com.pep.toolshare.lending.LendingException";
    private static final Map<String, Run> RUNS = new ConcurrentHashMap<>();
    /** How long one run of your test class may take. */
    static final Duration RUN_LIMIT = Duration.ofSeconds(10);

    private LearnerTests() {
    }

    /** What one run of a learner test class did. */
    public record Run(String simpleName, boolean finished, long found, long succeeded, long failed,
                      String failures, boolean mockitoPresent, int passingTestsWithMocks, int passingFakeTests,
                      int databasesCreated, int databaseReads, int passingReadBackTests,
                      List<String> refusalsHandled) {
    }

    /**
     * Runs {@code className}; asserts at least {@code minimum} tests ran and all passed. A refusal counts only when
     * {@code thrower} (the class under test) threw it, not when the test or one of its mocks created it.
     */
    public static Run assertRunsAndPasses(String className, int minimum, String thrower) {
        Run run = run(className, thrower);
        assertTrue(run.finished(), run.simpleName() + " did not finish within " + RUN_LIMIT.toSeconds()
                + " seconds and was stopped; look for a loop or a wait that never ends in your tests or the code they "
                + "call");
        assertTrue(run.found() >= minimum, run.simpleName() + " needs at least " + minimum
                + " @Test methods; found " + run.found());
        assertEquals(0, run.failed(), "your tests must pass:\n" + run.failures());
        assertEquals(run.found(), run.succeeded(),
                "every one of your tests must run and pass (no disabled or aborted tests)");
        return run;
    }

    /**
     * Runs {@code className} through the JUnit Platform once per check run and records the evidence described above;
     * later calls reuse that result, so state your tests keep in static fields is not carried into a second run.
     */
    public static synchronized Run run(String className, String thrower) {
        String key = className + " " + thrower;
        Run done = RUNS.get(key);
        if (done == null) {
            done = runOnce(className, thrower, RUN_LIMIT);
            RUNS.put(key, done);
        }
        return done;
    }

    /** Runs {@code className} with a time limit; checks of this checker use a shorter limit than {@link #RUN_LIMIT}. */
    static Run runOnce(String className, String thrower, Duration limit) {
        Class<?> type;
        try {
            type = Class.forName(className, false, LearnerTests.class.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("Missing test class " + className, exception);
        }
        Worker worker = new Worker(type, thrower);
        int createdBefore = TestDatabase.created();
        int readsBefore = TestDatabase.reads();
        List<RecordedEvent> exceptions;
        boolean finished;
        try (Recording recording = new Recording()) {
            recording.enable("jdk.JavaExceptionThrow").withStackTrace();
            recording.start();
            // The tests run on their own daemon thread, so one that never ends can be left behind when the limit is up.
            Thread thread = Thread.ofPlatform().daemon().name("learner-tests " + type.getSimpleName())
                    .unstarted(worker);
            thread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
            thread.start();
            try {
                finished = thread.join(limit);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                finished = false;
            } finally {
                recording.stop();
            }
            if (!finished) {
                thread.interrupt();
                return new Run(type.getSimpleName(), false, 0, 0, 0, "", false, 0, 0, 0, 0, 0, List.of());
            }
            exceptions = read(recording);
        }
        if (worker.error != null) {
            throw new AssertionError("Could not run " + type.getSimpleName() + ": " + worker.error, worker.error);
        }
        TestExecutionSummary summary = worker.summary.getSummary();
        StringWriter failures = new StringWriter();
        summary.printFailuresTo(new PrintWriter(failures), 5);
        PerTest perTest = worker.perTest;
        return new Run(type.getSimpleName(), true, summary.getTestsFoundCount(), summary.getTestsSucceededCount(),
                summary.getTotalFailureCount(), failures.toString(), worker.mockito != null, perTest.withMocks,
                perTest.fakes, TestDatabase.created() - createdBefore, TestDatabase.reads() - readsBefore,
                perTest.readBacks, refusalsHandled(exceptions, className, thrower));
    }

    /**
     * Runs the test class through the JUnit Platform on its own thread. Mockito keeps its listeners per thread, so the
     * listener is attached here, on the thread that runs your tests.
     */
    private static final class Worker implements Runnable {
        private final Class<?> type;
        private final String thrower;
        private final SummaryGeneratingListener summary = new SummaryGeneratingListener();
        private MockitoProbe mockito;
        private PerTest perTest;
        private Throwable error;

        Worker(Class<?> type, String thrower) {
            this.type = type;
            this.thrower = thrower;
        }

        @Override
        public void run() {
            try {
                mockito = MockitoProbe.attach();
                perTest = new PerTest(mockito, thrower);
                try {
                    LauncherDiscoveryRequest discovery = request().selectors(selectClass(type)).build();
                    Launcher launcher = LauncherFactory.create();
                    launcher.execute(discovery, summary, perTest);
                } finally {
                    if (mockito != null) {
                        mockito.detach();
                    }
                }
            } catch (Throwable throwable) {
                error = throwable;
            }
        }
    }

    private static List<RecordedEvent> read(Recording recording) {
        try {
            Path file = Files.createTempFile("learner-tests", ".jfr");
            try {
                recording.dump(file);
                return RecordingFile.readAllEvents(file);
            } finally {
                Files.deleteIfExists(file);
            }
        } catch (IOException exception) {
            throw new AssertionError("Could not read the test-run recording: " + exception, exception);
        }
    }

    /**
     * LendingException types that {@code thrower} created while one of your test methods was running: reading the
     * stack from the new exception outwards, a frame of {@code thrower} comes before any frame of your test class.
     */
    private static List<String> refusalsHandled(List<RecordedEvent> events, String className, String thrower) {
        Class<?> lendingException;
        try {
            lendingException = Class.forName(LENDING_EXCEPTION);
        } catch (ClassNotFoundException exception) {
            return List.of();
        }
        List<String> found = new ArrayList<>();
        for (RecordedEvent event : events) {
            if (!event.getEventType().getName().equals("jdk.JavaExceptionThrow") || event.getStackTrace() == null) {
                continue;
            }
            String thrown = event.getClass("thrownClass").getName();
            boolean refusal;
            try {
                refusal = lendingException.isAssignableFrom(Class.forName(thrown, false,
                        LearnerTests.class.getClassLoader()));
            } catch (ClassNotFoundException | LinkageError exception) {
                refusal = false;
            }
            if (!refusal) {
                continue;
            }
            boolean thrownByCodeUnderTest = false;
            for (RecordedFrame frame : event.getStackTrace().getFrames()) {
                String owner = frame.getMethod().getType().getName();
                if (owns(thrower, owner)) {
                    thrownByCodeUnderTest = true;
                } else if (owns(className, owner)) {
                    if (thrownByCodeUnderTest) {
                        found.add(thrown.substring(thrown.lastIndexOf('.') + 1));
                    }
                    break;
                }
            }
        }
        return found;
    }

    private static boolean owns(String type, String owner) {
        return owner.equals(type) || owner.startsWith(type + "$");
    }

    /**
     * Per passing test: did the code under test call a Mockito mock; did LendingService run on the in-memory sample
     * data while no mock was called (a fake); and did JdbcLoanRepository use a test database that the test then read
     * back? Mocks created but never called, for example in a field, do not count, and neither do calls your test makes
     * on a mock directly.
     */
    private static final class PerTest implements TestExecutionListener {
        private final MockitoProbe mockito;
        private final String thrower;
        private final Set<Object> invocationsSeen = Collections.newSetFromMap(new IdentityHashMap<>());
        private int serviceCallsSeen;
        private int repositoryOpensSeen = TestDatabase.repositoryOpens();
        private int readsSeen = TestDatabase.reads();
        private int withMocks;
        private int fakes;
        private int readBacks;

        PerTest(MockitoProbe mockito, String thrower) {
            this.mockito = mockito;
            this.thrower = thrower;
            this.serviceCallsSeen = SampleData.callsFrom(thrower);
        }

        @Override
        public void executionStarted(TestIdentifier test) {
            if (test.isTest()) {
                if (mockito != null) {
                    invocationsSeen.addAll(mockito.invocations());
                }
                serviceCallsSeen = SampleData.callsFrom(thrower);
                repositoryOpensSeen = TestDatabase.repositoryOpens();
                readsSeen = TestDatabase.reads();
            }
        }

        @Override
        public void executionFinished(TestIdentifier test, TestExecutionResult result) {
            if (!test.isTest()) {
                return;
            }
            // This test's mock calls are the invocations that were not there when it started. Counting objects rather
            // than totals keeps a reset(...) in @BeforeEach, which clears earlier invocations, from hiding them.
            int calls = 0;
            int allCalls = 0;
            if (mockito != null) {
                for (Object invocation : mockito.invocations()) {
                    if (invocationsSeen.add(invocation)) {
                        allCalls++;
                        if (mockito.madeBy(invocation, thrower)) {
                            calls++;
                        }
                    }
                }
            }
            int serviceCalls = SampleData.callsFrom(thrower);
            int repositoryOpens = TestDatabase.repositoryOpens();
            int reads = TestDatabase.reads();
            if (result.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {
                if (calls > 0) {
                    withMocks++;
                } else if (allCalls == 0 && serviceCalls > serviceCallsSeen) {
                    fakes++;
                }
                if (repositoryOpens > repositoryOpensSeen && reads > readsSeen) {
                    readBacks++;
                }
            }
            serviceCallsSeen = serviceCalls;
            repositoryOpensSeen = repositoryOpens;
            readsSeen = reads;
        }
    }

    /**
     * Listens to Mockito while your tests run. Mockito is your own test dependency (TODO 4F), so it is reached by
     * reflection: this file must also compile before you add it.
     */
    private static final class MockitoProbe implements InvocationHandler {
        private final List<Object> mocks = new ArrayList<>();
        private Object framework;
        private Object listener;
        private Method removeListener;

        static MockitoProbe attach() {
            try {
                Class<?> mockito = Class.forName("org.mockito.Mockito");
                Class<?> frameworkType = Class.forName("org.mockito.MockitoFramework");
                Class<?> listenerType = Class.forName("org.mockito.listeners.MockitoListener");
                MockitoProbe probe = new MockitoProbe();
                probe.framework = mockito.getMethod("framework").invoke(null);
                probe.listener = Proxy.newProxyInstance(LearnerTests.class.getClassLoader(), new Class<?>[] {
                        Class.forName("org.mockito.listeners.MockCreationListener")}, probe);
                frameworkType.getMethod("addListener", listenerType).invoke(probe.framework, probe.listener);
                probe.removeListener = frameworkType.getMethod("removeListener", listenerType);
                return probe;
            } catch (ClassNotFoundException exception) {
                return null;
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Could not listen to Mockito: " + exception, exception);
            }
        }

        void detach() {
            try {
                removeListener.invoke(framework, listener);
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Could not stop listening to Mockito: " + exception, exception);
            }
        }

        /** Every invocation Mockito currently holds on the mocks seen so far (stubbing and verify calls excluded). */
        List<Object> invocations() {
            List<Object> all = new ArrayList<>();
            try {
                Class<?> mockito = Class.forName("org.mockito.Mockito");
                Method details = mockito.getMethod("mockingDetails", Object.class);
                Method invocations = Class.forName("org.mockito.MockingDetails").getMethod("getInvocations");
                for (Object mock : mocks) {
                    all.addAll((Collection<?>) invocations.invoke(details.invoke(null, mock)));
                }
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Could not read Mockito invocations: " + exception, exception);
            }
            return all;
        }

        /**
         * Whether Mockito records {@code caller} as the class that made {@code invocation}. A call passed through
         * Java's own code (for example a method reference given to {@code Optional.map}) is not credited, because that
         * frame cannot show who wrote it.
         */
        boolean madeBy(Object invocation, String caller) {
            try {
                Method location = Class.forName("org.mockito.invocation.DescribedInvocation").getMethod("getLocation");
                return owns(caller, callerClass(location.invoke(invocation).toString()));
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Could not read a Mockito invocation: " + exception, exception);
            }
        }

        /** The class in a Mockito location such as {@code -> at com.example.Type.method(Type.java:12)}. */
        private static String callerClass(String location) {
            String frame = location.strip();
            int at = frame.indexOf("at ");
            int paren = frame.indexOf('(');
            if (at < 0 || paren < at) {
                return "";
            }
            String method = frame.substring(at + 3, paren);
            int dot = method.lastIndexOf('.');
            return dot < 0 ? "" : method.substring(0, dot);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "onMockCreated" -> mocks.add(args[0]);
                case "hashCode" -> {
                    return System.identityHashCode(proxy);
                }
                case "equals" -> {
                    return proxy == args[0];
                }
                case "toString" -> {
                    return "learner test Mockito listener";
                }
                default -> {
                    return null;
                }
            }
            return null;
        }
    }
}
