package com.pep.toolshare.lending;

import com.pep.toolshare.basics.LendingMath;
import com.pep.toolshare.domain.FeePolicies;
import com.pep.toolshare.domain.FeePolicy;
import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Member;
import com.pep.toolshare.domain.Receipts;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * The lending desk's rules. It depends only on the repository interfaces and a "today" supplier, all passed in
 * through the constructor, so tests can hand it fakes, stubs or mocks.
 *
 * <p>Failures are reported with {@link InvalidRequestException} (bad input), {@link NotFoundException} (unknown
 * member, tool or loan) and {@link RuleViolationException} (a lending rule refuses the request). See
 * {@code docs/milestones/4-http-api.md} for the exact rules and log lines.
 */
public class LendingService {
    /** A member may have at most this many loans open at once. */
    public static final int MAX_OPEN_LOANS = 3;

    private static final Logger log = LoggerFactory.getLogger(LendingService.class);

    private final MemberRepository members;
    private final ToolRepository tools;
    private final LoanRepository loans;
    private final Supplier<LocalDate> today;

    public LendingService(MemberRepository members, ToolRepository tools, LoanRepository loans,
                          Supplier<LocalDate> today) {
        this.members = Objects.requireNonNull(members, "members");
        this.tools = Objects.requireNonNull(tools, "tools");
        this.loans = Objects.requireNonNull(loans, "loans");
        this.today = Objects.requireNonNull(today, "today");
    }

    /** All tools ordered by code, or only one category's tools when {@code categoryCode} is not null or blank. */
    public List<Tool> tools(String categoryCode) {
        // TODO LEARNER 4D
        throw new UnsupportedOperationException("TODO LEARNER 4D: tools");
    }

    /** The tool with this code after {@link LendingMath#normalizeToolCode(String)}. */
    public Tool tool(String code) {
        // TODO LEARNER 4D
        throw new UnsupportedOperationException("TODO LEARNER 4D: tool");
    }

    public Member member(String id) {
        // TODO LEARNER 4D
        throw new UnsupportedOperationException("TODO LEARNER 4D: member");
    }

    public List<Member> members() {
        return members.findAll();
    }

    /** Lends a tool to a member for the category's maximum loan days, starting today. */
    public Loan checkout(String memberId, String toolCode) {
        // TODO LEARNER 4D
        throw new UnsupportedOperationException("TODO LEARNER 4D: checkout");
    }

    /** Records the return of an open loan today, charging the member's tier fee policy. */
    public Loan checkin(long loanId, boolean damaged) {
        // TODO LEARNER 4D
        throw new UnsupportedOperationException("TODO LEARNER 4D: checkin");
    }

    /** The member's open loans, ordered by id. */
    public List<Loan> openLoans(String memberId) {
        // TODO LEARNER 4D
        throw new UnsupportedOperationException("TODO LEARNER 4D: openLoans");
    }

    /** Every loan, ordered by id. */
    public List<Loan> loans() {
        return loans.findAll();
    }

    /** Takes a tool out of service for good. */
    public void retire(String toolCode) {
        // TODO LEARNER 4D
        throw new UnsupportedOperationException("TODO LEARNER 4D: retire");
    }

    /** The return receipt of a returned loan (see {@link Receipts}). */
    public String receipt(long loanId) {
        // TODO LEARNER 4D
        throw new UnsupportedOperationException("TODO LEARNER 4D: receipt");
    }
}
