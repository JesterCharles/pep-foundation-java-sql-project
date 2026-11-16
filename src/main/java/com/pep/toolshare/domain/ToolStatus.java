package com.pep.toolshare.domain;

/** Where a tool is in its lending life. Stored in the database as the constant name. */
public enum ToolStatus {
    AVAILABLE,
    ON_LOAN,
    IN_REPAIR,
    RETIRED
}
