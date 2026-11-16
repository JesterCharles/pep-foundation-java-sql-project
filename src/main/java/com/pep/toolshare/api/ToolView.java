package com.pep.toolshare.api;

import com.pep.toolshare.domain.Tool;

/** JSON shape of a tool. */
public record ToolView(String code, String name, String category, String status, String shelf,
                       long replacementCents) {
    public static ToolView from(Tool tool) {
        return new ToolView(tool.code(), tool.name(), tool.category().name(), tool.status().name(), tool.shelf(),
                tool.replacementCents());
    }
}
