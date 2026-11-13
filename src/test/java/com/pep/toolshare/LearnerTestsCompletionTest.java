package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 4: your own JUnit tests for the service, with a fake, a Mockito mock and an exception assertion. */
class LearnerTestsCompletionTest {
    private static final String CLASS_NAME = "com.pep.toolshare.learner.LendingServiceTest";
    private static final String SOURCE = "com/pep/toolshare/learner/LendingServiceTest.java";

    @Test
    void pomDeclaresMockitoForTestsOnly() throws IOException {
        String pom = Files.readString(LearnerRepo.ROOT.resolve("pom.xml"));
        Matcher dependency = Pattern.compile("<dependency>((?:(?!</dependency>).)*?<artifactId>mockito-core"
                + "</artifactId>(?:(?!</dependency>).)*)</dependency>", Pattern.DOTALL).matcher(pom);
        assertTrue(dependency.find(), "declare the org.mockito:mockito-core dependency in pom.xml");
        assertTrue(dependency.group(1).contains("<scope>test</scope>"),
                "Mockito is only for tests: give it <scope>test</scope>");
    }

    @Test
    void yourServiceTestsRunAndPass() {
        LearnerTests.assertRunsAndPasses(CLASS_NAME, 4);
    }

    @Test
    void yourTestsUseAnExceptionAssertionAFakeAndAMock() {
        String source = LearnerTests.source(SOURCE);
        assertTrue(source.contains("assertThrows("), "use assertThrows for at least one refused request");
        assertTrue(source.contains("SampleData.inMemory()") || source.contains("new InMemory"),
                "use the in-memory repositories as a fake in at least one test");
        assertTrue(source.contains("mock(") && source.contains("when(") && source.contains("verify("),
                "use a Mockito mock: mock(...), when(...).thenReturn(...) and verify(...)");
    }
}
