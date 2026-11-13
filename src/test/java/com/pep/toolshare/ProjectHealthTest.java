package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.DatabaseManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectHealthTest {
    @Test
    void resetCreatesTheSameSyntheticFixtureEveryTime() throws Exception {
        ConnectionFactory connections = TestDatabase.create();
        assertSeedCounts(connections);

        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO members (full_name, email) VALUES (?, ?)")) {
            statement.setString(1, "Temporary Member");
            statement.setString(2, "temporary@example.test");
            statement.executeUpdate();
        }
        assertEquals(3L, TestDatabase.count(connections, "members"));

        DatabaseManager.reset(connections);
        assertSeedCounts(connections);
    }

    private static void assertSeedCounts(ConnectionFactory connections) throws Exception {
        assertEquals(2L, TestDatabase.count(connections, "members"));
        assertEquals(5L, TestDatabase.count(connections, "tools"));
        assertEquals(2L, TestDatabase.count(connections, "loans"));
    }
}
