package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;

import java.util.List;

@Getter
public class StringParameter extends ToolParameter {
    private final List<String> enumList;

    public StringParameter(String name, boolean isRequired, List<String> enumList) {
        super(name, isRequired);
        this.enumList = enumList;
    }

    public StringParameter(String name, boolean isRequired) {
        this(name, isRequired, null);
    }

    public StringParameter() {
        this(null, true);
    }
}
