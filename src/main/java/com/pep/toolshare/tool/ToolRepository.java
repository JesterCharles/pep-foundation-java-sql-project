package com.pep.toolshare.tool;

import com.pep.toolshare.domain.CategorySummary;
import com.pep.toolshare.domain.Tool;

import java.util.List;
import java.util.Optional;

/** Tool lookup and reporting operations. */
public interface ToolRepository {
    Optional<Tool> findById(long id);

    List<Tool> findAvailableByCategory(String category);

    List<CategorySummary> summarizeCategories(int minimumToolCount);
}
