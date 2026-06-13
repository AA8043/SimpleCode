package org.a8043.simpleCode.session.tool;

import lombok.Getter;
import org.a8043.simpleCode.Settings;

import java.util.List;

@Getter
public class ToolParameter {
    private final String name;
    private final Type type;
    private final String description;
    private final boolean isRequired;
    private final List<String> enumList;

    public ToolParameter(String toolName, String name, Type type, boolean isRequired, List<String> enumList) {
        this.name = name;
        this.type = type;
        description = Settings.PROMPT_JSON.getByPath("tool." + toolName + "." + name, String.class);
        this.isRequired = isRequired;
        this.enumList = enumList;
    }

    public ToolParameter(String toolName, String name, Type type, boolean isRequired) {
        this(toolName, name, type, isRequired, null);
    }

    public enum Type {
        STRING, NUMBER, BOOLEAN, ARRAY
    }
}
