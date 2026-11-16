package com.pep.toolshare.api;

/** JSON body of {@code POST /tools/{code}/waitlist}: {@code {"memberId":"M-0006"}}. */
public record WaitRequest(String memberId) {
}
