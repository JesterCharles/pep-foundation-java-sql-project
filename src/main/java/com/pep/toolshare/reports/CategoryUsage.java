package com.pep.toolshare.reports;

/** One row of the category usage report. {@code demand} is HIGH (4+ loans), NORMAL (1-3) or NONE (0). */
public record CategoryUsage(String categoryCode, String label, int toolCount, int onLoanCount, int loanCount,
                            String demand) {
}
