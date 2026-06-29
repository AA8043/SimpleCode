package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;

@Getter
public abstract class ToolParameter {
    private final String name;
    private final boolean isRequired;

    protected ToolParameter(String name, boolean isRequired) {
        this.name = name;
        this.isRequired = isRequired;
    }
}
