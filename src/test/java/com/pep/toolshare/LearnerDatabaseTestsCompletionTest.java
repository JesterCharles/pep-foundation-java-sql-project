package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 5: your own tests for the JDBC loan repository against a throwaway test database. */
class LearnerDatabaseTestsCompletionTest {
    private static final String CLASS_NAME = "com.pep.toolshare.learner.JdbcLoanRepositoryTest";
    private static final String REPOSITORY = "com.pep.toolshare.jdbc.JdbcLoanRepository";

    @Test
    void yourRepositoryTestsRunAndPass() {
        LearnerTests.assertRunsAndPasses(CLASS_NAME, 3, REPOSITORY);
    }

    @Test
    void yourTestsUseAFreshDatabaseAndCheckARefusal() {
        LearnerTests.Run run = LearnerTests.assertRunsAndPasses(CLASS_NAME, 3, REPOSITORY);
        assertTrue(run.databasesCreated() >= run.found(), "create at least as many fresh databases with "
                + "TestDatabase.create() as you have tests (one per test); " + run.found() + " tests created "
                + run.databasesCreated());
        assertTrue(run.passingReadBackTests() > 0, "in at least one test, call JdbcLoanRepository on the database "
                + "from TestDatabase.create(), then read the tables back with TestDatabase.count or TestDatabase.text "
                + "to prove what it saved");
        assertFalse(run.refusalsHandled().isEmpty(), "while your tests run, JdbcLoanRepository itself must throw a "
                + "LendingException for a refused checkout or checkin; expect it with assertThrows");
    }
}
