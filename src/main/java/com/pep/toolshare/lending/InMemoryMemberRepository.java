package com.pep.toolshare.lending;

import com.pep.toolshare.domain.Member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A worked example of a repository behind an interface, stored in memory. Use it as the model for
 * {@link InMemoryToolRepository} and {@link InMemoryLoanRepository}.
 */
public final class InMemoryMemberRepository implements MemberRepository {
    private final Map<String, Member> members = new TreeMap<>();
    private final Set<String> membersWithLoans = new HashSet<>();

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members.values());
    }

    @Override
    public Member findById(String id) {
        return members.get(id);
    }

    @Override
    public void add(Member member) {
        if (members.containsKey(member.id())) {
            throw new RuleViolationException("member id already used: " + member.id());
        }
        for (Member existing : members.values()) {
            if (existing.email().equals(member.email())) {
                throw new RuleViolationException("email already used by another member");
            }
        }
        members.put(member.id(), member);
    }

    @Override
    public boolean updateEmail(String id, String email) {
        Member member = members.get(id);
        if (member == null) {
            return false;
        }
        String previous = member.email();
        member.changeEmail(email);
        for (Member other : members.values()) {
            if (other != member && other.email().equals(member.email())) {
                member.changeEmail(previous);
                throw new RuleViolationException("email already used by another member");
            }
        }
        return true;
    }

    @Override
    public boolean delete(String id) {
        if (!members.containsKey(id)) {
            return false;
        }
        if (membersWithLoans.contains(id)) {
            throw new RuleViolationException("member " + id + " has loans on record");
        }
        members.remove(id);
        return true;
    }

    /** Called by the loan repository so that a member with loans cannot be deleted, as in the database. */
    public void recordLoanFor(String memberId) {
        membersWithLoans.add(memberId);
    }
}
