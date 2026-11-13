package com.pep.toolshare.api;

/** JSON body of {@code POST /loans/{id}/return}: {@code {"damaged":false}}. The body may be omitted. */
public record ReturnRequest(boolean damaged) {
}
