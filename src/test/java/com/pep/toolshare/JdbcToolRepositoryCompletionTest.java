package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.domain.CategorySummary;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.tool.JdbcToolRepository;
import com.pep.toolshare.tool.ToolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcToolRepositoryCompletionTest {
    private ToolRepository tools;

    @BeforeEach
    void resetDatabase() {
        ConnectionFactory connections = TestDatabase.create();
        tools = new JdbcToolRepository(connections);
    }

    @Test
    void mapsOneToolById() {
        assertEquals(
                new Tool(2L, "Tile Cutter", "renovation", ToolStatus.ON_LOAN),
                tools.findById(2L).orElseThrow()
        );
    }

    @Test
    void availableToolQueryFiltersAndSortsDeterministically() {
        assertEquals(
                List.of("Claw Hammer", "Cordless Drill"),
                tools.findAvailableByCategory("carpentry").stream().map(Tool::name).toList()
        );
        assertEquals(
                List.of("Claw Hammer", "Cordless Drill", "Garden Rake"),
                tools.findAvailableByCategory(null).stream().map(Tool::name).toList()
        );
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
