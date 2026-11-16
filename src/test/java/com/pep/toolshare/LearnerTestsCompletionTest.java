package com.pep.toolshare;

import com.pep.toolshare.config.SampleData;
import com.pep.toolshare.config.Stores;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 4: your own JUnit tests for the service, with a fake, a Mockito mock and an exception assertion. */
class LearnerTestsCompletionTest {
    private static final String CLASS_NAME = "com.pep.toolshare.learner.LendingServiceTest";
    private static final String SERVICE = "com.pep.toolshare.lending.LendingService";

    @Test
    void pomDeclaresMockitoForTestsOnly() throws Exception {
        Document pom = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(LearnerRepo.ROOT.resolve("pom.xml").toFile());
        Element mockito = null;
        NodeList dependencies = pom.getElementsByTagName("dependency");
        for (int i = 0; i < dependencies.getLength(); i++) {
            Element dependency = (Element) dependencies.item(i);
            if (child(dependency, "groupId").equals("org.mockito") && child(dependency, "artifactId")
                    .equals("mockito-core") && dependency.getParentNode().getParentNode().getNodeName()
                    .equals("project")) {
                mockito = dependency;
            }
        }
        assertNotNull(mockito, "declare the org.mockito:mockito-core dependency in pom.xml");
        assertEquals("test", child(mockito, "scope"), "Mockito is only for tests: give it <scope>test</scope>");
    }

    @Test
    void yourServiceTestsRunAndPass() {
        LearnerTests.assertRunsAndPasses(CLASS_NAME, 4, SERVICE);
    }

    @Test
    void yourTestsUseAnExceptionAssertionAFakeAndAMock() {
        LearnerTests.Run run = LearnerTests.assertRunsAndPasses(CLASS_NAME, 4, SERVICE);
        assertTrue(run.mockitoPresent(), "add Mockito to pom.xml (TODO 4F) so your tests can use it");
        assertFalse(run.refusalsHandled().isEmpty(), "while your tests run, LendingService itself must throw a "
                + "LendingException (one thrown by the test or a mock does not count); expect it with assertThrows");
        assertTrue(run.passingFakeTests() > 0, "at least one test must give LendingService the in-memory "
                + "repositories from SampleData.inMemory() as a fake and call the service, with no Mockito mock called");
        assertTrue(run.passingTestsWithMocks() > 0, "in at least one test, give LendingService a mock(...) "
                + "and make the service call it");
        // How you stub (when(...).thenReturn(...)) and verify(...) that mock is reviewed in your walkthrough.
    }

    @Test
    void theEvidenceCountsMethodReferencesAndSharedSampleData() {
        String caller = MethodReferenceCaller.class.getName();
        assertEquals(1, LearnerTests.run(caller, caller).passingFakeTests(),
                "a repository call made through a method reference must count (checks the checker; needs 4C)");
        String shared = SharedSampleData.class.getName();
        assertEquals(2, LearnerTests.run(shared, shared).passingFakeTests(),
                "each test that uses sample data shared by its class must count (checks the checker; needs 4C)");
    }

    @Test
    void theEvidenceDoesNotCreditOneTimeSetupToATest() {
        String setup = SetupOnly.class.getName();
        assertEquals(0, LearnerTests.run(setup, setup).passingFakeTests(),
                "work done once in @BeforeAll must not count for a test (checks the checker; needs 4C)");
    }

    /** Sample test classes for the checks above; they stand in for the class under test themselves. */
    static class MethodReferenceCaller {
        @Test
        void looksUpThroughAMethodReference() {
            Stores stores = SampleData.inMemory();
            assertTrue(Optional.of("LT-1001-2").map(stores.tools()::findByCode).isPresent());
        }
    }

    static class SharedSampleData {
        private static Stores stores;

        @BeforeAll
        static void buildOnce() {
            stores = SampleData.inMemory();
        }

        @Test
        void readsTools() {
            assertEquals(12, stores.tools().findAll().size());
        }

        @Test
        void readsMembers() {
            assertEquals(6, stores.members().findAll().size());
        }
    }

    static class SetupOnly {
        @BeforeAll
        static void useTheStoresOnce() {
            SampleData.inMemory().tools().findAll();
        }

        @Test
        void doesNothing() {
            assertTrue(true);
        }
    }

    private static String child(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element element && element.getTagName().equals(name)) {
                return element.getTextContent().strip();
            }
        }
        return "";
    }
}
