package com.pep.toolshare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 5: your own tests for the JDBC loan repository against a throwaway test database. */
class LearnerDatabaseTestsCompletionTest {
    private static final String CLASS_NAME = "com.pep.toolshare.learner.JdbcLoanRepositoryTest";
    private static final String SOURCE = "com/pep/toolshare/learner/JdbcLoanRepositoryTest.java";

    @Test
    void yourRepositoryTestsRunAndPass() {
        LearnerTests.assertRunsAndPasses(CLASS_NAME, 3);
    }

    @Test
    void yourTestsUseAFreshDatabaseAndCheckARefusal() {
        String source = LearnerTests.source(SOURCE);
        assertTrue(source.contains("TestDatabase.create()"),
                "start each test from a fresh database with TestDatabase.create()");
        assertTrue(source.contains("assertThrows("), "check that a refused checkout or checkin throws");
        assertTrue(source.contains("TestDatabase.count(") || source.contains("TestDatabase.text("),
                "read the tables back with TestDatabase.count or TestDatabase.text to prove what was saved");
    }
}
