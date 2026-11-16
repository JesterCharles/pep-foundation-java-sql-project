package com.pep.toolshare.reports;

/** One of the most-borrowed tools of a category; {@code rank} starts at 1. */
public record TopTool(String categoryCode, int rank, String toolCode, String toolName, int loanCount) {
}
