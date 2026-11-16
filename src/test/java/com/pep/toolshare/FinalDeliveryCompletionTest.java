package com.pep.toolshare;

import com.pep.toolshare.api.LanternApi;
import com.pep.toolshare.config.AppConfig;
import com.pep.toolshare.config.StoreFactory;
import com.pep.toolshare.config.Stores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Week 6 integration: the whole application on a database file, driven only over HTTP, then restarted to prove that
 * every change was saved.
 */
class FinalDeliveryCompletionTest {
    private static final String TOKEN = "final-check-token";

    @TempDir
    Path folder;

    private HttpProbe startServer(AppConfig config) {
        Stores stores = StoreFactory.open(config);
        return new HttpProbe(LanternApi.create(Fixtures.service(stores), stores.reports(), config,
                () -> Fixtures.TODAY));
    }

    @Test
    void aDayAtTheLibraryIsSavedAcrossRestarts() {
        String url = "jdbc:h2:file:" + folder.resolve("lantern").toAbsolutePath()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
        AppConfig config = AppConfig.fromEnvironment(Map.of("LANTERN_STORE", "jdbc", "LANTERN_DB_URL", url,
                "LANTERN_ADMIN_TOKEN", TOKEN, "LANTERN_PORT", "0"));

        try (HttpProbe http = startServer(config)) {
            assertEquals(200, http.get("/health").status());
            HttpProbe.Reply loan = http.post("/loans", "{\"memberId\":\"M-0006\",\"toolCode\":\"LT-1007-8\"}");
            assertEquals(201, loan.status(), loan.describe());
            assertEquals(15, loan.json().get("id").asLong());
            assertEquals(409, http.post("/loans", "{\"memberId\":\"M-0003\",\"toolCode\":\"LT-1007-8\"}")
                    .status());
            assertEquals(201, http.post("/tools/LT-1007-8/waitlist", "{\"memberId\":\"M-0003\"}").status());
            HttpProbe.Reply late = http.post("/loans/9/return", "{\"damaged\":true}");
            assertEquals(600, late.json().get("feeCents").asLong(), late.describe());
            HttpProbe.Reply receipt = http.get("/loans/9/receipt");
            assertTrue(receipt.body().contains("Fee: $6.00"), receipt.body());
            assertEquals(2, http.get("/reports/overdue").json().size());
            assertEquals(401, http.post("/tools/LT-1008-9/retire", null, "guess").status());
            assertEquals(204, http.post("/tools/LT-1008-9/retire", null, TOKEN).status());
        }

        try (HttpProbe again = startServer(config)) {
            assertEquals("ON_LOAN", again.get("/tools/LT-1007-8").json().get("status").asText());
            assertEquals("IN_REPAIR", again.get("/tools/LT-1003-4").json().get("status").asText());
            assertEquals("RETIRED", again.get("/tools/LT-1008-9").json().get("status").asText());
            assertEquals(1, again.get("/members/M-0006/loans").json().size());
            assertEquals(2, again.get("/reports/overdue").json().size());
            HttpProbe.Reply top = again.get("/reports/top-tools?limit=1");
            assertEquals("LT-1003-4", top.json().get(0).get("code").asText());
            HttpProbe.Reply next = again.post("/loans", "{\"memberId\":\"M-0006\",\"toolCode\":\"LT-1012-4\"}");
            assertEquals(16, next.json().get("id").asLong(), "ids continue after a restart: " + next.describe());
        }
    }
}
