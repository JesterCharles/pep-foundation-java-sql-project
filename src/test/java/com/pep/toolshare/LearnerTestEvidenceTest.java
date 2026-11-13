package com.pep.toolshare;

import com.pep.toolshare.config.SampleData;
import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.lending.RuleViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Supplied health check for the evidence your own tests are judged by: tests that never exercise the code under test
 * must earn no credit. The nested classes are sample test classes run only through {@link LearnerTests}.
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
