package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;

import java.util.List;

@Getter
public class StringParameter extends ToolParameter {
    private final List<String> enumList;

    public StringParameter(String toolName, String name, boolean isRequired, List<String> enumList) {
        super(toolName, name, isRequired);
        this.enumList = enumList;
    }

    public StringParameter(String toolName, String question, boolean isRequired) {
        this(toolName, question, isRequired, null);
    }

    public StringParameter() {
        this(null, null, true);
    }
}
