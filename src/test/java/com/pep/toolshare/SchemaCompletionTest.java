package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Week 5: tables, keys, data types and constraints that protect the data even when Java code is wrong. */
class SchemaCompletionTest {
    private ConnectionFactory db;

    @BeforeEach
    void freshDatabase() {
        db = TestDatabase.create();
    }

    private void rejects(String why, String sql) {
        try {
            TestDatabase.execute(db, sql);
        } catch (SQLException expected) {
            assertTrue(expected.getSQLState() != null && expected.getSQLState().startsWith("23"),
                    why + ": expected a constraint violation (SQLState 23...), got " + expected.getSQLState()
                            + " " + expected.getMessage());
            return;
        }
        fail("The schema accepted a row it must reject (" + why + "): " + sql);
    }

    private static final String MEMBER = "INSERT INTO members (id, full_name, email, tier, active, referred_by) VALUES ";
    private static final String TOOL = "INSERT INTO tools (code, name, category_code, replacement_cents, status, shelf) "
            + "VALUES ";
    private static final String LOAN = "INSERT INTO loans (member_id, tool_code, loaned_on, due_on, returned_on, "
            + "fee_cents) VALUES ";

    @Test
    void seedDataLoadsIntoTheSchema() throws SQLException {
        assertEquals(5, TestDatabase.count(db, "SELECT COUNT(*) FROM categories"));
        assertEquals(6, TestDatabase.count(db, "SELECT COUNT(*) FROM members"));
        assertEquals(12, TestDatabase.count(db, "SELECT COUNT(*) FROM tools"));
        assertEquals(14, TestDatabase.count(db, "SELECT COUNT(*) FROM loans"));
        assertEquals(4, TestDatabase.count(db, "SELECT COUNT(*) FROM loans WHERE returned_on IS NULL"));
    }

    @Test
    void membersHaveKeysUniqueEmailsAndValidTiers() {
        rejects("duplicate member id", MEMBER + "('M-0001', 'Copy', 'copy@example.test', 'STARTER', TRUE, NULL)");
        rejects("duplicate email", MEMBER + "('M-0100', 'Copy', 'fern.ito@example.test', 'STARTER', TRUE, NULL)");
        rejects("unknown tier", MEMBER + "('M-0100', 'New', 'new@example.test', 'GOLD', TRUE, NULL)");
        rejects("missing name", MEMBER + "('M-0100', NULL, 'new@example.test', 'STARTER', TRUE, NULL)");
        rejects("unknown referrer", MEMBER + "('M-0100', 'New', 'new@example.test', 'STARTER', TRUE, 'M-0999')");
        rejects("self referral", MEMBER + "('M-0100', 'New', 'new@example.test', 'STARTER', TRUE, 'M-0100')");
    }

    @Test
    void memberActiveDefaultsToTrue() throws SQLException {
        TestDatabase.execute(db, "INSERT INTO members (id, full_name, email, tier) VALUES "
                + "('M-0100', 'New Member', 'new@example.test', 'STARTER')");
        assertEquals("TRUE", TestDatabase.text(db, "SELECT active FROM members WHERE id = 'M-0100'").toUpperCase());
    }

    @Test
    void toolsBelongToACategoryAndKeepValidValues() {
        rejects("duplicate tool code", TOOL + "('LT-1001-2', 'Copy', 'HAND', 100, 'AVAILABLE', 'A-01')");
        rejects("unknown category", TOOL + "('LT-2000-2', 'Pump', 'PLUMBING', 100, 'AVAILABLE', 'A-01')");
        rejects("free tool", TOOL + "('LT-2000-2', 'Pump', 'HAND', 0, 'AVAILABLE', 'A-01')");
        rejects("unknown status", TOOL + "('LT-2000-2', 'Pump', 'HAND', 100, 'LOST', 'A-01')");
        rejects("missing shelf", TOOL + "('LT-2000-2', 'Pump', 'HAND', 100, 'AVAILABLE', NULL)");
    }

    @Test
    void toolStatusDefaultsToAvailable() throws SQLException {
        TestDatabase.execute(db, "INSERT INTO tools (code, name, category_code, replacement_cents, shelf) VALUES "
                + "('LT-2000-2', 'Pump', 'HAND', 100, 'A-04')");
        assertEquals("AVAILABLE", TestDatabase.text(db, "SELECT status FROM tools WHERE code = 'LT-2000-2'"));
    }

    @Test
    void loansReferToRealMembersAndTools() {
        rejects("unknown member", LOAN + "('M-0999', 'LT-1001-2', DATE '2026-10-01', DATE '2026-10-08', NULL, 0)");
        rejects("unknown tool", LOAN + "('M-0001', 'LT-2000-2', DATE '2026-10-01', DATE '2026-10-08', NULL, 0)");
        rejects("deleting a member with loans", "DELETE FROM members WHERE id = 'M-0001'");
        rejects("deleting a tool with loans", "DELETE FROM tools WHERE code = 'LT-1001-2'");
    }

    @Test
    void loanDatesAndFeesAreChecked() {
        rejects("due before loaned", LOAN + "('M-0006', 'LT-1001-2', DATE '2026-10-08', DATE '2026-10-01', NULL, 0)");
        rejects("returned before loaned",
                LOAN + "('M-0006', 'LT-1001-2', DATE '2026-10-08', DATE '2026-10-09', DATE '2026-10-07', 0)");
        rejects("negative fee", LOAN + "('M-0006', 'LT-1001-2', DATE '2026-10-08', DATE '2026-10-09', NULL, -1)");
        rejects("missing loan date", LOAN + "('M-0006', 'LT-1001-2', NULL, DATE '2026-10-09', NULL, 0)");
    }

    @Test
    void loanIdsAreGeneratedAndFeeDefaultsToZero() throws SQLException {
        TestDatabase.execute(db, "INSERT INTO loans (member_id, tool_code, loaned_on, due_on) VALUES "
                + "('M-0006', 'LT-1001-2', DATE '2026-10-15', DATE '2026-10-29')");
        assertEquals(15, TestDatabase.count(db, "SELECT MAX(id) FROM loans"));
        assertEquals(0, TestDatabase.count(db, "SELECT fee_cents FROM loans WHERE id = 15"));
    }

    @Test
    void openLoansByMemberHaveAnIndex() throws SQLException {
        long matching = TestDatabase.count(db, """
                SELECT COUNT(*) FROM information_schema.index_columns first_column
                JOIN information_schema.index_columns second_column
                  ON second_column.index_name = first_column.index_name
                 AND second_column.table_name = first_column.table_name
                WHERE LOWER(first_column.table_name) = 'loans'
                  AND LOWER(first_column.column_name) = 'member_id' AND first_column.ordinal_position = 1
                  AND LOWER(second_column.column_name) = 'returned_on' AND second_column.ordinal_position = 2
                """);
        assertTrue(matching >= 1, "create an index on loans (member_id, returned_on)");
    }
}
