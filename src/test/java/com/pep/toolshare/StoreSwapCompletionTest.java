package com.pep.toolshare;

import com.pep.toolshare.config.AppConfig;
import com.pep.toolshare.config.StoreFactory;
import com.pep.toolshare.config.Stores;
import com.pep.toolshare.jdbc.JdbcLoanRepository;
import com.pep.toolshare.jdbc.JdbcMemberRepository;
import com.pep.toolshare.jdbc.JdbcToolRepository;
import com.pep.toolshare.lending.LendingService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 5: the same service runs on memory or on the database, chosen by configuration. */
class StoreSwapCompletionTest {
    @Test
    void storeAndDatabaseSettings() {
        AppConfig defaults = AppConfig.fromEnvironment(Map.of());
        assertEquals("memory", defaults.store());
        assertEquals(AppConfig.DEFAULT_DATABASE_URL, defaults.databaseUrl());
        AppConfig jdbc = AppConfig.fromEnvironment(Map.of("LANTERN_STORE", " JDBC ",
                "LANTERN_DB_URL", " jdbc:h2:mem:other "));
        assertEquals("jdbc", jdbc.store());
        assertEquals("jdbc:h2:mem:other", jdbc.databaseUrl());
        assertEquals("memory", AppConfig.fromEnvironment(Map.of("LANTERN_STORE", "  ")).store());
        IllegalArgumentException store = assertThrows(IllegalArgumentException.class,
                () -> AppConfig.fromEnvironment(Map.of("LANTERN_STORE", "postgres")));
        assertTrue(store.getMessage().contains("LANTERN_STORE"));
        IllegalArgumentException url = assertThrows(IllegalArgumentException.class,
                () -> AppConfig.fromEnvironment(Map.of("LANTERN_DB_URL", "jdbc:postgresql://db.example.test/x")));
        assertTrue(url.getMessage().contains("LANTERN_DB_URL"));
    }

    @Test
    void memoryStoreHasNoReports() {
        Stores stores = StoreFactory.open(AppConfig.fromEnvironment(Map.of()));
        assertNull(stores.reports());
        assertEquals(6, stores.members().findAll().size());
    }

    @Test
    void jdbcStoreUsesTheDatabaseAndKeepsItsData() {
        AppConfig config = AppConfig.fromEnvironment(Map.of("LANTERN_STORE", "jdbc",
                "LANTERN_DB_URL", TestDatabase.newUrl()));
        Stores first = StoreFactory.open(config);
        assertInstanceOf(JdbcMemberRepository.class, first.members());
        assertInstanceOf(JdbcToolRepository.class, first.tools());
        assertInstanceOf(JdbcLoanRepository.class, first.loans());
        assertNotNull(first.reports());
        assertEquals(14, first.loans().findAll().size(), "an empty database receives the sample data");

        LendingService service = new LendingService(first.members(), first.tools(), first.loans(),
                () -> Fixtures.TODAY);
        assertEquals(15, service.checkout("M-0006", "LT-1007-8").id());

        Stores second = StoreFactory.open(config);
        assertEquals(15, second.loans().findAll().size(), "opening again must not reset existing data");
        assertEquals(1, second.loans().findOpenByMember("M-0006").size());
    }

    @Test
    void envExampleListsTheDatabaseSettings() throws IOException {
        Map<String, String> settings = SecretHygieneCompletionTest.readExample();
        assertEquals("memory", settings.get("LANTERN_STORE"), ".env.example: add LANTERN_STORE=memory");
        assertTrue(settings.getOrDefault("LANTERN_DB_URL", "").startsWith("jdbc:h2:"),
                ".env.example: add LANTERN_DB_URL with the default embedded H2 URL, in single quotes");
    }
}
