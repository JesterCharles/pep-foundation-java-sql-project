package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.jdbc.SqlReports;
import com.pep.toolshare.reports.CategoryUsage;
import com.pep.toolshare.reports.MemberBalance;
import com.pep.toolshare.reports.MemberRef;
import com.pep.toolshare.reports.OverdueLoan;
import com.pep.toolshare.reports.Referral;
import com.pep.toolshare.reports.TopTool;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Week 5: reports answered by SQL joins, grouping, subqueries and window functions over the sample data. */
class SqlReportsCompletionTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private final ConnectionFactory db = TestDatabase.create();
    private final SqlReports reports = new SqlReports(db);

    @Test
    void categoryUsageIncludesCategoriesWithoutTools() {
        assertEquals(List.of(
                new CategoryUsage("CLEANING", "Cleaning", 0, 0, 0, "NONE"),
                new CategoryUsage("GARDEN", "Garden", 3, 1, 3, "NORMAL"),
                new CategoryUsage("HAND", "Hand tools", 4, 1, 4, "HIGH"),
                new CategoryUsage("LADDERS", "Ladders and access", 2, 1, 3, "NORMAL"),
                new CategoryUsage("POWER", "Power tools", 3, 1, 4, "HIGH")), reports.categoryUsage());
    }

    @Test
    void busyCategoriesFilterGroups() {
        assertEquals(List.of("HAND", "POWER"), reports.busyCategories(4));
        assertEquals(List.of("GARDEN", "HAND", "LADDERS", "POWER"), reports.busyCategories(1));
        assertEquals(List.of(), reports.busyCategories(5));
    }

    @Test
    void overdueLoansAreOpenAndPastDueOldestFirst() {
        assertEquals(List.of(
                new OverdueLoan(9, "M-0003", "Cleo Marsh", "LT-1003-4", "Cordless drill", "2026-10-09", 6),
                new OverdueLoan(11, "M-0004", "Dana Whitlow", "LT-1009-1", "Step ladder", "2026-10-12", 3),
                new OverdueLoan(10, "M-0002", "Bram Okafor", "LT-1006-7", "Hedge trimmer", "2026-10-13", 2)),
                reports.overdueLoans(DAY));
        assertEquals(4, reports.overdueLoans(DAY.plusDays(1)).size(), "loan 8 is due on 2026-10-15");
        assertEquals(List.of(), reports.overdueLoans(LocalDate.of(2026, 10, 9)));
    }

    @Test
    void membersWithoutLoansAndReferrals() {
        assertEquals(List.of(new MemberRef("M-0006", "Fern Ito")), reports.membersWithoutLoans());
        assertEquals(List.of(
                new Referral("M-0002", "Bram Okafor", "M-0001", "Avery Quill"),
                new Referral("M-0003", "Cleo Marsh", "M-0001", "Avery Quill"),
                new Referral("M-0004", "Dana Whitlow", "M-0002", "Bram Okafor")), reports.referrals());
    }

    @Test
    void topToolsAreRankedWithinEachCategory() {
        assertEquals(List.of(
                new TopTool("GARDEN", 1, "LT-1006-7", "Hedge trimmer", 2),
                new TopTool("GARDEN", 2, "LT-1007-8", "Lawn aerator", 1),
                new TopTool("HAND", 1, "LT-1001-2", "Claw hammer", 2),
                new TopTool("HAND", 2, "LT-1002-3", "Socket set", 1),
                new TopTool("LADDERS", 1, "LT-1009-1", "Step ladder", 2),
                new TopTool("LADDERS", 2, "LT-1010-2", "Extension ladder", 1),
                new TopTool("POWER", 1, "LT-1003-4", "Cordless drill", 3),
                new TopTool("POWER", 2, "LT-1004-5", "Circular saw", 1)), reports.topToolsPerCategory(2));
        assertEquals(4, reports.topToolsPerCategory(1).size());
    }

    @Test
    void balancesIncludeMembersWithNoFees() {
        assertEquals(List.of(
                new MemberBalance("M-0004", "Dana Whitlow", 225),
                new MemberBalance("M-0002", "Bram Okafor", 200),
                new MemberBalance("M-0003", "Cleo Marsh", 150),
                new MemberBalance("M-0001", "Avery Quill", 0),
                new MemberBalance("M-0005", "Eli Northcott", 0),
                new MemberBalance("M-0006", "Fern Ito", 0)), reports.memberBalances());
    }

    @Test
    void reportsFollowTheDataNotTheSample() throws SQLException {
        TestDatabase.execute(db, "INSERT INTO tools (code, name, category_code, replacement_cents, status, shelf) "
                + "VALUES ('LT-2000-2', 'Pressure washer', 'CLEANING', 21000, 'AVAILABLE', 'E-01')");
        TestDatabase.execute(db, "INSERT INTO loans (member_id, tool_code, loaned_on, due_on, returned_on, fee_cents)"
                + " VALUES ('M-0006', 'LT-2000-2', DATE '2026-10-01', DATE '2026-10-04', DATE '2026-10-08', 400)");
        TestDatabase.execute(db, "INSERT INTO loans (member_id, tool_code, loaned_on, due_on, returned_on, fee_cents)"
                + " VALUES ('M-0006', 'LT-2000-2', DATE '2026-10-10', DATE '2026-10-11', DATE '2026-10-12', 0)");
        assertEquals(new CategoryUsage("CLEANING", "Cleaning", 1, 0, 2, "NORMAL"), reports.categoryUsage().get(0));
        assertEquals(List.of(), reports.membersWithoutLoans());
        assertEquals(new MemberBalance("M-0006", "Fern Ito", 400), reports.memberBalances().get(0));
        assertEquals(new TopTool("CLEANING", 1, "LT-2000-2", "Pressure washer", 2),
                reports.topToolsPerCategory(1).get(0));
    }
}
