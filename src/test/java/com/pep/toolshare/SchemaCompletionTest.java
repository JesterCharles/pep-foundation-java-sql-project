package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SchemaCompletionTest {
    private ConnectionFactory connections;

    @BeforeEach
    void resetDatabase() {
        connections = TestDatabase.create();
    }

    @Test
    void memberEmailMustBeUnique() {
        assertThrows(SQLException.class, () -> execute(
                "INSERT INTO members (full_name, email) VALUES (?, ?)",
                "Another Amara", "amara@example.test"));
    }

    @Test
    void toolStatusMustUseTheDeclaredValues() {
        assertThrows(SQLException.class, () -> execute(
                "INSERT INTO tools (name, category, status) VALUES (?, ?, ?)",
                "Mystery Tool", "testing", "MISSING"));
    }

    @Test
    void loanMustReferenceExistingMemberAndToolRows() {
        assertThrows(SQLException.class, () -> execute(
                "INSERT INTO loans (member_id, tool_id, borrowed_on, due_on) "
                        + "VALUES (?, ?, DATE '2030-03-01', DATE '2030-03-08')",
                999L, 1L));
        assertThrows(SQLException.class, () -> execute(
                "INSERT INTO loans (member_id, tool_id, borrowed_on, due_on) "
                        + "VALUES (?, ?, DATE '2030-03-01', DATE '2030-03-08')",
                1L, 999L));
    }

    @Test
    void loanDatesMustMoveForward() {
        assertThrows(SQLException.class, () -> execute(
                "INSERT INTO loans (member_id, tool_id, borrowed_on, due_on) "
                        + "VALUES (1, 3, DATE '2030-03-10', DATE '2030-03-09')"));
        assertThrows(SQLException.class, () -> execute(
                "INSERT INTO loans (member_id, tool_id, borrowed_on, due_on, returned_on) "
                        + "VALUES (1, 3, DATE '2030-03-10', DATE '2030-03-17', DATE '2030-03-09')"));
    }

    private void execute(String sql, Object... values) throws SQLException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }
}
