package com.pep.toolshare.lending;

import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;
import com.pep.toolshare.domain.ToolStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** {@link ToolRepository} kept in a map in memory. Follow {@link InMemoryMemberRepository} as the example. */
public final class InMemoryToolRepository implements ToolRepository {
    // TODO LEARNER 4C: choose the map that stores tools by code.
    private final Map<String, Tool> tools = new HashMap<>();

    @Override
    public List<Tool> findAll() {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: findAll");
    }

    @Override
    public List<Tool> findByCategory(ToolCategory category) {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: findByCategory");
    }

    @Override
    public Tool findByCode(String code) {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: findByCode");
    }

    @Override
    public void add(Tool tool) {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: add");
    }

    @Override
    public boolean updateStatus(String code, ToolStatus status) {
        // TODO LEARNER 4C
        throw new UnsupportedOperationException("TODO LEARNER 4C: updateStatus");
    }
}
