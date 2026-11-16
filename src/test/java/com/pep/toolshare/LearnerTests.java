package com.pep.toolshare;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordingFile;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
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
import java.util.HashSet;
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
    private static final List<String> SAMPLE_DATA_STORES = List.of("com.pep.toolshare.lending.InMemoryMemberRepository",
            "com.pep.toolshare.lending.InMemoryToolRepository", "com.pep.toolshare.lending.InMemoryLoanRepository");
    private static final List<String> PLATFORM = List.of("java.", "jdk.", "sun.");
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
        List<RecordedEvent> events;
        boolean finished;
        try (Recording recording = new Recording()) {
            recording.enable("jdk.JavaExceptionThrow").withStackTrace();
            recording.enable("jdk.MethodTrace").with("filter", String.join(";", SAMPLE_DATA_STORES)).withStackTrace();
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
            events = read(recording);
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
                perTest.fakes(storeCallers(events, thrower)), TestDatabase.created() - createdBefore,
                TestDatabase.reads() - readsBefore, perTest.readBacks, refusalsHandled(events, className, thrower));
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
     * Every method on the stack of a call that {@code thrower} made on the in-memory repositories: the first frame
     * above them that is not Java's own code, so a method reference such as {@code .map(tools::findByCode)} is
     * credited to the class that wrote it. Each method is named {@code Class#method}.
     */
    private static Set<String> storeCallers(List<RecordedEvent> events, String thrower) {
        Set<String> methods = new HashSet<>();
        for (RecordedEvent event : events) {
            if (!event.getEventType().getName().equals("jdk.MethodTrace") || event.getStackTrace() == null) {
                continue;
            }
            List<RecordedFrame> frames = event.getStackTrace().getFrames();
            String caller = frames.stream().map(frame -> frame.getMethod().getType().getName())
                    .filter(name -> SAMPLE_DATA_STORES.stream().noneMatch(store -> owns(store, name))
                            && PLATFORM.stream().noneMatch(name::startsWith))
                    .findFirst().orElse("");
            if (owns(thrower, caller)) {
                for (RecordedFrame frame : frames) {
                    methods.add(frame.getMethod().getType().getName() + "#" + frame.getMethod().getName());
                }
            }
        }
        return methods;
    }

    /**
     * Per passing test: did the code under test call a Mockito mock; did LendingService run on the in-memory sample
     * data while no mock was called (a fake); and did JdbcLoanRepository use a test database that the test then read
     * back? Mocks created but never called, for example in a field, do not count, and neither do calls your test makes
     * on a mock directly. A fake counts only for the test method whose own frame is on the stack of the service's
     * call, so work done once in {@code @BeforeAll} is not credited to a test.
     */
    private static final class PerTest implements TestExecutionListener {
        private final MockitoProbe mockito;
        private final String thrower;
        private final Set<Object> invocationsSeen = Collections.newSetFromMap(new IdentityHashMap<>());
        private final List<String> passedWithoutMocks = new ArrayList<>();
        private int repositoryOpensSeen = TestDatabase.repositoryOpens();
        private int readsSeen = TestDatabase.reads();
        private int withMocks;
        private int readBacks;

        PerTest(MockitoProbe mockito, String thrower) {
            this.mockito = mockito;
            this.thrower = thrower;
        }

        /** Passing tests with no mock called whose own method made the code under test use the in-memory stores. */
        int fakes(Set<String> storeCallers) {
            return (int) passedWithoutMocks.stream().filter(storeCallers::contains).count();
        }

        @Override
        public void executionStarted(TestIdentifier test) {
            if (test.isTest()) {
                if (mockito != null) {
                    invocationsSeen.addAll(mockito.invocations());
                }
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
            int repositoryOpens = TestDatabase.repositoryOpens();
            int reads = TestDatabase.reads();
            if (result.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {
                if (calls > 0) {
                    withMocks++;
                } else if (allCalls == 0 && test.getSource().orElse(null) instanceof MethodSource method) {
                    passedWithoutMocks.add(method.getClassName() + "#" + method.getMethodName());
                }
                if (repositoryOpens > repositoryOpensSeen && reads > readsSeen) {
                    readBacks++;
                }
            }
            repositoryOpensSeen = repositoryOpens;
            readsSeen = reads;
        }
    }

    /**
     * Listens to Mockito while your tests run: notes each mock created and, for each call made on a mock, the class that
     * made it. Mockito is your own test dependency (TODO 4F), so it is reached by reflection: this file must also
     * compile before you add it.
     */
    private static final class MockitoProbe implements InvocationHandler {
        private static final StackWalker WALKER = StackWalker.getInstance(
                Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE, StackWalker.Option.SHOW_HIDDEN_FRAMES));
        private static final List<String> MOCKITO = List.of("org.mockito.", "net.bytebuddy.");
        private final List<Object> mocks = new ArrayList<>();
        private final Map<Object, String> callers = Collections.synchronizedMap(new IdentityHashMap<>());
        private Object framework;
        private Object listener;
        private Method removeListener;
        private Method invocationListeners;
        private Method reportedInvocation;
        private Method invokedMock;

        static MockitoProbe attach() {
            try {
                Class<?> mockito = Class.forName("org.mockito.Mockito");
                Class<?> frameworkType = Class.forName("org.mockito.MockitoFramework");
                Class<?> listenerType = Class.forName("org.mockito.listeners.MockitoListener");
                MockitoProbe probe = new MockitoProbe();
                probe.framework = mockito.getMethod("framework").invoke(null);
                probe.listener = Proxy.newProxyInstance(LearnerTests.class.getClassLoader(), new Class<?>[] {
                        Class.forName("org.mockito.listeners.MockCreationListener"),
                        Class.forName("org.mockito.listeners.InvocationListener")}, probe);
                probe.invocationListeners = Class.forName("org.mockito.mock.MockCreationSettings")
                        .getMethod("getInvocationListeners");
                probe.reportedInvocation = Class.forName("org.mockito.listeners.MethodInvocationReport")
                        .getMethod("getInvocation");
                probe.invokedMock = Class.forName("org.mockito.invocation.InvocationOnMock").getMethod("getMock");
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

        /** Whether {@code caller} (with its nested classes, lambdas and method references) made {@code invocation}. */
        boolean madeBy(Object invocation, String caller) {
            return owns(caller, callers.getOrDefault(invocation, ""));
        }

        /**
         * Notes the class that called the mock: the first frame that is not Java's own code, Mockito, this listener or
         * the mock itself, so a method reference such as {@code .map(members::findById)} is credited to the class that
         * wrote it.
         */
        private void called(Object report) throws ReflectiveOperationException {
            Object invocation = reportedInvocation.invoke(report);
            Object mock = invokedMock.invoke(invocation);
            String caller = WALKER.walk(frames -> frames.map(StackWalker.StackFrame::getDeclaringClass)
                    .filter(type -> !type.isInstance(mock) && !owns(LearnerTests.class.getName(), type.getName())
                            && PLATFORM.stream().noneMatch(type.getName()::startsWith)
                            && MOCKITO.stream().noneMatch(type.getName()::startsWith))
                    .findFirst()).map(Class::getName).orElse("");
            callers.put(invocation, caller);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws ReflectiveOperationException {
            switch (method.getName()) {
                case "onMockCreated" -> {
                    mocks.add(args[0]);
                    ((List<Object>) invocationListeners.invoke(args[1])).add(proxy);
                }
                case "reportInvocation" -> called(args[0]);
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
