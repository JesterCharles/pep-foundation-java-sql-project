package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.StorageException;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Member;
import com.pep.toolshare.domain.MemberTier;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.jdbc.JdbcLoanRepository;
import com.pep.toolshare.jdbc.JdbcMemberRepository;
import com.pep.toolshare.jdbc.JdbcToolRepository;
import com.pep.toolshare.lending.RuleViolationException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 5: CRUD through JDBC with prepared statements, row mapping and closed resources. */
class JdbcRepositoriesCompletionTest {
    private final ConnectionFactory db = TestDatabase.create();
    private final JdbcMemberRepository members = new JdbcMemberRepository(db);
    private final JdbcToolRepository tools = new JdbcToolRepository(db);
    private final JdbcLoanRepository loans = new JdbcLoanRepository(db);

    @Test
    void membersAreMappedFromRows() {
        List<Member> all = members.findAll();
        assertEquals(6, all.size());
        assertEquals("M-0001", all.get(0).id());
        Member bram = members.findById("M-0002");
        assertEquals("Bram Okafor", bram.fullName());
        assertEquals("bram.okafor@example.test", bram.email());
        assertEquals(MemberTier.CHAMPION, bram.tier());
        assertTrue(bram.isActive());
        assertEquals("M-0001", bram.referredBy());
        assertFalse(members.findById("M-0005").isActive());
        assertNull(members.findById("M-0404"));
    }

    @Test
    void membersAreAddedUpdatedAndDeleted() throws SQLException {
        members.add(new Member("M-0100", "Gale Hart", "gale.hart@example.test", MemberTier.STARTER, true, "M-0006"));
        assertEquals("M-0006", members.findById("M-0100").referredBy());
        assertThrows(RuleViolationException.class, () -> members.add(new Member("M-0101", "Copy",
                "gale.hart@example.test", MemberTier.STARTER, true, null)), "duplicate email");
        assertTrue(members.updateEmail("M-0100", "Gale.New@Example.TEST"));
        assertEquals("gale.new@example.test", members.findById("M-0100").email());
        assertFalse(members.updateEmail("M-0404", "x.y@example.test"));
        assertThrows(RuleViolationException.class, () -> members.updateEmail("M-0100", "fern.ito@example.test"),
                "another member already uses this email");
        assertEquals("gale.new@example.test", members.findById("M-0100").email());
        assertThrows(RuleViolationException.class, () -> members.delete("M-0001"), "has loans");
        assertTrue(members.delete("M-0100"));
        assertFalse(members.delete("M-0100"));
        assertEquals(6, TestDatabase.count(db, "SELECT COUNT(*) FROM members"));
    }

    @Test
    void valuesAreBoundNotConcatenated() throws SQLException {
        String hostile = "M-0001' OR '1'='1";
        assertNull(members.findById(hostile), "a quote in the value must not change the query");
        assertNull(tools.findByCode("x' OR 'x'='x"));
        assertEquals(List.of(), loans.findOpenByMember("M-0001' OR '1'='1"));
        members.add(new Member("M-0100", "O'Brien Test", "obrien@example.test", MemberTier.STARTER, true, null));
        assertEquals("O'Brien Test", members.findById("M-0100").fullName());
        assertEquals(7, TestDatabase.count(db, "SELECT COUNT(*) FROM members"));
    }

    @Test
    void toolsAreMappedAndOrdered() {
        List<Tool> all = tools.findAll();
        assertEquals(12, all.size());
        assertEquals("LT-1001-2", all.get(0).code());
        assertEquals("LT-1012-4", all.get(11).code());
        Tool drill = tools.findByCode("LT-1003-4");
        assertEquals("Cordless drill", drill.name());
        assertEquals(ToolCategory.POWER, drill.category());
        assertEquals(12_900, drill.replacementCents());
        assertEquals(ToolStatus.ON_LOAN, drill.status());
        assertEquals("B-01", drill.shelf());
        assertEquals(List.of("LT-1003-4", "LT-1004-5", "LT-1005-6"),
                tools.findByCategory(ToolCategory.POWER).stream().map(Tool::code).toList());
        assertEquals(List.of(), tools.findByCategory(ToolCategory.CLEANING));
    }

    @Test
    void toolsAreAddedAndUpdated() {
        tools.add(new Tool("LT-2000-2", "Pressure washer", ToolCategory.CLEANING, 21_000, ToolStatus.AVAILABLE,
                "E-01"));
        assertEquals("Pressure washer", tools.findByCode("LT-2000-2").name());
        assertThrows(RuleViolationException.class, () -> tools.add(new Tool("LT-2000-2", "Copy",
                ToolCategory.CLEANING, 100, ToolStatus.AVAILABLE, "E-01")));
        assertTrue(tools.updateStatus("LT-2000-2", ToolStatus.IN_REPAIR));
        assertEquals(ToolStatus.IN_REPAIR, tools.findByCode("LT-2000-2").status());
        assertFalse(tools.updateStatus("LT-9999-9", ToolStatus.RETIRED));
    }

    @Test
    void loansAreMappedIncludingNullReturnDates() {
        List<Loan> all = loans.findAll();
        assertEquals(14, all.size());
        assertEquals(new Loan(2, "M-0002", "LT-1003-4", LocalDate.of(2026, 9, 3), LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 14), 200), all.get(1));
        Loan open = loans.findById(9);
        assertTrue(open.isOpen());
        assertNull(open.returnedOn());
        assertEquals(List.of(10L), loans.findOpenByMember("M-0002").stream().map(Loan::id).toList());
        assertNull(loans.findById(404));
    }

    @Test
    void databaseFailuresAreWrappedWithTheirCause() {
        ConnectionFactory broken = () -> {
            throw new SQLException("database unavailable", "08001");
        };
        StorageException failure = assertThrows(StorageException.class,
                () -> new JdbcToolRepository(broken).findAll());
        assertInstanceOf(SQLException.class, failure.getCause(), "keep the SQLException as the cause");
        assertThrows(StorageException.class, () -> new JdbcMemberRepository(broken).findById("M-0001"));
        assertThrows(StorageException.class, () -> new JdbcLoanRepository(broken).findAll());
    }
}
