package com.pep.toolshare.lending;

import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;

import java.util.List;

/** Stores tools. The in-memory and JDBC implementations must behave the same way. */
public interface ToolRepository {
    /** Every tool, ordered by code. */
    List<Tool> findAll();

    /** The tools in {@code category}, ordered by code. */
    List<Tool> findByCategory(ToolCategory category);

    /** The tool with {@code code}, or {@code null} when there is none. */
    Tool findByCode(String code);

    /** Adds a new tool; throws {@link RuleViolationException} when the code is already used. */
    void add(Tool tool);

    /** Sets a tool's status; returns {@code false} when no tool has {@code code}. */
    boolean updateStatus(String code, ToolStatus status);
}
