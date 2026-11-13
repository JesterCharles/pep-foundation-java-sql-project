package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.domain.CategorySummary;
import com.pep.toolshare.tool.JdbcToolRepository;
import com.pep.toolshare.tool.ToolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryReportCompletionTest {
    private ToolRepository tools;

    @BeforeEach
    void resetDatabase() {
        ConnectionFactory connections = TestDatabase.create();
        tools = new JdbcToolRepository(connections);
    }

    @Test
    void categoryReportUsesJoinsAggregatesAndHaving() {
        assertEquals(
                List.of(
                        new CategorySummary("carpentry", 3L, 2L, 0L),
                        new CategorySummary("gardening", 1L, 1L, 0L),
                        new CategorySummary("renovation", 1L, 0L, 1L)
                ),
                tools.summarizeCategories(1)
        );
        assertEquals(
                List.of(new CategorySummary("carpentry", 3L, 2L, 0L)),
                tools.summarizeCategories(2)
        );
    }
}
