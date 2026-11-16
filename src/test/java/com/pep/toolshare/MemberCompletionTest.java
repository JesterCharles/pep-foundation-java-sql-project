package com.pep.toolshare;

import com.pep.toolshare.domain.Member;
import com.pep.toolshare.domain.MemberTier;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 3: a class that keeps every object valid (constructors, encapsulation, null handling, toString). */
class MemberCompletionTest {
    private static Member avery() {
        return new Member("M-0001", "  Avery Quill ", " Avery.Quill@Example.TEST ", MemberTier.REGULAR, true, null);
    }

    @Test
    void constructorStoresCleanedValues() {
        Member member = avery();
        assertEquals("M-0001", member.id());
        assertEquals("Avery Quill", member.fullName());
        assertEquals("avery.quill@example.test", member.email());
        assertEquals(MemberTier.REGULAR, member.tier());
        assertTrue(member.isActive());
        assertNull(member.referredBy());
    }

    @Test
    void referredByAnotherMemberIsKept() {
        Member member = new Member("M-0002", "Bram Okafor", "bram@example.test", MemberTier.CHAMPION, true, "M-0001");
        assertEquals("M-0001", member.referredBy());
    }

    @Test
    void requiredValuesRejectNullWithAClearMessage() {
        NullPointerException missing = assertThrows(NullPointerException.class,
                () -> new Member(null, "A B", "a@b.test", MemberTier.STARTER, true, null));
        assertNotEquals(null, missing.getMessage(), "use Objects.requireNonNull(value, \"message\")");
        assertThrows(NullPointerException.class,
                () -> new Member("M-0001", null, "a@b.test", MemberTier.STARTER, true, null));
        assertThrows(NullPointerException.class,
                () -> new Member("M-0001", "A B", null, MemberTier.STARTER, true, null));
        assertThrows(NullPointerException.class,
                () -> new Member("M-0001", "A B", "a@b.test", null, true, null));
    }

    @Test
    void invalidValuesAreRejected() {
        for (String badId : new String[] {"M-001", "M-00001", "X-0001", "m-0001", "M-00A1", ""}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Member(badId, "A B", "a@b.test", MemberTier.STARTER, true, null), badId);
        }
        assertThrows(IllegalArgumentException.class,
                () -> new Member("M-0001", "   ", "a@b.test", MemberTier.STARTER, true, null));
        for (String badEmail : new String[] {"ab.test", "@b.test", "a@b", "a@@b.test", "a@b@c.test", "a@.test",
                "a@b.", "  "}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Member("M-0001", "A B", badEmail, MemberTier.STARTER, true, null), badEmail);
        }
        assertThrows(IllegalArgumentException.class,
                () -> new Member("M-0001", "A B", "a@b.test", MemberTier.STARTER, true, "M-0001"), "self referral");
        assertThrows(IllegalArgumentException.class,
                () -> new Member("M-0001", "A B", "a@b.test", MemberTier.STARTER, true, "nobody"));
    }

    @Test
    void isMemberIdChecksTheFormat() {
        assertTrue(Member.isMemberId("M-0042"));
        assertFalse(Member.isMemberId("M-42"));
        assertFalse(Member.isMemberId(null));
    }

    @Test
    void emailChangesOnlyToAValidValue() {
        Member member = avery();
        member.changeEmail(" New.Address@Example.TEST");
        assertEquals("new.address@example.test", member.email());
        assertThrows(IllegalArgumentException.class, () -> member.changeEmail("not-an-email"));
        assertEquals("new.address@example.test", member.email(), "a rejected change leaves the old value");
        assertThrows(NullPointerException.class, () -> member.changeEmail(null));
    }

    @Test
    void deactivateEndsMembership() {
        Member member = avery();
        member.deactivate();
        assertFalse(member.isActive());
    }

    @Test
    void toStringIsReadableAndHidesTheEmail() {
        assertEquals("M-0001 Avery Quill (REGULAR)", avery().toString());
    }

    @Test
    void fieldsAreHiddenAndIdHasNoSetter() throws Exception {
        for (var field : Member.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), "field " + field.getName() + " must be private");
        }
        assertTrue(Modifier.isFinal(Member.class.getDeclaredField("id").getModifiers()), "id never changes");
        for (Method method : Member.class.getMethods()) {
            assertFalse(method.getName().equals("setId"), "an id must not have a setter");
        }
    }

    @Test
    void tierFollowsWeekTwoRule() {
        assertEquals(MemberTier.CHAMPION, MemberTier.forCompletedLoans(31));
        assertThrows(IllegalArgumentException.class, () -> MemberTier.forCompletedLoans(-1));
    }
}
