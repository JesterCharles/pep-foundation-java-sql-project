package com.pep.toolshare.reports;

import java.util.HashMap;
import java.util.Map;

/**
 * One {@link WaitList} of member ids per tool code, created the first time a tool is asked for. The server handles
 * requests on several threads, so looking up a list is synchronized.
 */
public final class WaitListBoard {
    private final Map<String, WaitList<String>> lists = new HashMap<>();

    public synchronized WaitList<String> forTool(String toolCode) {
        return lists.computeIfAbsent(toolCode, code -> new WaitList<>());
    }
}
