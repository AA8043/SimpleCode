package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;

@Getter
public class ArrayParameter extends ToolParameter {
    private final ToolParameter type;

    public ArrayParameter(String name, boolean isRequired, ToolParameter type) {
        super(name, isRequired);
        this.type = type;
    }
}
