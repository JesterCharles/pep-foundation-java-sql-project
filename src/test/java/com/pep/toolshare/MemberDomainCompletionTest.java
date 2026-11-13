package com.pep.toolshare;

import com.pep.toolshare.domain.Member;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberDomainCompletionTest {
    @Test
    void memberRejectsInvalidRequiredValues() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Member(null, null, "valid@example.test")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Member(null, "   ", "valid@example.test")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Member(null, "Riley Stone", null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Member(null, "Riley Stone", "not-an-email"))
        );
    }
}
