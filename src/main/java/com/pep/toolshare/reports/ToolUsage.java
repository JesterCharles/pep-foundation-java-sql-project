package com.pep.toolshare.reports;

/** How many times a tool has been borrowed. */
public record ToolUsage(String code, String name, long loanCount) {
}
