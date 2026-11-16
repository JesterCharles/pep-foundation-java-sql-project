package com.pep.toolshare;

import com.pep.toolshare.api.LanternApi;
import com.pep.toolshare.basics.LendingMath;
import com.pep.toolshare.config.AppConfig;
import com.pep.toolshare.config.StoreFactory;
import com.pep.toolshare.config.Stores;
import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.DatabaseManager;
import com.pep.toolshare.domain.Dates;
import com.pep.toolshare.jdbc.SqlReports;
import com.pep.toolshare.lending.LendingService;
import com.pep.toolshare.reports.CategoryUsage;
import com.pep.toolshare.reports.MemberRef;
import com.pep.toolshare.reports.OverdueLoan;
import com.pep.toolshare.reports.TopTool;
import io.javalin.Javalin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Command-line entry point of the fictional Lantern Tool Library.
 *
 * <pre>
 * reset            recreate the local database with the synthetic sample data
 * report [DATE]    print the database reports as of DATE (default: today), for example 2026-10-15
 * serve            start the JSON API on http://127.0.0.1:$LANTERN_PORT (default 7070), this computer only
 * sql              open an interactive SQL shell on the local database
 * </pre>
 */
public final class App {
    /** The API listens on the loopback address only, so other computers on the network cannot reach it. */
    static final String HOST = "127.0.0.1";

    private App() {
    }

    public static void main(String[] args) {
        String command = args.length == 0 ? "help" : args[0];
        switch (command) {
            case "reset" -> reset(AppConfig.fromSystemEnvironment());
            case "report" -> report(AppConfig.fromSystemEnvironment(), args.length > 1 ? args[1] : null);
            case "serve" -> serve(AppConfig.fromSystemEnvironment());
            case "sql" -> sqlShell(AppConfig.fromSystemEnvironment());
            default -> help();
        }
    }

    private static void help() {
        System.out.println("Lantern Tool Library (fictional). Commands:");
        System.out.println("  reset            recreate the local database with the synthetic sample data");
        System.out.println("  report [DATE]    print the database reports as of DATE, for example 2026-10-15");
        System.out.println("  serve            start the JSON API on http://127.0.0.1:$LANTERN_PORT (default 7070)");
        System.out.println("  sql              open an interactive SQL shell on the local database");
    }

    private static void sqlShell(AppConfig config) {
        database(config);
        System.out.println("SQL shell on " + config.databaseUrl());
        System.out.println("End each statement with ; and type exit to leave.");
        try {
            org.h2.tools.Shell.main("-url", config.databaseUrl());
        } catch (java.sql.SQLException exception) {
            System.err.println("Could not open the database: " + exception.getMessage());
            System.err.println("Stop the API server first, or run ./scripts/reset.sh if the database is missing.");
            System.exit(1);
        }
    }

    private static ConnectionFactory database(AppConfig config) {
        try {
            Files.createDirectories(Path.of("data"));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not create the local data directory", exception);
        }
        return ConnectionFactory.forUrl(config.databaseUrl());
    }

    private static void reset(AppConfig config) {
        DatabaseManager.reset(database(config));
        System.out.println("Local database reset with the synthetic sample data.");
    }

    private static void report(AppConfig config, String dateText) {
        LocalDate asOf = dateText == null ? LocalDate.now() : Dates.parseOrNull(dateText);
        if (asOf == null) {
            System.err.println("Not a date: " + dateText + " (expected something like 2026-10-15)");
            System.exit(2);
        }
        ConnectionFactory connections = database(config);
        DatabaseManager.initializeIfNeeded(connections);
        SqlReports reports = new SqlReports(connections);

        System.out.println("Lantern Tool Library report as of " + asOf);
        List<CategoryUsage> categories = reports.categoryUsage();
        System.out.println("Categories: " + categories.size());
        categories.forEach(row -> System.out.printf("- %s: %d tools, %d on loan, %d loans, demand %s%n",
                row.label(), row.toolCount(), row.onLoanCount(), row.loanCount(), row.demand()));
        List<OverdueLoan> overdue = reports.overdueLoans(asOf);
        System.out.println("Overdue loans: " + overdue.size());
        overdue.forEach(row -> System.out.printf("- loan %d: %s %s, %d days late (%s)%n",
                row.loanId(), row.toolCode(), row.toolName(), row.daysLate(), row.memberId()));
        List<TopTool> top = reports.topToolsPerCategory(1);
        System.out.println("Top tool per category: " + top.size());
        top.forEach(row -> System.out.printf("- %s: %s %s (%d loans)%n",
                row.categoryCode(), row.toolCode(), row.toolName(), row.loanCount()));
        List<MemberRef> idle = reports.membersWithoutLoans();
        System.out.println("Members without loans: " + idle.size());
        idle.forEach(row -> System.out.printf("- %s %s%n", row.id(), row.fullName()));
        System.out.println("Members with late fees:");
        reports.memberBalances().stream().filter(row -> row.feeCents() > 0).forEach(row -> System.out.printf(
                "- balance %s: %s%n", row.memberId(), LendingMath.formatCents(row.feeCents())));
    }

    private static void serve(AppConfig config) {
        if (config.store().equals("jdbc")) {
            database(config);
        }
        Stores stores = StoreFactory.open(config);
        LendingService service = new LendingService(stores.members(), stores.tools(), stores.loans(),
                LocalDate::now);
        Javalin app = LanternApi.create(service, stores.reports(), config, LocalDate::now);
        try {
            listen(app, config.port());
        } catch (RuntimeException exception) {
            System.err.println("Could not start on port " + config.port() + ": " + exception.getMessage());
            System.err.println("If the address is already in use, stop the other server or set LANTERN_PORT.");
            System.exit(1);
        }
        System.out.println("Lantern API listening on http://" + HOST + ":" + app.port() + " with " + config);
        System.out.println("Press Ctrl+C to stop.");
    }

    /** Starts {@code app} on {@link #HOST} and {@code port} (0 = any free port). */
    static Javalin listen(Javalin app, int port) {
        return app.start(HOST, port);
    }
}
