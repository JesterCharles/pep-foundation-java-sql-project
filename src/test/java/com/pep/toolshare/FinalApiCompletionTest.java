package com.pep.toolshare;

import com.fasterxml.jackson.databind.JsonNode;
import com.pep.toolshare.api.LanternApi;
import com.pep.toolshare.config.AppConfig;
import com.pep.toolshare.config.StoreFactory;
import com.pep.toolshare.config.Stores;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 6: report, analytics and wait-list routes on top of the Week 4 API. */
class FinalApiCompletionTest {
    private static HttpProbe start(String store) {
        AppConfig config = AppConfig.fromEnvironment(Map.of("LANTERN_STORE", store,
                "LANTERN_DB_URL", TestDatabase.newUrl()));
        Stores stores = StoreFactory.open(config);
        return new HttpProbe(LanternApi.create(Fixtures.service(stores), stores.reports(), config,
                () -> Fixtures.TODAY));
    }

    private static void assertError(int status, HttpProbe.Reply reply) {
        assertEquals(status, reply.status(), reply.describe());
        assertTrue(reply.contentType().startsWith("application/json"), reply.describe());
        assertTrue(reply.json().hasNonNull("error"), reply.describe());
    }

    @Test
    void databaseReportsAreServedAsJson() {
        try (HttpProbe http = start("jdbc")) {
            HttpProbe.Reply categories = http.get("/reports/categories");
            assertEquals(200, categories.status(), categories.describe());
            assertEquals(5, categories.json().size());
            assertEquals("CLEANING", categories.json().get(0).get("categoryCode").asText());
            HttpProbe.Reply overdue = http.get("/reports/overdue");
            assertEquals(3, overdue.json().size(), "default asOf is today: " + overdue.describe());
            assertEquals(9, overdue.json().get(0).get("loanId").asLong());
            assertEquals(6, overdue.json().get(0).get("daysLate").asInt());
            assertEquals(4, http.get("/reports/overdue?asOf=2026-10-16").json().size());
            assertError(400, http.get("/reports/overdue?asOf=yesterday"));
        }
    }

    @Test
    void reportsNeedTheDatabase() {
        try (HttpProbe http = start("memory")) {
            assertError(503, http.get("/reports/categories"));
            assertError(503, http.get("/reports/overdue"));
            assertEquals(200, http.get("/reports/top-tools").status(), "analytics work in memory too");
        }
    }

    @Test
    void topToolsAndShelvesComeFromStreams() {
        try (HttpProbe http = start("memory")) {
            JsonNode top = http.get("/reports/top-tools?limit=2").json();
            assertEquals(2, top.size());
            assertEquals("LT-1003-4", top.get(0).get("code").asText());
            assertEquals(3, top.get(0).get("loanCount").asLong());
            assertEquals("LT-1001-2", top.get(1).get("code").asText());
            assertEquals(3, http.get("/reports/top-tools").json().size(), "default limit is 3");
            assertError(400, http.get("/reports/top-tools?limit=0"));
            assertError(400, http.get("/reports/top-tools?limit=21"));
            assertError(400, http.get("/reports/top-tools?limit=many"));
            JsonNode shelves = http.get("/reports/shelves").json();
            assertEquals("A-01", shelves.fieldNames().next());
            assertEquals("[\"LT-1001-2\",\"LT-1012-4\"]", shelves.get("A-01").toString());
            assertEquals(11, shelves.size());
        }
    }

    @Test
    void waitListsQueueMembersForToolsOnLoan() {
        try (HttpProbe http = start("memory")) {
            HttpProbe.Reply first = http.post("/tools/LT-1003-4/waitlist", "{\"memberId\":\"M-0006\"}");
            assertEquals(201, first.status(), first.describe());
            assertEquals(1, first.json().get("position").asInt());
            assertEquals(2, http.post("/tools/LT-1003-4/waitlist", "{\"memberId\":\"M-0001\"}").json()
                    .get("position").asInt());
            assertError(409, http.post("/tools/LT-1003-4/waitlist", "{\"memberId\":\"M-0006\"}"));
            assertError(409, http.post("/tools/LT-1007-8/waitlist", "{\"memberId\":\"M-0006\"}"));
            assertError(404, http.post("/tools/LT-1003-4/waitlist", "{\"memberId\":\"M-0404\"}"));
            assertError(404, http.post("/tools/LT-9999-9/waitlist", "{\"memberId\":\"M-0006\"}"));
            assertError(400, http.post("/tools/LT-1003-4/waitlist", null));
            JsonNode list = http.get("/tools/LT-1003-4/waitlist").json();
            assertEquals("[\"M-0006\",\"M-0001\"]", list.get("waiting").toString());
            assertEquals("[]", http.get("/tools/LT-1006-7/waitlist").json().get("waiting").toString());
        }
    }
}
