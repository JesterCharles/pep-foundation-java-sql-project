package com.pep.toolshare.domain;

/** One row of the tool-category report. */
public record CategorySummary(
        String category,
        long totalTools,
        long availableTools,
        long activeLoans
) {
}
