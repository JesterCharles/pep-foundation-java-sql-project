package com.pep.toolshare.reports;

/** A member's total fees in cents. */
public record MemberBalance(String memberId, String fullName, long feeCents) {
}
