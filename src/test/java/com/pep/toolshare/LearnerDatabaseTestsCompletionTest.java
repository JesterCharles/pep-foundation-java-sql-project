package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 5: your own tests for the JDBC loan repository against a throwaway test database. */
class LearnerDatabaseTestsCompletionTest {
    private static final String CLASS_NAME = "com.pep.toolshare.learner.JdbcLoanRepositoryTest";

    @Test
    void yourRepositoryTestsRunAndPass() {
        LearnerTests.assertRunsAndPasses(CLASS_NAME, 3);
    }

    @Test
    void yourTestsUseAFreshDatabaseAndCheckARefusal() {
        LearnerTests.Run run = LearnerTests.assertRunsAndPasses(CLASS_NAME, 3);
        assertTrue(run.databasesCreated() >= run.found(), "start every test from its own fresh database with "
                + "TestDatabase.create(); " + run.found() + " tests created " + run.databasesCreated());
        assertTrue(run.databaseReads() > 0,
                "read the tables back with TestDatabase.count or TestDatabase.text to prove what was saved");
        assertFalse(run.refusalsHandled().isEmpty(), "check that a refused checkout or checkin throws: one of your "
                + "tests must make the repository throw a LendingException and expect it (assertThrows)");
    }
}
