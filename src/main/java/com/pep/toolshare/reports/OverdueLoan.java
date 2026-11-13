package com.pep.toolshare.reports;

/** One overdue open loan. {@code dueOn} is an ISO date such as {@code 2026-10-09}. */
public record OverdueLoan(long loanId, String memberId, String memberName, String toolCode, String toolName,
                          String dueOn, int daysLate) {
}
