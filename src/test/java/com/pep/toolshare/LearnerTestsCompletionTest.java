package com.pep.toolshare;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

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
        assertFalse(run.refusalsHandled().isEmpty(), "use assertThrows for at least one refused request: one of "
                + "your tests must make LendingService itself throw a LendingException (not throw one from the test "
                + "or a mock) and expect it");
        assertTrue(run.passingFakeTests() > 0, "at least one test must give LendingService the in-memory "
                + "repositories from SampleData.inMemory() as a fake and call the service, with no Mockito mock called");
        assertTrue(run.passingTestsWithMocks() > 0, "in at least one test, give LendingService a mock(...) "
                + "and make the service call it");
        assertTrue(run.usedStubbings() > 0, "stub a mock with when(...).thenReturn(...) and make sure the stubbed "
                + "call happens");
        assertTrue(run.verifications() > 0, "check a call on a mock with verify(...)");
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
