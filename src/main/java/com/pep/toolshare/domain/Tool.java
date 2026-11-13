package com.pep.toolshare.domain;

import java.util.Objects;

/** A tool that can be borrowed from the fictional library. */
public record Tool(long id, String name, String category, ToolStatus status) {
    public Tool {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        name = requireText(name, "name");
        category = requireText(category, "category");
        Objects.requireNonNull(status, "status");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
