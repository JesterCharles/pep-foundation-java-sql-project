package com.pep.toolshare.reports;

import java.time.LocalDate;
import java.util.List;

/** Management reports answered by SQL queries against the database (Week 5). */
public interface LibraryReports {
    /** Every category with its tool count, tools on loan, total loans and demand label, ordered by code. */
    List<CategoryUsage> categoryUsage();

    /** Codes of the categories with at least {@code minLoans} loans in total, ordered by code. */
    List<String> busyCategories(int minLoans);

    /** Open loans whose due date is before {@code asOf}, ordered by due date, then loan id. */
    List<OverdueLoan> overdueLoans(LocalDate asOf);

    /** Members who have never borrowed anything, ordered by id. */
    List<MemberRef> membersWithoutLoans();

    /** Every member who was referred by another member, with the referrer, ordered by member id. */
    List<Referral> referrals();

    /** The {@code perCategory} most-borrowed tools in each category (ties by code), ordered by category, rank. */
    List<TopTool> topToolsPerCategory(int perCategory);

    /** Every member with the total of their fees (0 when none), highest total first, then by id. */
    List<MemberBalance> memberBalances();
}
