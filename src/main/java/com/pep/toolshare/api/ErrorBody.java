package com.pep.toolshare.api;

/** JSON shape of every error response: {@code {"error":"..."}}. */
public record ErrorBody(String error) {
}
