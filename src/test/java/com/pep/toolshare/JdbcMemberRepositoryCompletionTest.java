package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.domain.Member;
import com.pep.toolshare.member.JdbcMemberRepository;
import com.pep.toolshare.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcMemberRepositoryCompletionTest {
    private MemberRepository members;

    @BeforeEach
    void resetDatabase() {
        ConnectionFactory connections = TestDatabase.create();
        members = new JdbcMemberRepository(connections);
    }

    @Test
    void createReadUpdateAndDeleteRoundTrip() {
        long id = members.create(new Member(null, "Morgan O'Neil", "morgan@example.test"));
        assertTrue(id > 0, "create should return the generated id");
        assertEquals(
                new Member(id, "Morgan O'Neil", "morgan@example.test"),
                members.findById(id).orElseThrow()
        );

        assertTrue(members.updateEmail(id, "morgan.updated@example.test"));
        assertEquals("morgan.updated@example.test", members.findById(id).orElseThrow().email());

        assertTrue(members.deleteById(id));
        assertTrue(members.findById(id).isEmpty());
    }

    @Test
    void missingRowsReturnEmptyOrFalse() {
        assertTrue(members.findById(999L).isEmpty());
        assertFalse(members.updateEmail(999L, "missing@example.test"));
        assertFalse(members.deleteById(999L));
    }
}
