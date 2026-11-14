package com.pep.toolshare;

import com.pep.toolshare.config.SampleData;
import com.pep.toolshare.config.Stores;
import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.lending.RuleViolationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Supplied health check for the evidence your own tests are judged by: tests that never exercise the code under test
 * must earn no credit, and calls made through method references or on shared sample data must still count. The nested
 * classes are sample test classes run only through {@link LearnerTests}; the last two need the in-memory repositories
 * (Milestone 4) and are skipped before then.
 */
class LearnerTestEvidenceTest {
    private static final String SERVICE = "com.pep.toolshare.lending.LendingService";
    private static final String REPOSITORY = "com.pep.toolshare.jdbc.JdbcLoanRepository";

    @Test
    void sampleDataWithoutTheServiceIsNotAFake() {
        LearnerTests.Run run = LearnerTests.run(FakeWithoutService.class.getName(), SERVICE);
        assertEquals(0, run.passingFakeTests(), "building SampleData.inMemory() alone must not count as a fake");
    }

    @Test
    void readingTheDatabaseWithoutTheRepositoryIsNotEvidence() {
        LearnerTests.Run run = LearnerTests.run(ReadBackWithoutRepository.class.getName(), REPOSITORY);
        assertEquals(0, run.passingReadBackTests(),
                "reading a test database back without calling JdbcLoanRepository must not count");
    }

    @Test
    void aRefusalTheTestThrowsItselfIsNotEvidence() {
        LearnerTests.Run run = LearnerTests.run(SelfThrownRefusal.class.getName(), SERVICE);
        assertTrue(run.refusalsHandled().isEmpty(), "an exception the test creates itself must not count");
    }

    @Test
    void callsThroughAMethodReferenceAreCredited() {
        assumeTrue(sampleDataReady(), "the in-memory repositories are not written yet");
        LearnerTests.Run run = LearnerTests.run(MethodReferenceCaller.class.getName(),
                MethodReferenceCaller.class.getName());
        assertEquals(1, run.passingFakeTests(), "a repository call made through a method reference must count");
    }

    @Test
    void sampleDataSharedByATestClassCountsForEveryTest() {
        assumeTrue(sampleDataReady(), "the in-memory repositories are not written yet");
        LearnerTests.Run run = LearnerTests.run(SharedSampleData.class.getName(), SharedSampleData.class.getName());
        assertEquals(2, run.passingFakeTests(), "each test that uses shared sample data must count");
    }

    private static boolean sampleDataReady() {
        try {
            return SampleData.inMemory().tools().findByCode("LT-1001-2") != null;
        } catch (RuntimeException unfinished) {
            return false;
        }
    }

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

    static class FakeWithoutService {
        @Test
        void buildsTheSampleDataOnly() {
            try {
                SampleData.inMemory();
            } catch (RuntimeException unfinished) {
                // the in-memory repositories may not be written yet
            }
        }
    }

    static class ReadBackWithoutRepository {
        @Test
        void readsTheSeedRowsOnly() {
            try {
                ConnectionFactory database = TestDatabase.create();
                TestDatabase.count(database, "SELECT COUNT(*) FROM tools");
            } catch (Exception unfinished) {
                // the schema may not be written yet
            }
        }
    }

    static class SelfThrownRefusal {
        @Test
        void throwsItsOwnRefusal() {
            assertThrows(RuntimeException.class, () -> {
                throw new RuleViolationException("made by the test");
            });
        }
    }
}
