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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs one of your own test classes and records what the run actually did: how many tests ran and passed, which tests
 * called Mockito mocks and which used the in-memory sample data instead, whether stubbings and verifications happened,
 * how many fresh test databases were created and read back, and which refusals (LendingException and its subclasses)
 * the code under test threw while your tests ran.
 */
public final class LearnerTests {
    private static final String LENDING_EXCEPTION = "com.pep.toolshare.lending.LendingException";
    private static final Map<String, Run> RUNS = new ConcurrentHashMap<>();

    private LearnerTests() {
    }

    /** What one run of a learner test class did. */
    public record Run(String simpleName, long found, long succeeded, long failed, String failures,
                      boolean mockitoPresent, int mocksCreated, int usedStubbings, int verifications,
                      int passingTestsWithMocks, int passingFakeTests, int databasesCreated,
                      int databaseReads, int passingReadBackTests, List<String> refusalsHandled) {
    }

    /**
     * Runs {@code className}; asserts at least {@code minimum} tests ran and all passed. A refusal counts only when
     * {@code thrower} (the class under test) threw it, not when the test or one of its mocks created it.
     */
    public static Run assertRunsAndPasses(String className, int minimum, String thrower) {
        Run run = run(className, thrower);
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
            done = runOnce(className, thrower);
            RUNS.put(key, done);
        }
        return done;
    }

    private static Run runOnce(String className, String thrower) {
        Class<?> type;
        try {
            type = Class.forName(className, false, LearnerTests.class.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("Missing test class " + className, exception);
        }
        MockitoProbe mockito = MockitoProbe.attach();
        PerTest perTest = new PerTest(mockito, thrower);
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        int createdBefore = TestDatabase.created();
        int readsBefore = TestDatabase.reads();
        List<RecordedEvent> exceptions;
        try (Recording recording = new Recording()) {
            recording.enable("jdk.JavaExceptionThrow").withStackTrace();
            recording.start();
            try {
                LauncherDiscoveryRequest discovery = request().selectors(selectClass(type)).build();
                Launcher launcher = LauncherFactory.create();
                launcher.execute(discovery, summaryListener, perTest);
            } finally {
                recording.stop();
                if (mockito != null) {
                    mockito.detach();
                }
            }
            exceptions = read(recording);
        }
        TestExecutionSummary summary = summaryListener.getSummary();
        StringWriter failures = new StringWriter();
        summary.printFailuresTo(new PrintWriter(failures), 5);
        return new Run(type.getSimpleName(), summary.getTestsFoundCount(), summary.getTestsSucceededCount(),
                summary.getTotalFailureCount(), failures.toString(), mockito != null,
                mockito == null ? 0 : mockito.mocks.size(), mockito == null ? 0 : mockito.usedStubbings(),
                mockito == null ? 0 : mockito.verifications, perTest.withMocks, perTest.fakes,
                TestDatabase.created() - createdBefore, TestDatabase.reads() - readsBefore, perTest.readBacks,
                refusalsHandled(exceptions, className, thrower));
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
        private int callsSeen;
        private int allCallsSeen;
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
                callsSeen = mockito == null ? 0 : mockito.calls(thrower);
                allCallsSeen = mockito == null ? 0 : mockito.calls(null);
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
            int calls = mockito == null ? 0 : mockito.calls(thrower);
            int allCalls = mockito == null ? 0 : mockito.calls(null);
            int serviceCalls = SampleData.callsFrom(thrower);
            int repositoryOpens = TestDatabase.repositoryOpens();
            int reads = TestDatabase.reads();
            if (result.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {
                if (calls > callsSeen) {
                    withMocks++;
                } else if (allCalls == allCallsSeen && serviceCalls > serviceCallsSeen) {
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
        private int verifications;
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
                        Class.forName("org.mockito.listeners.MockCreationListener"),
                        Class.forName("org.mockito.listeners.VerificationListener")}, probe);
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

        /**
         * Calls made on the mocks so far; when {@code caller} is not null, only those whose calling frame, as Mockito
         * records it, is inside {@code caller}. A call passed through Java's own code (for example a method reference
         * given to {@code Optional.map}) is not credited, because that frame cannot show who wrote it. Mockito does not
         * record stubbing and verification calls as invocations.
         */
        int calls(String caller) {
            int calls = 0;
            try {
                Class<?> mockito = Class.forName("org.mockito.Mockito");
                Method details = mockito.getMethod("mockingDetails", Object.class);
                Method invocations = Class.forName("org.mockito.MockingDetails").getMethod("getInvocations");
                Method location = Class.forName("org.mockito.invocation.DescribedInvocation").getMethod("getLocation");
                for (Object mock : mocks) {
                    for (Object invocation : (Collection<?>) invocations.invoke(details.invoke(null, mock))) {
                        String from = callerClass(location.invoke(invocation).toString());
                        if (caller == null || owns(caller, from)) {
                            calls++;
                        }
                    }
                }
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Could not read Mockito invocations: " + exception, exception);
            }
            return calls;
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

        int usedStubbings() {
            int used = 0;
            try {
                Class<?> mockito = Class.forName("org.mockito.Mockito");
                Method details = mockito.getMethod("mockingDetails", Object.class);
                Method stubbings = Class.forName("org.mockito.MockingDetails").getMethod("getStubbings");
                Method wasUsed = Class.forName("org.mockito.stubbing.Stubbing").getMethod("wasUsed");
                for (Object mock : mocks) {
                    for (Object stubbing : (Collection<?>) stubbings.invoke(details.invoke(null, mock))) {
                        if ((Boolean) wasUsed.invoke(stubbing)) {
                            used++;
                        }
                    }
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
                throw new AssertionError("Could not read Mockito stubbings: " + exception, exception);
            } catch (InvocationTargetException exception) {
                throw new AssertionError("Could not read Mockito stubbings: " + exception.getCause(), exception);
            }
            return used;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "onMockCreated" -> mocks.add(args[0]);
                case "onVerification" -> verifications++;
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
