package com.pep.toolshare;

import com.fasterxml.jackson.databind.JsonNode;
import com.pep.toolshare.api.LanternApi;
import com.pep.toolshare.config.AppConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 4: Javalin routes and handlers over real localhost HTTP, with honest status codes and JSON errors. */
class LanternApiCompletionTest {
    private static final String TOKEN = "test-admin-token";
    private HttpProbe http;

    @BeforeEach
    void start() {
        AppConfig config = AppConfig.fromEnvironment(Map.of("LANTERN_ADMIN_TOKEN", TOKEN));
        http = new HttpProbe(LanternApi.create(Fixtures.memoryService(), null, config, () -> Fixtures.TODAY));
    }

    @AfterEach
    void stop() {
        http.close();
    }

    private static void assertError(int status, HttpProbe.Reply reply) {
        assertEquals(status, reply.status(), reply.describe());
        assertTrue(reply.contentType().startsWith("application/json"), "errors are JSON: " + reply.describe());
        JsonNode error = reply.json().get("error");
        assertTrue(error != null && !error.asText().isBlank(), "error body is {\"error\":\"...\"}: " + reply.body());
    }

    @Test
    void listsToolsAsJsonOptionallyByCategory() {
        HttpProbe.Reply all = http.get("/tools");
        assertEquals(200, all.status(), all.describe());
        assertTrue(all.contentType().startsWith("application/json"));
        assertEquals(12, all.json().size());
        assertEquals("LT-1001-2", all.json().get(0).get("code").asText());
        HttpProbe.Reply garden = http.get("/tools?category=garden");
        assertEquals(3, garden.json().size(), garden.describe());
        assertEquals("GARDEN", garden.json().get(2).get("category").asText());
        assertError(400, http.get("/tools?category=plumbing"));
    }

    @Test
    void readsOneToolByPathParameter() {
        HttpProbe.Reply ladder = http.get("/tools/LT-1009-1");
        assertEquals(200, ladder.status(), ladder.describe());
        JsonNode body = ladder.json();
        assertEquals("Step ladder", body.get("name").asText());
        assertEquals("ON_LOAN", body.get("status").asText());
        assertEquals("D-01", body.get("shelf").asText());
        assertError(404, http.get("/tools/LT-9999-9"));
        assertError(400, http.get("/tools/not-a-code"));
    }

    @Test
    void checkoutCreatesALoan() {
        HttpProbe.Reply created = http.post("/loans", "{\"memberId\":\"M-0006\",\"toolCode\":\"LT-1007-8\"}");
        assertEquals(201, created.status(), created.describe());
        JsonNode loan = created.json();
        assertEquals(15, loan.get("id").asLong());
        assertEquals("2026-10-25", loan.get("dueOn").asText());
        assertTrue(loan.get("returnedOn").isNull());
        assertEquals("ON_LOAN", http.get("/tools/LT-1007-8").json().get("status").asText());
        assertEquals(1, http.get("/members/M-0006/loans").json().size());
    }

    @Test
    void checkoutErrorsUseTheRightStatus() {
        assertError(409, http.post("/loans", "{\"memberId\":\"M-0006\",\"toolCode\":\"LT-1003-4\"}"));
        assertError(409, http.post("/loans", "{\"memberId\":\"M-0005\",\"toolCode\":\"LT-1007-8\"}"));
        assertError(404, http.post("/loans", "{\"memberId\":\"M-0404\",\"toolCode\":\"LT-1007-8\"}"));
        assertError(400, http.post("/loans", "{\"memberId\":\"M-0006\"}"));
        assertError(400, http.post("/loans", null));
        assertError(400, http.post("/loans", "{not json"));
    }

    @Test
    void returnRecordsTheFee() {
        HttpProbe.Reply returned = http.post("/loans/9/return", "{\"damaged\":false}");
        assertEquals(200, returned.status(), returned.describe());
        assertEquals(600, returned.json().get("feeCents").asLong());
        assertEquals("2026-10-15", returned.json().get("returnedOn").asText());
        HttpProbe.Reply damaged = http.post("/loans/11/return", null);
        assertEquals(200, damaged.status(), "the body is optional: " + damaged.describe());
        assertError(409, http.post("/loans/9/return", "{}"));
        assertError(404, http.post("/loans/404/return", "{}"));
        assertError(400, http.post("/loans/abc/return", "{}"));
    }

    @Test
    void damagedReturnSendsTheToolToRepair() {
        assertEquals(200, http.post("/loans/10/return", "{\"damaged\":true}").status());
        assertEquals("IN_REPAIR", http.get("/tools/LT-1006-7").json().get("status").asText());
    }

    @Test
    void receiptIsPlainText() {
        HttpProbe.Reply receipt = http.get("/loans/2/receipt");
        assertEquals(200, receipt.status(), receipt.describe());
        assertTrue(receipt.contentType().startsWith("text/plain"), receipt.contentType());
        assertTrue(receipt.body().startsWith("Lantern Tool Library - return receipt\n"), receipt.body());
        assertError(409, http.get("/loans/9/receipt"));
    }

    @Test
    void memberLoansAreListed() {
        HttpProbe.Reply loans = http.get("/members/M-0001/loans");
        assertEquals(200, loans.status(), loans.describe());
        assertEquals(1, loans.json().size());
        assertEquals(8, loans.json().get(0).get("id").asLong());
        assertError(404, http.get("/members/M-0404/loans"));
    }

    @Test
    void retireNeedsTheAdminToken() {
        assertError(401, http.post("/tools/LT-1008-9/retire", null));
        assertError(401, http.post("/tools/LT-1008-9/retire", null, "wrong-token"));
        HttpProbe.Reply retired = http.post("/tools/LT-1008-9/retire", null, TOKEN);
        assertEquals(204, retired.status(), retired.describe());
        assertEquals("RETIRED", http.get("/tools/LT-1008-9").json().get("status").asText());
        assertError(409, http.post("/tools/LT-1003-4/retire", null, TOKEN));
    }

    @Test
    void adminRoutesAreDisabledWithoutAConfiguredToken() {
        try (HttpProbe noAdmin = new HttpProbe(LanternApi.create(Fixtures.memoryService(), null,
                AppConfig.fromEnvironment(Map.of()), () -> Fixtures.TODAY))) {
            assertError(403, noAdmin.post("/tools/LT-1008-9/retire", null, ""));
        }
    }

    @Test
    void unknownRouteIsNotFound() {
        assertEquals(404, http.get("/nothing-here").status());
        assertEquals(200, http.get("/health").status());
    }
}
