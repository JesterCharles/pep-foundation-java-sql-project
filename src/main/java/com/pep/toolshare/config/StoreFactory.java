package com.pep.toolshare.config;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.DatabaseManager;
import com.pep.toolshare.jdbc.JdbcLoanRepository;
import com.pep.toolshare.jdbc.JdbcMemberRepository;
import com.pep.toolshare.jdbc.JdbcToolRepository;
import com.pep.toolshare.jdbc.SqlReports;

/**
 * Chooses where the application keeps its data. The rest of the program uses only the repository interfaces, so
 * switching from memory to the database changes nothing but the objects built here.
 */
public final class StoreFactory {
    private StoreFactory() {
    }

    /**
     * {@code memory}: {@link SampleData#inMemory()} (no reports). {@code jdbc}: connect to
     * {@code config.databaseUrl()}, create the schema and sample rows if they are missing
     * ({@link DatabaseManager#initializeIfNeeded}), and use the JDBC repositories plus {@link SqlReports}.
     */
    public static Stores open(AppConfig config) {
        // TODO LEARNER 5F: build the JDBC stores when config.store() is "jdbc".
        return SampleData.inMemory();
    }
}
