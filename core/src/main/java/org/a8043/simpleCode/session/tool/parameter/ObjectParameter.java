package org.a8043.simpleCode.session.tool.parameter;

import lombok.Getter;

import java.util.List;

@Getter
public class ObjectParameter extends ToolParameter {
    private final List<ToolParameter> content;

    public ObjectParameter(String toolName, String name, boolean isRequired, List<ToolParameter> content) {
        super(toolName, name, isRequired);
        this.content = content;
    }
}
