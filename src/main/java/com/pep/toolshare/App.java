package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.db.DatabaseManager;
import com.pep.toolshare.db.LocalDatabase;
import com.pep.toolshare.domain.CategorySummary;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.tool.JdbcToolRepository;
import com.pep.toolshare.tool.ToolRepository;

import java.util.List;

/** Command-line entry point for the fictional Lantern Tool Library backend. */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        ConnectionFactory connections = LocalDatabase.connectionFactory();
        if (args.length == 1 && "reset".equalsIgnoreCase(args[0])) {
            DatabaseManager.reset(connections);
            System.out.println("Local tool-library database reset with synthetic fixtures.");
            return;
        }

        DatabaseManager.initializeIfNeeded(connections);
        ToolRepository tools = new JdbcToolRepository(connections);
        List<Tool> available = tools.findAvailableByCategory(null);
        List<CategorySummary> summaries = tools.summarizeCategories(1);

        System.out.println("Lantern Tool Library (fictional)");
        System.out.println("Available tools: " + available.size());
        available.forEach(tool -> System.out.printf("- %s [%s]%n", tool.name(), tool.category()));
        System.out.println("Category report rows: " + summaries.size());
        summaries.forEach(summary -> System.out.printf(
                "- %s: total=%d, available=%d, active loans=%d%n",
                summary.category(),
                summary.totalTools(),
                summary.availableTools(),
                summary.activeLoans()
        ));
        if (available.isEmpty() || summaries.isEmpty()) {
            System.out.println("Starter state: complete the repository TODOs to populate this report.");
        }
    }
}
