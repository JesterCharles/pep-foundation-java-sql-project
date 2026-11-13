package com.pep.toolshare.config;

import java.util.Map;
import java.util.Objects;

/**
 * Settings read from environment variables, so that no port, password or database location is hard-coded.
 *
 * <pre>
 * LANTERN_PORT         HTTP port, 0-65535 (0 = any free port)   default 7070
 * LANTERN_ADMIN_TOKEN  token required by admin routes           default "" (admin routes disabled)
 * LANTERN_STORE        memory or jdbc                           default memory      (Week 5)
 * LANTERN_DB_URL       an embedded H2 JDBC URL (jdbc:h2:...)     default DEFAULT_DATABASE_URL (Week 5)
 * </pre>
 *
 * <p>Values are stripped; a blank value counts as missing. An invalid value throws
 * {@link IllegalArgumentException} whose message names the variable.
 */
public record AppConfig(int port, String adminToken, String store, String databaseUrl) {
    public static final int DEFAULT_PORT = 7070;
    public static final String DEFAULT_DATABASE_URL =
            "jdbc:h2:file:./data/lantern;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";

    public AppConfig {
        Objects.requireNonNull(adminToken, "adminToken");
        Objects.requireNonNull(store, "store");
        Objects.requireNonNull(databaseUrl, "databaseUrl");
    }

    /** Reads the real process environment. */
    public static AppConfig fromSystemEnvironment() {
        return fromEnvironment(System.getenv());
    }

    /** Reads settings from {@code env}, a map from variable name to value (tests pass their own map). */
    public static AppConfig fromEnvironment(Map<String, String> env) {
        // TODO LEARNER 4B: read LANTERN_PORT and LANTERN_ADMIN_TOKEN.
        int port = DEFAULT_PORT;
        String adminToken = "";
        // TODO LEARNER 5F: read LANTERN_STORE and LANTERN_DB_URL.
        String store = "memory";
        String databaseUrl = DEFAULT_DATABASE_URL;
        return new AppConfig(port, adminToken, store, databaseUrl);
    }

    /** True when an admin token is configured. */
    public boolean adminEnabled() {
        return !adminToken.isEmpty();
    }

    /**
     * Safe to log: the admin token is never shown, only whether it is set. Example:
     * {@code AppConfig[port=7070, adminToken=<set>, store=memory, databaseUrl=jdbc:h2:...]}
     */
    @Override
    public String toString() {
        // TODO LEARNER 4B: never include the token itself.
        return "AppConfig[port=" + port + ", adminToken=" + adminToken + ", store=" + store
                + ", databaseUrl=" + databaseUrl + "]";
    }
}
