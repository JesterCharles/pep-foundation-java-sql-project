package com.pep.toolshare;

import com.pep.toolshare.config.AppConfig;
import com.pep.toolshare.lending.InvalidRequestException;
import com.pep.toolshare.lending.LendingException;
import com.pep.toolshare.lending.NotFoundException;
import com.pep.toolshare.lending.RuleViolationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 4: a custom unchecked exception hierarchy, and configuration read from environment variables. */
class ExceptionsAndConfigCompletionTest {
    @Test
    void lendingExceptionsAreUncheckedAndShareABaseClass() {
        assertTrue(RuntimeException.class.isAssignableFrom(LendingException.class),
                "LendingException must be unchecked (extend RuntimeException)");
        for (Class<?> type : List.of(NotFoundException.class, RuleViolationException.class,
                InvalidRequestException.class)) {
            assertSame(LendingException.class, type.getSuperclass(),
                    type.getSimpleName() + " extends LendingException");
        }
    }

    @Test
    void everyLendingExceptionKeepsItsMessageAndCause() throws Exception {
        IllegalStateException cause = new IllegalStateException("low-level problem");
        for (Class<?> type : List.of(LendingException.class, NotFoundException.class,
                RuleViolationException.class, InvalidRequestException.class)) {
            Constructor<?> withCause = type.getConstructor(String.class, Throwable.class);
            RuntimeException wrapped = assertInstanceOf(RuntimeException.class,
                    withCause.newInstance("what went wrong", cause), type.getSimpleName() + " is unchecked");
            assertEquals("what went wrong", wrapped.getMessage(), type.getSimpleName());
            assertSame(cause, wrapped.getCause(), type.getSimpleName() + " keeps the cause");
            RuntimeException plain = assertInstanceOf(RuntimeException.class,
                    type.getConstructor(String.class).newInstance("plain"));
            assertEquals("plain", plain.getMessage());
        }
    }

    @Test
    void configUsesDefaultsWhenVariablesAreMissing() {
        AppConfig config = AppConfig.fromEnvironment(Map.of());
        assertEquals(7070, config.port());
        assertEquals("", config.adminToken());
        assertFalse(config.adminEnabled());
    }

    @Test
    void configReadsAndCleansVariables() {
        AppConfig config = AppConfig.fromEnvironment(Map.of("LANTERN_PORT", " 8181 ",
                "LANTERN_ADMIN_TOKEN", " s3cret-local-token "));
        assertEquals(8181, config.port());
        assertEquals("s3cret-local-token", config.adminToken());
        assertTrue(config.adminEnabled());
        assertEquals(0, AppConfig.fromEnvironment(Map.of("LANTERN_PORT", "0")).port(), "0 means any free port");
        assertEquals(7070, AppConfig.fromEnvironment(Map.of("LANTERN_PORT", "  ")).port(), "blank means default");
    }

    @Test
    void invalidPortIsRejectedWithItsCause() {
        IllegalArgumentException notNumber = assertThrows(IllegalArgumentException.class,
                () -> AppConfig.fromEnvironment(Map.of("LANTERN_PORT", "seventy")));
        assertTrue(notNumber.getMessage().contains("LANTERN_PORT"), "the message names the variable");
        assertInstanceOf(NumberFormatException.class, notNumber.getCause(), "keep the parsing failure as the cause");
        assertThrows(IllegalArgumentException.class, () -> AppConfig.fromEnvironment(Map.of("LANTERN_PORT", "70000")));
        assertThrows(IllegalArgumentException.class, () -> AppConfig.fromEnvironment(Map.of("LANTERN_PORT", "-1")));
    }

    @Test
    void configNeverPrintsTheToken() {
        AppConfig config = AppConfig.fromEnvironment(Map.of("LANTERN_ADMIN_TOKEN", "s3cret-local-token"));
        assertFalse(config.toString().contains("s3cret-local-token"), "toString must not reveal the token");
        assertTrue(config.toString().contains("adminToken=<set>"), config.toString());
        assertTrue(AppConfig.fromEnvironment(Map.of()).toString().contains("adminToken=<not set>"));
    }
}
