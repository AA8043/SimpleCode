package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;
import org.a8043.simpleCode.SimpleCode;

@Getter
public abstract class ToolParameter {
    private final String name;
    private final String description;
    private final boolean isRequired;

    protected ToolParameter(String toolName, String name, boolean isRequired) {
        this.name = name;
        description = SimpleCode.PROMPT_JSON.getByPath("tool." + toolName + "." + name, String.class);
        this.isRequired = isRequired;
    }
}
