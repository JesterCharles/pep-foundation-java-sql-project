package com.pep.toolshare.config;

import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.Member;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;
import com.pep.toolshare.lending.LoanRepository;
import com.pep.toolshare.lending.MemberRepository;
import com.pep.toolshare.lending.ToolRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Supplied: wraps the sample-data repositories so the completion checks can see which code called them, for example
 * that {@code LendingService} itself used them. Every call is passed straight through; nothing else changes.
 */
final class ServiceCalls {
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.SHOW_HIDDEN_FRAMES);
    private static final Map<String, AtomicInteger> CALLS = new ConcurrentHashMap<>();
    private static final Set<String> PLATFORM = Set.of("java.", "jdk.", "sun.");

    private ServiceCalls() {
    }

    /** Calls made by {@code type}, its nested classes and its lambdas and method references. */
    static int callsFrom(String type) {
        int total = 0;
        for (Map.Entry<String, AtomicInteger> entry : CALLS.entrySet()) {
            if (entry.getKey().equals(type) || entry.getKey().startsWith(type + "$")) {
                total += entry.getValue().get();
            }
        }
        return total;
    }

    /**
     * Notes the class that called the repository: the first frame above the wrapper that is not Java's own code, so a
     * method reference such as {@code .map(tools::findByCode)} is credited to the class that wrote it.
     */
    private static void seen() {
        WALKER.walk(frames -> frames.skip(2).map(StackWalker.StackFrame::getClassName)
                        .filter(name -> PLATFORM.stream().noneMatch(name::startsWith)).findFirst())
                .ifPresent(name -> CALLS.computeIfAbsent(name, key -> new AtomicInteger()).incrementAndGet());
    }

    static Stores wrap(Stores stores) {
        return new Stores(new Members(stores.members()), new Tools(stores.tools()), new Loans(stores.loans()),
                stores.reports());
    }

    private record Members(MemberRepository repository) implements MemberRepository {
        @Override
        public List<Member> findAll() {
            seen();
            return repository.findAll();
        }

        @Override
        public Member findById(String id) {
            seen();
            return repository.findById(id);
        }

        @Override
        public void add(Member member) {
            seen();
            repository.add(member);
        }

        @Override
        public boolean updateEmail(String id, String email) {
            seen();
            return repository.updateEmail(id, email);
        }

        @Override
        public boolean delete(String id) {
            seen();
            return repository.delete(id);
        }
    }

    private record Tools(ToolRepository repository) implements ToolRepository {
        @Override
        public List<Tool> findAll() {
            seen();
            return repository.findAll();
        }

        @Override
        public List<Tool> findByCategory(ToolCategory category) {
            seen();
            return repository.findByCategory(category);
        }

        @Override
        public Tool findByCode(String code) {
            seen();
            return repository.findByCode(code);
        }

        @Override
        public void add(Tool tool) {
            seen();
            repository.add(tool);
        }

        @Override
        public boolean updateStatus(String code, ToolStatus status) {
            seen();
            return repository.updateStatus(code, status);
        }
    }

    private record Loans(LoanRepository repository) implements LoanRepository {
        @Override
        public Loan checkout(String memberId, String toolCode, LocalDate loanedOn, LocalDate dueOn) {
            seen();
            return repository.checkout(memberId, toolCode, loanedOn, dueOn);
        }

        @Override
        public Loan checkin(long loanId, LocalDate returnedOn, long feeCents, ToolStatus toolStatusAfter) {
            seen();
            return repository.checkin(loanId, returnedOn, feeCents, toolStatusAfter);
        }

        @Override
        public Loan findById(long id) {
            seen();
            return repository.findById(id);
        }

        @Override
        public List<Loan> findOpenByMember(String memberId) {
            seen();
            return repository.findOpenByMember(memberId);
        }

        @Override
        public List<Loan> findAll() {
            seen();
            return repository.findAll();
        }
    }
}
