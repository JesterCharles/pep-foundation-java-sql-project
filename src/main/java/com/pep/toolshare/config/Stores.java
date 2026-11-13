package com.pep.toolshare.config;

import com.pep.toolshare.lending.LoanRepository;
import com.pep.toolshare.lending.MemberRepository;
import com.pep.toolshare.lending.ToolRepository;
import com.pep.toolshare.reports.LibraryReports;

/** The repositories the application runs on. {@code reports} is {@code null} when data is kept in memory. */
public record Stores(MemberRepository members, ToolRepository tools, LoanRepository loans, LibraryReports reports) {
}
