package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;

@Getter
public class NumberParameter extends ToolParameter {
    private final Integer min;
    private final Integer max;

    public NumberParameter(String name, boolean isRequired) {
        this(name, isRequired, null, null);
    }

    public NumberParameter(String name, boolean isRequired, Integer min, Integer max) {
        super(name, isRequired);
        this.min = min;
        this.max = max;
    }
}
