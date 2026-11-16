package com.pep.toolshare.api;

/** JSON body of {@code POST /loans}: {@code {"memberId":"M-0001","toolCode":"LT-1001-2"}}. */
public record CheckoutRequest(String memberId, String toolCode) {
}
