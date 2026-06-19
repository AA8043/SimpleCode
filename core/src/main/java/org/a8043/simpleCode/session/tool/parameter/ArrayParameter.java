package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;

@Getter
public class ArrayParameter extends ToolParameter {
    private final ToolParameter type;

    public ArrayParameter(String toolName, String name, boolean isRequired, ToolParameter type) {
        super(toolName, name, isRequired);
        this.type = type;
    }
}
