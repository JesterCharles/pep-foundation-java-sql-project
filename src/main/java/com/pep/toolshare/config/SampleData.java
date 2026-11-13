package com.pep.toolshare.config;

import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Member;
import com.pep.toolshare.domain.MemberTier;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.lending.InMemoryLoanRepository;
import com.pep.toolshare.lending.InMemoryMemberRepository;
import com.pep.toolshare.lending.InMemoryToolRepository;

import java.time.LocalDate;

/**
 * The synthetic Lantern Tool Library data held in memory. It matches {@code src/main/resources/db/seed.sql} row for
 * row, so the in-memory and database stores start from the same state.
 */
public final class SampleData {
    private SampleData() {
    }

    public static Stores inMemory() {
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        InMemoryToolRepository tools = new InMemoryToolRepository();
        InMemoryLoanRepository loans = new InMemoryLoanRepository(tools, members);

        members.add(new Member("M-0001", "Avery Quill", "avery.quill@example.test", MemberTier.REGULAR, true, null));
        members.add(new Member("M-0002", "Bram Okafor", "bram.okafor@example.test", MemberTier.CHAMPION, true,
                "M-0001"));
        members.add(new Member("M-0003", "Cleo Marsh", "cleo.marsh@example.test", MemberTier.STARTER, true,
                "M-0001"));
        members.add(new Member("M-0004", "Dana Whitlow", "dana.whitlow@example.test", MemberTier.STARTER, true,
                "M-0002"));
        members.add(new Member("M-0005", "Eli Northcott", "eli.northcott@example.test", MemberTier.REGULAR, false,
                null));
        members.add(new Member("M-0006", "Fern Ito", "fern.ito@example.test", MemberTier.STARTER, true, null));

        tool(tools, "LT-1001-2", "Claw hammer", ToolCategory.HAND, 1_800, ToolStatus.AVAILABLE, "A-01");
        tool(tools, "LT-1002-3", "Socket set", ToolCategory.HAND, 4_500, ToolStatus.ON_LOAN, "A-02");
        tool(tools, "LT-1003-4", "Cordless drill", ToolCategory.POWER, 12_900, ToolStatus.ON_LOAN, "B-01");
        tool(tools, "LT-1004-5", "Circular saw", ToolCategory.POWER, 15_900, ToolStatus.AVAILABLE, "B-02");
        tool(tools, "LT-1005-6", "Orbital sander", ToolCategory.POWER, 7_900, ToolStatus.IN_REPAIR, "B-03");
        tool(tools, "LT-1006-7", "Hedge trimmer", ToolCategory.GARDEN, 8_900, ToolStatus.ON_LOAN, "C-01");
        tool(tools, "LT-1007-8", "Lawn aerator", ToolCategory.GARDEN, 6_500, ToolStatus.AVAILABLE, "C-02");
        tool(tools, "LT-1008-9", "Wheelbarrow", ToolCategory.GARDEN, 9_900, ToolStatus.AVAILABLE, "C-03");
        tool(tools, "LT-1009-1", "Step ladder", ToolCategory.LADDERS, 7_500, ToolStatus.ON_LOAN, "D-01");
        tool(tools, "LT-1010-2", "Extension ladder", ToolCategory.LADDERS, 18_900, ToolStatus.AVAILABLE, "D-02");
        tool(tools, "LT-1011-3", "Tile cutter", ToolCategory.HAND, 5_400, ToolStatus.RETIRED, "A-03");
        tool(tools, "LT-1012-4", "Pipe wrench", ToolCategory.HAND, 2_600, ToolStatus.AVAILABLE, "A-01");

        loan(loans, 1, "M-0001", "LT-1001-2", "2026-09-01", "2026-09-15", "2026-09-14", 0);
        loan(loans, 2, "M-0002", "LT-1003-4", "2026-09-03", "2026-09-10", "2026-09-14", 200);
        loan(loans, 3, "M-0003", "LT-1006-7", "2026-09-05", "2026-09-15", "2026-09-15", 0);
        loan(loans, 4, "M-0001", "LT-1003-4", "2026-09-20", "2026-09-27", "2026-09-27", 0);
        loan(loans, 5, "M-0004", "LT-1009-1", "2026-09-22", "2026-09-29", "2026-10-02", 225);
        loan(loans, 6, "M-0002", "LT-1001-2", "2026-09-25", "2026-10-09", "2026-10-08", 0);
        loan(loans, 7, "M-0005", "LT-1007-8", "2026-09-26", "2026-10-06", "2026-10-06", 0);
        loan(loans, 8, "M-0001", "LT-1002-3", "2026-10-01", "2026-10-15", null, 0);
        loan(loans, 9, "M-0003", "LT-1003-4", "2026-10-02", "2026-10-09", null, 0);
        loan(loans, 10, "M-0002", "LT-1006-7", "2026-10-03", "2026-10-13", null, 0);
        loan(loans, 11, "M-0004", "LT-1009-1", "2026-10-05", "2026-10-12", null, 0);
        loan(loans, 12, "M-0001", "LT-1004-5", "2026-09-10", "2026-09-17", "2026-09-16", 0);
        loan(loans, 13, "M-0003", "LT-1010-2", "2026-09-12", "2026-09-19", "2026-09-21", 150);
        loan(loans, 14, "M-0002", "LT-1012-4", "2026-09-15", "2026-09-29", "2026-09-29", 0);

        return new Stores(members, tools, loans, null);
    }

    private static void tool(InMemoryToolRepository tools, String code, String name, ToolCategory category,
                             long replacementCents, ToolStatus status, String shelf) {
        tools.add(new Tool(code, name, category, replacementCents, status, shelf));
    }

    private static void loan(InMemoryLoanRepository loans, long id, String memberId, String toolCode,
                             String loanedOn, String dueOn, String returnedOn, long feeCents) {
        loans.restore(new Loan(id, memberId, toolCode, LocalDate.parse(loanedOn), LocalDate.parse(dueOn),
                returnedOn == null ? null : LocalDate.parse(returnedOn), feeCents));
    }
}
