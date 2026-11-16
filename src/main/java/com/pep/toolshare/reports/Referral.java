package com.pep.toolshare.reports;

/** A member and the member who referred them. */
public record Referral(String memberId, String memberName, String referrerId, String referrerName) {
}
