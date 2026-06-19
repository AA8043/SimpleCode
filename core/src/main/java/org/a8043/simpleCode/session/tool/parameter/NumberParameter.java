package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;

@Getter
public class NumberParameter extends ToolParameter {
    private final Integer min;
    private final Integer max;

    public NumberParameter(String toolName, String name, boolean isRequired, Integer min, Integer max) {
        super(toolName, name, isRequired);
        this.min = min;
        this.max = max;
    }
}
