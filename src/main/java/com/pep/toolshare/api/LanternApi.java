package com.pep.toolshare.api;

import com.pep.toolshare.config.AppConfig;
import com.pep.toolshare.domain.Dates;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.lending.InvalidRequestException;
import com.pep.toolshare.lending.LendingService;
import com.pep.toolshare.lending.NotFoundException;
import com.pep.toolshare.lending.RuleViolationException;
import com.pep.toolshare.reports.InventoryAnalytics;
import com.pep.toolshare.reports.LibraryReports;
import com.pep.toolshare.reports.WaitList;
import com.pep.toolshare.reports.WaitListBoard;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The Lantern Tool Library JSON API (Javalin 7). {@link #create} is supplied; you write the three methods it calls.
 * Every error response is JSON shaped like {@link ErrorBody}. The routes, bodies and status codes are listed in
 * {@code docs/milestones/4-http-api.md} (Week 4) and {@code docs/milestones/6-final-delivery.md} (Week 6).
 */
public final class LanternApi {
    private static final Logger log = LoggerFactory.getLogger(LanternApi.class);

    private LanternApi() {
    }

    /**
     * Builds (but does not start) the application. {@code reports} is {@code null} when data is kept in memory.
     * {@code today} gives the date used by the report routes when the request names none.
     */
    public static Javalin create(LendingService service, LibraryReports reports, AppConfig settings,
                                 Supplier<LocalDate> today) {
        WaitListBoard waitLists = new WaitListBoard();
        return Javalin.create(config -> {
            config.routes.get("/health", ctx -> ctx.json(Map.of("status", "ok")));
            errorHandlers(config);
            coreRoutes(config, service, settings);
            reportRoutes(config, service, reports, waitLists, today);
        });
    }

    /** Week 4: map exceptions to status codes and {@link ErrorBody} JSON. */
    static void errorHandlers(JavalinConfig config) {
        // TODO LEARNER 4E: register the exception handlers.
    }

    /** Week 4: tools, members, loans and the admin route. */
    static void coreRoutes(JavalinConfig config, LendingService service, AppConfig settings) {
        // TODO LEARNER 4E: register the Week 4 routes.
    }

    /** Week 6: reports, analytics and wait lists. */
    static void reportRoutes(JavalinConfig config, LendingService service, LibraryReports reports,
                             WaitListBoard waitLists, Supplier<LocalDate> today) {
        // TODO LEARNER 6D: register the Week 6 routes.
    }
}
